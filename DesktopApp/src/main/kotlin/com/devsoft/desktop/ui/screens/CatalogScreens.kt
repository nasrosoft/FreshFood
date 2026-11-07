package com.devsoft.desktop.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsoft.desktop.MasterController
import com.devsoft.desktop.data.Product
import com.devsoft.desktop.data.PurchaseItem
import com.devsoft.desktop.ui.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID

// ============================================================ PRODUCTS
@Composable
fun ProductsScreen(c: MasterController) {
    var query by remember { mutableStateOf("") }
    var editing by remember { mutableStateOf<Product?>(null) }
    var isNew by remember { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Product?>(null) }

    val list = c.products.filter { query.isBlank() || it.name.contains(query, true) || (it.barcode ?: "").contains(query) }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Produits", "${c.products.size} produit(s) — catalogue partagé avec les téléphones") {
            SearchBox(query, { query = it }, "Nom ou code-barres…")
            Button(onClick = { isNew = true; editing = Product(id = UUID.randomUUID().toString(), name = "") }) { Text("+ Nouveau produit") }
        }
        Panel(Modifier.fillMaxSize(), padding = Dp4.None) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                HeaderCell("Produit", 2f); HeaderCell("Code-barres", 1.2f); HeaderCell("Achat", 0.9f); HeaderCell("Vente", 0.9f)
                HeaderCell("Marge", 0.6f); HeaderCell("Stock", 0.7f); HeaderCell("État", 0.8f); HeaderCell("", 0.3f)
            }
            HorizontalDivider(color = Palette.Border)
            if (list.isEmpty()) EmptyState("Aucun produit")
            LazyColumn {
                items(list, key = { it.id }) { p ->
                    val margin = if (p.selling_price > 0) (p.selling_price - p.purchase_price) / p.selling_price * 100 else 0.0
                    Row(
                        Modifier.fillMaxWidth().clickable { isNew = false; editing = p }.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Cell("${p.emoji ?: "📦"} ${p.name}", 2f, bold = true)
                        Cell(p.barcode ?: "-", 1.2f, color = Palette.Muted)
                        Cell(Fmt.money(p.purchase_price), 0.9f)
                        Cell(Fmt.money(p.selling_price), 0.9f, bold = true)
                        Cell("%.0f %%".format(margin), 0.6f, color = if (margin < 5) Palette.Error else Palette.Muted)
                        Cell("${p.current_stock} ${p.unit}", 0.7f)
                        Box(Modifier.weight(0.8f)) {
                            when {
                                p.current_stock <= 0 -> StatusChip("Rupture", Palette.Error)
                                p.current_stock <= p.min_stock -> StatusChip("Faible", Palette.Warning)
                                else -> StatusChip("En stock", Palette.Success)
                            }
                        }
                        Box(Modifier.weight(0.3f)) {
                            IconButton(onClick = { toDelete = p }) { Icon(Icons.Filled.Delete, "Supprimer", tint = Palette.Subtle) }
                        }
                    }
                    HorizontalDivider(color = Palette.Border.copy(alpha = 0.6f))
                }
            }
        }
    }

    editing?.let { p -> ProductDialog(c, p, isNew) { editing = null } }
    toDelete?.let { p ->
        ConfirmDialog("Supprimer ${p.name} ?", "Le produit sera retiré du catalogue de tous les appareils.", "Supprimer", destructive = true,
            onConfirm = { c.act({ deleteProduct(p.id) }) }, onDismiss = { toDelete = null })
    }
}

@Composable
private fun ProductDialog(c: MasterController, p: Product, isNew: Boolean, onClose: () -> Unit) {
    var name by remember { mutableStateOf(p.name) }
    var emoji by remember { mutableStateOf(p.emoji ?: "📦") }
    var barcode by remember { mutableStateOf(p.barcode ?: "") }
    var unit by remember { mutableStateOf(p.unit) }
    var buy by remember { mutableStateOf(if (isNew) "" else p.purchase_price.toString()) }
    var sell by remember { mutableStateOf(if (isNew) "" else p.selling_price.toString()) }
    var minSell by remember { mutableStateOf(if (isNew) "" else p.min_selling_price.toString()) }
    var minStock by remember { mutableStateOf(p.min_stock.toString()) }
    var desc by remember { mutableStateOf(p.description ?: "") }
    var active by remember { mutableStateOf(p.is_active) }

    FormDialog(
        title = if (isNew) "Nouveau produit" else "Modifier ${p.name}",
        confirmEnabled = name.isNotBlank() && sell.toDoubleOrZero() > 0,
        onConfirm = {
            val updated = p.copy(
                name = name.trim(), emoji = emoji.ifBlank { null }, barcode = barcode.trim().ifBlank { null }, unit = unit.ifBlank { "Unit" },
                purchase_price = buy.toDoubleOrZero(), selling_price = sell.toDoubleOrZero(), min_selling_price = minSell.toDoubleOrZero(),
                min_stock = minStock.toIntOrZero(), description = desc.ifBlank { null }, is_active = active
            )
            c.act({ saveProduct(updated, isNew) }, onDone = onClose)
        },
        onDismiss = onClose
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field("Emoji", emoji, { emoji = it }, Modifier.width(90.dp))
            Field("Nom du produit", name, { name = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field("Code-barres", barcode, { barcode = it }, Modifier.weight(1f))
            Field("Unité (Unit, KG, L, Box…)", unit, { unit = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field("Prix d'achat", buy, { buy = it }, Modifier.weight(1f))
            Field("Prix de vente", sell, { sell = it }, Modifier.weight(1f))
            Field("Prix min.", minSell, { minSell = it }, Modifier.weight(1f))
        }
        Field("Alerte stock minimum", minStock, { minStock = it })
        Field("Description", desc, { desc = it }, singleLine = false)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(active, { active = it }); Text("Produit actif (visible à la vente)")
        }
        if (isNew) Text("Le stock initial s'ajoute via « Entrées de stock » (avec date d'expiration pour le FEFO).", color = Palette.Subtle, fontSize = 12.sp)
    }
}

// ============================================================ PURCHASES (stock inflow)
private data class PurchaseLine(val product: Product, val qty: Int, val price: Double, val expiry: String)

@Composable
fun PurchasesScreen(c: MasterController) {
    var product by remember { mutableStateOf<Product?>(null) }
    var qty by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var expiry by remember { mutableStateOf(LocalDate.now().plusDays(30).toString()) }
    var invoice by remember { mutableStateOf("") }
    val lines = remember { mutableStateListOf<PurchaseLine>() }
    var busy by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val expiryValid = runCatching { LocalDate.parse(expiry) }.isSuccess

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Entrées de stock", "Réception fournisseur : crée un lot (FEFO) et augmente le stock")
        Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Panel(Modifier.weight(1f)) {
                Text("Ajouter une ligne", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                SelectField("Produit", c.products, product, { "${it.name} (stock ${it.current_stock})" }, {
                    product = it; if (it != null) price = it.purchase_price.toString()
                }, Modifier.fillMaxWidth(), searchable = true)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Field("Quantité reçue", qty, { qty = it }, Modifier.weight(1f))
                    Field("Prix d'achat unitaire", price, { price = it }, Modifier.weight(1f))
                }
                Spacer(Modifier.height(10.dp))
                Field("Date d'expiration (AAAA-MM-JJ)", expiry, { expiry = it })
                if (!expiryValid) Text("Format de date invalide", color = Palette.Error, fontSize = 12.sp)
                Spacer(Modifier.height(12.dp))
                Button(
                    enabled = product != null && qty.toIntOrZero() > 0 && expiryValid,
                    onClick = {
                        lines += PurchaseLine(product!!, qty.toIntOrZero(), price.toDoubleOrZero(), expiry)
                        product = null; qty = ""; price = ""
                    }
                ) { Text("Ajouter") }
            }
            Panel(Modifier.weight(1.3f)) {
                Text("Bon de réception", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                Field("N° facture fournisseur (optionnel)", invoice, { invoice = it })
                Spacer(Modifier.height(10.dp))
                if (lines.isEmpty()) EmptyState("Aucune ligne")
                lines.forEachIndexed { i, l ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(l.product.name, Modifier.weight(1f))
                        Text("${l.qty} × ${Fmt.money(l.price)}", Modifier.width(170.dp), fontSize = 13.sp)
                        Text("exp. ${l.expiry}", Modifier.width(110.dp), color = Palette.Muted, fontSize = 13.sp)
                        IconButton(onClick = { lines.removeAt(i) }) { Icon(Icons.Filled.Delete, "Retirer", tint = Palette.Error) }
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Palette.Border)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Total achat", Modifier.weight(1f))
                    Text(Fmt.money(lines.sumOf { it.qty * it.price }), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                message?.let { Text(it, color = Palette.Success, fontSize = 13.sp) }
                Spacer(Modifier.height(10.dp))
                Button(
                    enabled = lines.isNotEmpty() && !busy,
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        busy = true; message = null
                        scope.launch {
                            try {
                                val id = c.repo.processPurchase(invoice, lines.map { PurchaseItem(it.product.id, it.qty, it.price, it.expiry) })
                                message = "Réception $id enregistrée."
                                lines.clear(); invoice = ""
                                c.refreshAll()
                            } catch (e: Exception) {
                                c.error = c.friendly(e)
                            } finally { busy = false }
                        }
                    }
                ) { Text("Valider la réception") }
            }
        }
    }
}
