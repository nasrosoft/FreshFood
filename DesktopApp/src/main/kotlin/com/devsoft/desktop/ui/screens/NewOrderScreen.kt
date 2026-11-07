package com.devsoft.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsoft.desktop.MasterController
import com.devsoft.desktop.data.Customer
import com.devsoft.desktop.data.Product
import com.devsoft.desktop.data.Profile
import com.devsoft.desktop.ui.*
import kotlinx.coroutines.launch

/**
 * Counter sale or dispatch of a delivery order to a driver's phone.
 * Uses process_sale exactly like the Android POS: a delivery creates a draft (PENDING) sale
 * whose stock and credit are only committed when the driver marks it LIVRÉE.
 */
@Composable
fun NewOrderScreen(c: MasterController, onDispatched: () -> Unit) {
    var query by remember { mutableStateOf("") }
    val cart = remember { mutableStateMapOf<String, Int>() }
    var customer by remember { mutableStateOf<Customer?>(null) }
    var credit by remember { mutableStateOf(false) }
    var delivery by remember { mutableStateOf(true) }
    var driver by remember { mutableStateOf<Profile?>(null) }
    var busy by remember { mutableStateOf(false) }
    var result by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val byId = c.products.associateBy { it.id }
    val lines = cart.entries.mapNotNull { (id, q) -> byId[id]?.let { it to q } }
    val total = lines.sumOf { (p, q) -> p.selling_price * q }
    val visible = c.products.filter {
        it.is_active && (query.isBlank() || it.name.contains(query, true) || (it.barcode ?: "").contains(query))
    }

    fun add(p: Product, delta: Int) {
        val next = (cart[p.id] ?: 0) + delta
        when {
            next <= 0 -> cart.remove(p.id)
            next > p.current_stock -> cart[p.id] = p.current_stock.coerceAtLeast(0)
            else -> cart[p.id] = next
        }
        if ((cart[p.id] ?: 1) <= 0) cart.remove(p.id)
    }

    val creditProblem = when {
        credit && customer == null -> "Une vente à crédit nécessite un client."
        credit && customer != null && customer!!.credit_limit > 0 &&
            customer!!.current_credit + total > customer!!.credit_limit ->
            "Plafond de crédit dépassé (${Fmt.money(customer!!.current_credit)} / ${Fmt.money(customer!!.credit_limit)})."
        else -> null
    }
    val canSubmit = lines.isNotEmpty() && !busy && creditProblem == null && (!delivery || customer != null)

    Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
        // ---------------- catalogue
        Column(Modifier.weight(1.6f)) {
            ScreenHeader("Nouvelle commande", "Vente comptoir ou envoi d'une livraison au téléphone d'un livreur") {
                SearchBox(query, { query = it }, "Nom ou code-barres…")
            }
            LazyVerticalGrid(GridCells.Adaptive(190.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(visible, key = { it.id }) { p ->
                    val inCart = cart[p.id] ?: 0
                    val out = p.current_stock <= 0
                    Column(
                        Modifier.clip(RoundedCornerShape(12.dp))
                            .background(if (inCart > 0) Palette.PrimaryContainer else Palette.Surface)
                            .clickable(enabled = !out) { add(p, 1) }
                            .padding(12.dp)
                    ) {
                        Text("${p.emoji ?: "📦"}  ${p.name}", fontWeight = FontWeight.SemiBold, maxLines = 2, color = if (out) Palette.Subtle else Palette.Text)
                        Spacer(Modifier.height(6.dp))
                        Text(Fmt.money(p.selling_price), color = Palette.Primary, fontWeight = FontWeight.Bold)
                        Text(
                            if (out) "Rupture" else "Stock : ${p.current_stock} ${p.unit}",
                            color = if (out) Palette.Error else if (p.current_stock <= p.min_stock) Palette.Warning else Palette.Muted,
                            fontSize = 12.sp
                        )
                        if (inCart > 0) Text("Dans le panier : $inCart", color = Palette.OnPrimaryContainer, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        // ---------------- cart / checkout
        Panel(Modifier.weight(1f).fillMaxHeight()) {
            Text("Panier", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                if (lines.isEmpty()) EmptyState("Cliquez sur un produit pour l'ajouter")
                lines.forEach { (p, q) ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(p.name, fontSize = 14.sp, maxLines = 1)
                            Text("${Fmt.money(p.selling_price)} × $q = ${Fmt.money(p.selling_price * q)}", color = Palette.Muted, fontSize = 12.sp)
                        }
                        IconButton(onClick = { add(p, -1) }) { Icon(Icons.Filled.Remove, "Moins") }
                        Text("$q", fontWeight = FontWeight.Bold)
                        IconButton(onClick = { add(p, 1) }, enabled = q < p.current_stock) { Icon(Icons.Filled.Add, "Plus") }
                        IconButton(onClick = { cart.remove(p.id) }) { Icon(Icons.Filled.Delete, "Retirer", tint = Palette.Error) }
                    }
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Palette.Border)

            SelectField(
                "Client", c.customers.filter { it.is_active }, customer,
                { "${it.name}${if (it.current_credit > 0) " · crédit ${Fmt.money(it.current_credit)}" else ""}" },
                { customer = it }, Modifier.fillMaxWidth(), allowNone = "Client comptoir (aucun)", searchable = true
            )
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(!credit, { credit = false }, label = { Text("Espèces") })
                FilterChip(credit, { credit = true }, label = { Text("À crédit") })
                Spacer(Modifier.width(12.dp))
                FilterChip(delivery, { delivery = true }, label = { Text("Livraison") })
                FilterChip(!delivery, { delivery = false }, label = { Text("Comptoir") })
            }
            if (delivery) {
                Spacer(Modifier.height(10.dp))
                SelectField(
                    "Livreur (reçoit la commande sur son téléphone)", c.drivers.filter { it.is_active }, driver,
                    { it.displayName }, { driver = it }, Modifier.fillMaxWidth(), allowNone = "Non assigné (à dispatcher plus tard)"
                )
                Text(
                    "Livraison = brouillon : le stock et le crédit sont validés quand le livreur marque la commande LIVRÉE.",
                    color = Palette.Subtle, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)
                )
                if (customer == null) Text("Choisissez un client pour une livraison.", color = Palette.Warning, fontSize = 12.sp)
            }
            creditProblem?.let { Text(it, color = Palette.Error, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)) }
            result?.let { Text(it, color = Palette.Success, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp)) }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total", Modifier.weight(1f), fontSize = 16.sp)
                Text(Fmt.money(total), fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Palette.Primary)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                onClick = {
                    busy = true; result = null
                    scope.launch {
                        try {
                            val req = c.repo.newSaleRequest(lines, if (credit) "CREDIT" else "CASH", customer?.id, delivery, if (delivery) driver?.id else null)
                            val invoice = c.repo.processSale(req)
                            result = if (delivery) "Commande $invoice envoyée${driver?.let { " à ${it.displayName}" } ?: ""}." else "Vente $invoice enregistrée."
                            cart.clear()
                            c.refreshAll()
                            if (delivery) onDispatched()
                        } catch (e: Exception) {
                            c.error = c.friendly(e)
                        } finally {
                            busy = false
                        }
                    }
                }
            ) {
                Text(if (delivery) "Envoyer la livraison" else "Valider la vente")
            }
        }
    }
}
