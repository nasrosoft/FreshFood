package com.devsoft.desktop.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsoft.desktop.MasterController
import com.devsoft.desktop.data.DeliveryOrderWithDetails
import com.devsoft.desktop.data.Profile
import com.devsoft.desktop.ui.*

private enum class DeliveryFilter(val label: String) { ACTIVE("En cours"), DELIVERED("Livrées"), CANCELLED("Annulées"), ALL("Toutes") }

@Composable
fun DeliveriesScreen(c: MasterController) {
    var filter by remember { mutableStateOf(DeliveryFilter.ACTIVE) }
    var driverFilter by remember { mutableStateOf<Profile?>(null) }
    var query by remember { mutableStateOf("") }
    var openId by remember { mutableStateOf<String?>(null) }

    val list = c.deliveries.filter { d ->
        when (filter) {
            DeliveryFilter.ACTIVE -> d.order.status in DeliveryStatus.active
            DeliveryFilter.DELIVERED -> d.order.status == "DELIVERED"
            DeliveryFilter.CANCELLED -> d.order.status == "CANCELLED"
            DeliveryFilter.ALL -> true
        } && (driverFilter == null || d.order.delivery_employee_id == driverFilter!!.id) &&
            (query.isBlank() || (d.customer?.name ?: "").contains(query, true) || d.order.id.startsWith(query, true))
    }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Livraisons", "Dispatch et suivi en direct des téléphones des livreurs (actualisation toutes les 10 s)") {
            SearchBox(query, { query = it }, "Client ou n° commande…", Modifier.width(240.dp))
            SelectField("Livreur", c.drivers, driverFilter, { it.displayName }, { driverFilter = it }, Modifier.width(220.dp), allowNone = "Tous les livreurs")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 12.dp)) {
            DeliveryFilter.entries.forEach { f ->
                val n = c.deliveries.count {
                    when (f) {
                        DeliveryFilter.ACTIVE -> it.order.status in DeliveryStatus.active
                        DeliveryFilter.DELIVERED -> it.order.status == "DELIVERED"
                        DeliveryFilter.CANCELLED -> it.order.status == "CANCELLED"
                        DeliveryFilter.ALL -> true
                    }
                }
                FilterChip(filter == f, { filter = f }, label = { Text("${f.label} ($n)") })
            }
        }
        Panel(Modifier.fillMaxSize(), padding = Dp4.None) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                HeaderCell("Commande", 0.9f); HeaderCell("Client", 1.6f); HeaderCell("Livreur", 1.3f); HeaderCell("Articles", 0.7f)
                HeaderCell("Montant", 1f); HeaderCell("Paiement", 0.8f); HeaderCell("Statut", 0.9f); HeaderCell("Mis à jour", 1f)
            }
            HorizontalDivider(color = Palette.Border)
            if (list.isEmpty()) EmptyState("Aucune livraison")
            LazyColumn {
                items(list, key = { it.order.id }) { d ->
                    Row(
                        Modifier.fillMaxWidth().clickable { openId = d.order.id }.padding(horizontal = 18.dp, vertical = 11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Cell(Fmt.shortId(d.order.id), 0.9f, color = Palette.Muted)
                        Cell(d.customer?.name ?: "-", 1.6f, bold = true)
                        Cell(d.driver?.displayName ?: "— non assignée", 1.3f, color = if (d.driver == null) Palette.Warning else Palette.Text)
                        Cell("${d.items.sumOf { it.item.quantity }}", 0.7f)
                        Cell(Fmt.money(d.total), 1f, bold = true)
                        Cell(if (d.sale?.payment_method == "CREDIT") "Crédit" else "Espèces", 0.8f)
                        Box(Modifier.weight(0.9f)) { StatusChip(DeliveryStatus.label(d.order.status), DeliveryStatus.color(d.order.status)) }
                        Cell(Fmt.ago(d.order.updated_at ?: d.order.created_at), 1f, color = Palette.Muted)
                    }
                    HorizontalDivider(color = Palette.Border.copy(alpha = 0.6f))
                }
            }
        }
    }

    openId?.let { id ->
        val d = c.deliveries.firstOrNull { it.order.id == id }
        if (d == null) openId = null else DeliveryDialog(c, d) { openId = null }
    }
}

@Composable
private fun DeliveryDialog(c: MasterController, d: DeliveryOrderWithDetails, onClose: () -> Unit) {
    val editable = d.order.status in DeliveryStatus.active
    val qty = remember(d.order.id) { mutableStateMapOf<String, Int>().apply { d.items.forEach { put(it.item.id, it.item.quantity) } } }
    var payment by remember(d.order.id) { mutableStateOf(d.sale?.payment_method ?: "CASH") }
    var confirm by remember { mutableStateOf<String?>(null) }
    val finalTotal = d.items.sumOf { (qty[it.item.id] ?: 0) * (it.product?.selling_price ?: 0.0) }

    FormDialog(
        title = "Livraison ${Fmt.shortId(d.order.id)} — ${d.customer?.name ?: "client"}",
        confirmLabel = "Fermer",
        width = 640,
        onConfirm = onClose,
        onDismiss = onClose
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            StatusChip(DeliveryStatus.label(d.order.status), DeliveryStatus.color(d.order.status))
            Spacer(Modifier.width(10.dp))
            Text("Créée ${Fmt.dateTime(d.order.created_at)} · facture ${d.sale?.invoice_number ?: "-"}", color = Palette.Muted, fontSize = 13.sp)
        }
        d.customer?.let {
            Text("📞 ${it.phone ?: "-"}   📍 ${listOfNotNull(it.address, it.commune, it.wilaya).joinToString(", ").ifBlank { "-" }}", fontSize = 13.sp)
            Text("Crédit actuel du client : ${Fmt.money(it.current_credit)}", fontSize = 13.sp, color = Palette.Muted)
        }

        if (editable) {
            SelectField(
                "Livreur assigné", c.drivers.filter { it.is_active }, d.driver, { it.displayName },
                { p -> c.act({ assignDriver(d.order.id, p?.id) }) }, Modifier.fillMaxWidth(), allowNone = "Non assigné"
            )
        } else {
            Text("Livreur : ${d.driver?.displayName ?: "-"}", fontSize = 14.sp)
        }

        Text("Articles", fontWeight = FontWeight.SemiBold)
        d.items.forEach { line ->
            val q = qty[line.item.id] ?: 0
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(line.product?.name ?: "Produit supprimé", Modifier.weight(1f))
                Text(Fmt.money(line.product?.selling_price ?: 0.0), Modifier.width(110.dp), color = Palette.Muted, fontSize = 13.sp)
                if (editable) {
                    IconButton(onClick = { qty[line.item.id] = (q - 1).coerceAtLeast(0) }) { Icon(Icons.Filled.Remove, "Moins") }
                    Text("$q", fontWeight = FontWeight.Bold, modifier = Modifier.width(28.dp))
                    IconButton(onClick = { qty[line.item.id] = q + 1 }) { Icon(Icons.Filled.Add, "Plus") }
                } else {
                    Text("× $q", fontWeight = FontWeight.Bold)
                }
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Total ${if (editable) "final" else ""}", Modifier.weight(1f))
            Text(Fmt.money(if (editable) finalTotal else d.total), fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Palette.Primary)
        }

        if (editable) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Paiement final :", fontSize = 13.sp)
                FilterChip(payment != "CREDIT", { payment = "CASH" }, label = { Text("Espèces") })
                FilterChip(payment == "CREDIT", { payment = "CREDIT" }, label = { Text("Crédit") })
            }
            Text(
                "« Marquer livrée » est le point de validation unique : stock (FEFO), facture et crédit client sont ajustés sur les quantités livrées.",
                color = Palette.Subtle, fontSize = 12.sp
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { confirm = "deliver" }, colors = ButtonDefaults.buttonColors(containerColor = Palette.Success)) { Text("Marquer livrée") }
                if (d.order.status != "OUT_FOR_DELIVERY") OutlinedButton(onClick = { c.act({ updateDeliveryStatus(d.order.id, "OUT_FOR_DELIVERY") }) }) { Text("En route") }
                OutlinedButton(onClick = { confirm = "cancel" }) { Text("Annuler la commande", color = Palette.Error) }
            }
        }
        TextButton(onClick = { confirm = "delete" }) { Text("Supprimer définitivement", color = Palette.Error) }
    }

    when (confirm) {
        "deliver" -> ConfirmDialog(
            "Valider la livraison ?",
            "Total final ${Fmt.money(finalTotal)} en ${if (payment == "CREDIT") "crédit" else "espèces"}. Le stock et le crédit client seront mis à jour.",
            "Valider",
            onConfirm = { c.act({ completeDelivery(d.order.id, qty.toMap(), payment) }, onDone = onClose) },
            onDismiss = { confirm = null }
        )
        "cancel" -> ConfirmDialog(
            "Annuler la livraison ?", "La commande disparaîtra du téléphone du livreur. Aucun stock n'a encore été déduit.",
            "Annuler la commande", destructive = true,
            onConfirm = { c.act({ updateDeliveryStatus(d.order.id, "CANCELLED") }) },
            onDismiss = { confirm = null }
        )
        "delete" -> ConfirmDialog(
            "Supprimer la livraison ?", "Cette action est définitive.", "Supprimer", destructive = true,
            onConfirm = { c.act({ deleteDelivery(d.order.id) }, onDone = onClose) },
            onDismiss = { confirm = null }
        )
    }
}
