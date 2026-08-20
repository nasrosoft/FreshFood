# Spécifications & Comportements : Retours Produits, Stock & Crédit Client

Ce document détaille les règles métier et les impacts en base de données lors du retour de produits, de la modification de quantités (réduction et augmentation) et de la validation finale de la livraison pour les modes de paiement **Crédit** et **Espèces**.

---

## 1. Principe Fondamental : La Livraison Finale est la Source de Vérité

Une commande peut être créée et éditée (quantités augmentées ou diminuées) avant sa confirmation/livraison finale.
- **Pendant la phase d'édition / livraison en cours** :
  - L'interface calcule et affiche le total en temps réel.
  - La dette finale du client (`customers.current_credit`) et l'écriture définitive `DEBT` sont **établies à partir des quantités réellement livrées** au moment de la validation.
- **Lors de la validation de la livraison** :
  - Les ajustements de stock, de facture (`sales`), d'articles (`sale_items` & `delivery_items`) et de crédit client sont exécutés de façon **atomique**.

---

## 2. Cas d'une Vente à CRÉDIT (Customer Credit)

### Exemple de Référence :
- Solde initial du client : **14 000 DA**
- Commande initiale : **4 Milka × 500 DA = 2 000 DA**
- Modification avant validation : **4 → 2 Milka** (Retour de 2 Milka = 1 000 DA)
- Total de commande final : **2 Milka × 500 DA = 1 000 DA**

### Résultat Final en Base de Données :
- `customers.current_credit` = 14 000 + 1 000 = **15 000 DA** (et JAMAIS 16 000 DA).
- `sales.total_amount` = **1 000 DA**
- `sales.credit_amount` = **1 000 DA**
- `sales.paid_amount` = **0 DA**
- `delivery_items.quantity` = **2**
- `sale_items.quantity` = **2** (subtotal = 1 000 DA)
- `products.current_stock` = Stock précédent + **2**
- `stock_batches.quantity` = Quantité de lot précédente + **2**
- `credit_transactions.amount` = **1 000 DA** (type `DEBT`)
- `customer_credit_details` : `items_summary` = `2x Milka`, `total_amount` = 1 000 DA

---

## 3. Gestion Bidirectionnelle des Quantités (Idempotence & Ajustements)

Le système calcule toujours le delta entre la quantité enregistrée et la nouvelle quantité :
$$\text{différence} = \text{quantité précédente} - \text{nouvelle quantité}$$

### Cas A : Réduction ($4 \rightarrow 2 \rightarrow 1$)
- $4 \rightarrow 2$ : différence = $+2 \implies$ **+2 remis en stock** (`movement_type = DELIVERY_RETURN`).
- $2 \rightarrow 1$ : différence = $+1 \implies$ **+1 additionnel remis en stock**.
- Total retourné au stock = 3. Quantité livrée = 1.

### Cas B : Ré-augmentation ($4 \rightarrow 2 \rightarrow 3$)
- $4 \rightarrow 2$ : +2 remis en stock.
- $2 \rightarrow 3$ : différence = $-1 \implies$ **1 unité prélevée du stock** (`movement_type = DELIVERY`) selon la méthode FEFO.
- Quantité livrée finale = 3.

---

## 4. Cas d'une Vente en ESPÈCES (Cash)

- Le même mécanisme de gestion de stock s'applique.
- `sales.total_amount` = Nouveau Total (ex: 1 000 DA)
- `sales.paid_amount` = Nouveau Total (ex: 1 000 DA)
- `sales.credit_amount` = 0 DA
- Le solde crédit du client (`current_credit`) reste **inchangé** (les espèces n'affectent pas la dette).

---

## 5. Cas Spécial : Conversion ESPÈCES ➔ CRÉDIT à la livraison

Si la commande a été initialement créée en Espèces mais que le client demande à la passer à Crédit lors de la livraison :
1. Les quantités retournées réintègrent le stock.
2. Le total final livré est calculé.
3. `sales.payment_method = 'CREDIT'`, `paid_amount = 0`, `credit_amount = total final`.
4. La transaction d'espèces est supprimée.
5. Une transaction `DEBT` de la valeur finale est créée dans `credit_transactions`.
6. Le journal `customer_credit_details` est alimenté.
7. `customers.current_credit` augmente uniquement du montant final livré.

---

## 6. Table Récapitulative des Impacts BDD

| Composant | Action lors de la Validation |
| :--- | :--- |
| **`products`** | `current_stock` ajusté du delta (+ pour retours, - pour ajouts) |
| **`stock_batches`** | Quantité de lot ajustée (FEFO) |
| **`stock_movements`** | Mouvement `DELIVERY_RETURN` ou `DELIVERY` |
| **`sale_items`** | Quantités et sous-totaux mis à jour (supprimé si 0) |
| **`delivery_items`** | Quantités mises à jour vers la quantité finale |
| **`sales`** | `total_amount` recalculé sur la somme des `sale_items` |
| **`credit_transactions`** | Montant de la dette `DEBT` fixé au total final |
| **`customers`** | `current_credit` recalculé automatiquement via trigger |
| **`customer_credit_details`** | Résumé d'audit synchronisé avec les articles livrés |
| **`delivery_orders`** | Statut mis à jour à `'DELIVERED'` |

---

## 7. Diagramme de la Transaction Atomique

```
           [Livreur / Admin valide la livraison]
                              │
                              ▼
           [Calcul des deltas (ancien vs nouveau)]
                              │
             ┌────────────────┴────────────────┐
             ▼                                 ▼
   [Si Réduction (Retour)]           [Si Augmentation]
   • +Qté dans stock & lots          • -Qté dans stock & lots (FEFO)
   • Movement: DELIVERY_RETURN       • Movement: DELIVERY
             │                                 │
             └────────────────┬────────────────┘
                              ▼
                [Mise à jour sale_items & delivery_items]
                              ▼
                [Recalcul total_amount exact]
                              ▼
             ┌────────────────┴────────────────┐
             ▼                                 ▼
      [Si CRÉDIT]                        [Si ESPÈCES]
   • sales.credit_amount = total     • sales.paid_amount = total
   • credit_transactions = total     • payments = total
   • current_credit client ajusté    • Pas d'impact sur crédit
   • customer_credit_details màj
                              │
                              ▼
                 [delivery_orders = DELIVERED]
                              │
                              ▼
                    [COMMIT TRANSACTION]
```
