package com.devsoft.desktop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsoft.desktop.MasterController
import com.devsoft.desktop.Section
import com.devsoft.desktop.ui.*
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun DashboardScreen(c: MasterController, go: (Section) -> Unit) {
    val today = LocalDate.now()
    fun isToday(ts: String?) = Fmt.parse(ts)?.atZoneSameInstant(ZoneId.systemDefault())?.toLocalDate() == today

    val todaySales = c.sales.filter { it.status == "COMPLETED" && isToday(it.created_at) }
    val salesTotal = todaySales.sumOf { it.total_amount }
    val creditTotal = c.customers.sumOf { it.current_credit }
    val lowStock = c.products.filter { it.current_stock <= it.min_stock }
    val active = c.deliveries.filter { it.order.status in DeliveryStatus.active }
    val deliveredToday = c.deliveries.count { it.order.status == "DELIVERED" && isToday(it.order.updated_at) }
    val onlinePhones = c.devices.count {
        it.platform == "ANDROID" && it.status == "APPROVED" &&
            (Fmt.parse(it.last_seen_at)?.toInstant()?.toEpochMilli() ?: 0) > System.currentTimeMillis() - 5 * 60_000
    }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        ScreenHeader("Tableau de bord", "Vue globale de l'entreprise — mise à jour en temps réel") {
            OutlinedButton(onClick = { c.refreshAll() }) {
                Icon(Icons.Filled.Refresh, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Actualiser")
            }
            Button(onClick = { go(Section.NEW_ORDER) }) { Text("+ Nouvelle commande") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            KpiCard("Ventes du jour", Fmt.money(salesTotal), "${todaySales.size} vente(s)", Palette.Primary, Modifier.weight(1f))
            KpiCard("Crédit clients", Fmt.money(creditTotal), "${c.customers.count { it.current_credit > 0 }} client(s) débiteur(s)", Palette.Warning, Modifier.weight(1f))
            KpiCard("Livraisons actives", "${active.size}", "$deliveredToday livrée(s) aujourd'hui", Palette.Info, Modifier.weight(1f))
            KpiCard("Stock faible", "${lowStock.size}", "produit(s) à réapprovisionner", if (lowStock.isEmpty()) Palette.Success else Palette.Error, Modifier.weight(1f))
            KpiCard("Téléphones connectés", "$onlinePhones", "${c.pendingDevices} en attente d'approbation", Palette.Success, Modifier.weight(1f))
        }
        Spacer(Modifier.height(18.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            // Live fleet
            Panel(Modifier.weight(1.4f).heightIn(min = 320.dp)) {
                Text("Flotte en direct", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(10.dp))
                if (active.isEmpty()) EmptyState("Aucune livraison en cours")
                val byDriver = active.groupBy { it.driver?.displayName ?: "Non assignée" }
                byDriver.forEach { (driver, orders) ->
                    Text("$driver · ${orders.size} commande(s)", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                    orders.forEach { d ->
                        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(Fmt.shortId(d.order.id), Modifier.width(100.dp), color = Palette.Muted, fontSize = 13.sp)
                            Text(d.customer?.name ?: "-", Modifier.weight(1f), fontSize = 14.sp)
                            Text(Fmt.money(d.total), Modifier.width(130.dp), fontSize = 14.sp)
                            StatusChip(DeliveryStatus.label(d.order.status), DeliveryStatus.color(d.order.status))
                        }
                    }
                }
            }
            // Low stock
            Panel(Modifier.weight(1f).heightIn(min = 320.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Stock faible", fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    TextButton(onClick = { go(Section.PURCHASES) }) { Text("Réapprovisionner") }
                }
                if (lowStock.isEmpty()) EmptyState("Tout le stock est sain 👍")
                lowStock.take(12).forEach { p ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("${p.emoji ?: "📦"} ${p.name}", Modifier.weight(1f), fontSize = 14.sp)
                        Text("${p.current_stock} / min ${p.min_stock}", color = if (p.current_stock <= 0) Palette.Error else Palette.Warning, fontSize = 13.sp)
                    }
                }
            }
        }
        Spacer(Modifier.height(18.dp))

        Panel(Modifier.fillMaxWidth()) {
            Text("Dernières ventes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(10.dp))
            Row { HeaderCell("Facture", 1.2f); HeaderCell("Client", 1.5f); HeaderCell("Mode", 0.8f); HeaderCell("Statut", 0.8f); HeaderCell("Montant", 1f); HeaderCell("Date", 1f) }
            HorizontalDivider(Modifier.padding(vertical = 6.dp), color = Palette.Border)
            val names = c.customers.associate { it.id to it.name }
            if (c.sales.isEmpty()) EmptyState("Aucune vente")
            c.sales.take(10).forEach { s ->
                Row(Modifier.padding(vertical = 5.dp)) {
                    Cell(s.invoice_number ?: "-", 1.2f)
                    Cell(s.customer_id?.let { names[it] } ?: "Comptoir", 1.5f)
                    Cell(if (s.payment_method == "CREDIT") "Crédit" else "Espèces", 0.8f)
                    Cell(if (s.status == "PENDING") "Brouillon" else "Validée", 0.8f, color = if (s.status == "PENDING") Palette.Warning else Palette.Success)
                    Cell(Fmt.money(s.total_amount), 1f, bold = true)
                    Cell(Fmt.dateTime(s.created_at), 1f, color = Palette.Muted)
                }
            }
        }
    }
}
