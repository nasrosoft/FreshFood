package com.devsoft.desktop.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsoft.desktop.MasterController
import com.devsoft.desktop.data.Customer
import com.devsoft.desktop.data.CustomerCreditDetail
import com.devsoft.desktop.ui.*
import kotlinx.coroutines.launch
import java.util.UUID

// ============================================================ CUSTOMERS
@Composable
fun CustomersScreen(c: MasterController) {
    var query by remember { mutableStateOf("") }
    var onlyDebtors by remember { mutableStateOf(false) }
    var selectedId by remember { mutableStateOf<String?>(null) }
    var editing by remember { mutableStateOf<Customer?>(null) }
    var isNew by remember { mutableStateOf(false) }

    val list = c.customers.filter {
        (query.isBlank() || it.name.contains(query, true) || (it.phone ?: "").contains(query)) && (!onlyDebtors || it.current_credit > 0)
    }
    val selected = c.customers.firstOrNull { it.id == selectedId }

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Clients & crédit", "Total dû : ${Fmt.money(c.customers.sumOf { it.current_credit })}") {
            FilterChip(onlyDebtors, { onlyDebtors = !onlyDebtors }, label = { Text("Débiteurs uniquement") })
            SearchBox(query, { query = it }, "Nom ou téléphone…", Modifier.width(240.dp))
            Button(onClick = { isNew = true; editing = Customer(id = UUID.randomUUID().toString(), name = "") }) { Text("+ Nouveau client") }
        }
        Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(18.dp)) {
            Panel(Modifier.weight(1.3f).fillMaxHeight(), padding = Dp4.None) {
                Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                    HeaderCell("Client", 1.6f); HeaderCell("Téléphone", 1f); HeaderCell("Type", 0.8f); HeaderCell("Crédit", 1f); HeaderCell("Plafond", 1f)
                }
                HorizontalDivider(color = Palette.Border)
                LazyColumn {
                    items(list, key = { it.id }) { cu ->
                        Row(
                            Modifier.fillMaxWidth()
                                .background(if (cu.id == selectedId) Palette.PrimaryContainer else Palette.Surface)
                                .clickable { selectedId = cu.id }
                                .padding(horizontal = 18.dp, vertical = 10.dp)
                        ) {
                            Cell(cu.name, 1.6f, bold = true)
                            Cell(cu.phone ?: "-", 1f, color = Palette.Muted)
                            Cell(cu.customer_type ?: "-", 0.8f, color = Palette.Muted)
                            Cell(Fmt.money(cu.current_credit), 1f, bold = true, color = if (cu.current_credit > 0) Palette.Warning else Palette.Muted)
                            Cell(if (cu.credit_limit > 0) Fmt.money(cu.credit_limit) else "—", 1f, color = Palette.Muted)
                        }
                        HorizontalDivider(color = Palette.Border.copy(alpha = 0.6f))
                    }
                }
            }
            Box(Modifier.weight(1f).fillMaxHeight()) {
                if (selected == null) Panel(Modifier.fillMaxWidth()) { EmptyState("Sélectionnez un client") }
                else CustomerDetail(c, selected, onEdit = { isNew = false; editing = selected }, onDeleted = { selectedId = null })
            }
        }
    }
    editing?.let { cu -> CustomerDialog(c, cu, isNew) { editing = null } }
}

@Composable
private fun CustomerDetail(c: MasterController, cu: Customer, onEdit: () -> Unit, onDeleted: () -> Unit) {
    var history by remember(cu.id) { mutableStateOf<List<CustomerCreditDetail>?>(null) }
    var amount by remember(cu.id) { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    var confirmPay by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(cu.id, cu.current_credit) {
        history = runCatching { c.repo.getCreditHistory(cu.id) }.getOrDefault(emptyList())
    }

    Panel(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(cu.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text(listOfNotNull(cu.phone, cu.address, cu.commune, cu.wilaya).joinToString(" · ").ifBlank { "—" }, color = Palette.Muted, fontSize = 13.sp)
            }
            OutlinedButton(onClick = onEdit) { Text("Modifier") }
        }
        Spacer(Modifier.height(14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            KpiCard("Crédit actuel", Fmt.money(cu.current_credit), accent = if (cu.current_credit > 0) Palette.Warning else Palette.Success, modifier = Modifier.weight(1f))
            KpiCard("Plafond", if (cu.credit_limit > 0) Fmt.money(cu.credit_limit) else "Illimité", accent = Palette.Muted, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(14.dp))
        Text("Encaisser un remboursement", fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field("Montant (DA)", amount, { amount = it }, Modifier.weight(1f))
            Button(enabled = amount.toDoubleOrZero() > 0 && cu.current_credit > 0, onClick = { confirmPay = true }) { Text("Encaisser") }
        }
        Spacer(Modifier.height(14.dp))
        Text("Historique du crédit", fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            when {
                history == null -> CircularProgressIndicator(Modifier.size(22.dp))
                history!!.isEmpty() -> EmptyState("Aucune opération")
                else -> history!!.forEach { h ->
                    val isPay = h.transaction_type == "PAYMENT"
                    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
                        Column(Modifier.weight(1f)) {
                            Text(if (isPay) "Remboursement" else (h.invoice_number ?: "Vente à crédit"), fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                            Text(h.items_summary ?: "", color = Palette.Muted, fontSize = 12.sp, maxLines = 2)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text((if (isPay) "− " else "+ ") + Fmt.money(h.credit_amount), color = if (isPay) Palette.Success else Palette.Warning, fontWeight = FontWeight.Bold)
                            Text(Fmt.dateTime(h.created_at), color = Palette.Subtle, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
        TextButton(onClick = { confirmDelete = true }) { Text("Supprimer le client", color = Palette.Error) }
    }

    if (confirmPay) {
        val v = amount.toDoubleOrZero()
        ConfirmDialog("Encaisser ${Fmt.money(v)} ?", "Nouveau crédit de ${cu.name} : ${Fmt.money((cu.current_credit - v).coerceAtLeast(0.0))}", "Encaisser",
            onConfirm = { c.act({ registerPayment(cu.id, v) }, onDone = { amount = "" }) }, onDismiss = { confirmPay = false })
    }
    if (confirmDelete) {
        ConfirmDialog("Supprimer ${cu.name} ?", "Le client et son historique seront supprimés sur tous les appareils.", "Supprimer", destructive = true,
            onConfirm = { c.act({ deleteCustomer(cu.id) }, onDone = onDeleted) }, onDismiss = { confirmDelete = false })
    }
}

@Composable
private fun CustomerDialog(c: MasterController, cu: Customer, isNew: Boolean, onClose: () -> Unit) {
    var name by remember { mutableStateOf(cu.name) }
    var phone by remember { mutableStateOf(cu.phone ?: "") }
    var address by remember { mutableStateOf(cu.address ?: "") }
    var wilaya by remember { mutableStateOf(cu.wilaya ?: "") }
    var commune by remember { mutableStateOf(cu.commune ?: "") }
    var limit by remember { mutableStateOf(if (cu.credit_limit > 0) cu.credit_limit.toString() else "") }
    var type by remember { mutableStateOf(cu.customer_type ?: "RETAIL") }
    var notes by remember { mutableStateOf(cu.notes ?: "") }
    val types = listOf("RETAIL", "WHOLESALE", "SHOP", "RESTAURANT", "CAFETERIA", "OTHER")

    FormDialog(
        title = if (isNew) "Nouveau client" else "Modifier ${cu.name}",
        confirmEnabled = name.isNotBlank(),
        onConfirm = {
            c.act({
                saveCustomer(cu.copy(
                    name = name.trim(), phone = phone.ifBlank { null }, address = address.ifBlank { null }, wilaya = wilaya.ifBlank { null },
                    commune = commune.ifBlank { null }, credit_limit = limit.toDoubleOrZero(), customer_type = type, notes = notes.ifBlank { null }
                ), isNew)
            }, onDone = onClose)
        },
        onDismiss = onClose
    ) {
        Field("Nom", name, { name = it })
        Field("Téléphone", phone, { phone = it })
        Field("Adresse", address, { address = it })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field("Wilaya", wilaya, { wilaya = it }, Modifier.weight(1f))
            Field("Commune", commune, { commune = it }, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            Field("Plafond de crédit (vide = illimité)", limit, { limit = it }, Modifier.weight(1f))
            SelectField("Type", types, type, { it }, { type = it ?: "RETAIL" }, Modifier.weight(1f))
        }
        Field("Notes", notes, { notes = it }, singleLine = false)
    }
}

// ============================================================ STAFF (drivers)
@Composable
fun StaffScreen(c: MasterController) {
    var showCreate by remember { mutableStateOf(false) }
    val staff = c.profiles.sortedWith(compareBy({ it.role != "ADMIN" }, { it.displayName }))

    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Livreurs & équipe", "Les comptes sont créés ici, sur le poste principal uniquement") {
            Button(onClick = { showCreate = true }) { Text("+ Nouveau livreur") }
        }
        Panel(Modifier.fillMaxSize(), padding = Dp4.None) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                HeaderCell("Nom", 1.6f); HeaderCell("Rôle", 0.8f); HeaderCell("Livraisons actives", 1f); HeaderCell("Livrées", 0.8f)
                HeaderCell("Téléphone(s)", 1.4f); HeaderCell("Compte", 1f)
            }
            HorizontalDivider(color = Palette.Border)
            LazyColumn {
                items(staff, key = { it.id }) { p ->
                    val active = c.deliveries.count { it.order.delivery_employee_id == p.id && it.order.status in DeliveryStatus.active }
                    val done = c.deliveries.count { it.order.delivery_employee_id == p.id && it.order.status == "DELIVERED" }
                    val phones = c.devices.filter { it.user_id == p.id && it.platform == "ANDROID" }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Cell(p.displayName, 1.6f, bold = true)
                        Cell(if (p.role == "DELIVERY") "Livreur" else if (p.role == "ADMIN") "Admin" else p.role, 0.8f, color = Palette.Muted)
                        Cell("$active", 1f)
                        Cell("$done", 0.8f)
                        Cell(phones.joinToString { "${it.device_name ?: "Android"} (${statusLabel(it.status)})" }.ifBlank { "—" }, 1.4f, color = Palette.Muted)
                        Box(Modifier.weight(1f)) {
                            if (p.role == "ADMIN") StatusChip("Admin", Palette.Primary)
                            else Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(p.is_active, { on -> c.act({ setProfileActive(p.id, on) }) })
                                Spacer(Modifier.width(6.dp))
                                Text(if (p.is_active) "Actif" else "Désactivé", fontSize = 13.sp, color = Palette.Muted)
                            }
                        }
                    }
                    HorizontalDivider(color = Palette.Border.copy(alpha = 0.6f))
                }
            }
        }
    }
    if (showCreate) CreateDriverDialog(c) { showCreate = false }
}

@Composable
private fun CreateDriverDialog(c: MasterController, onClose: () -> Unit) {
    var first by remember { mutableStateOf("") }
    var last by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    FormDialog(
        title = "Nouveau livreur",
        confirmLabel = if (busy) "Création…" else "Créer le compte",
        confirmEnabled = !busy && first.isNotBlank() && email.contains("@") && password.length >= 6,
        onConfirm = {
            busy = true
            scope.launch {
                try {
                    c.repo.createDriver(email.trim(), password, first.trim(), last.trim())
                    c.refreshAll(); onClose()
                } catch (e: Exception) {
                    c.error = c.friendly(e)
                } finally { busy = false }
            }
        },
        onDismiss = onClose
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Field("Prénom", first, { first = it }, Modifier.weight(1f))
            Field("Nom", last, { last = it }, Modifier.weight(1f))
        }
        Field("Email de connexion", email, { email = it })
        OutlinedTextField(password, { password = it }, label = { Text("Mot de passe (6 caractères min.)") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
        Text("Le livreur se connecte avec ces identifiants sur son téléphone, puis vous approuvez le téléphone dans « Appareils Android ».",
            color = Palette.Subtle, fontSize = 12.sp)
    }
}

internal fun statusLabel(s: String) = when (s) {
    "APPROVED" -> "approuvé"
    "PENDING" -> "en attente"
    "REVOKED" -> "révoqué"
    else -> s
}
