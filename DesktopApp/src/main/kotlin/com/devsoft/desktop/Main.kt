package com.devsoft.desktop

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.*
import com.devsoft.desktop.data.DeviceIdentity
import com.devsoft.desktop.ui.*
import com.devsoft.desktop.ui.screens.*
import kotlinx.coroutines.launch

enum class Section(val label: String, val icon: ImageVector) {
    DASHBOARD("Tableau de bord", Icons.Filled.Dashboard),
    NEW_ORDER("Nouvelle commande", Icons.Filled.AddShoppingCart),
    DELIVERIES("Livraisons", Icons.Filled.LocalShipping),
    PRODUCTS("Produits", Icons.Filled.Inventory2),
    PURCHASES("Entrées de stock", Icons.Filled.MoveToInbox),
    CUSTOMERS("Clients & crédit", Icons.Filled.People),
    STAFF("Livreurs", Icons.Filled.Badge),
    DEVICES("Appareils Android", Icons.Filled.PhoneAndroid),
    SETTINGS("Paramètres", Icons.Filled.Settings)
}

fun main() = application {
    val scope = rememberCoroutineScope()
    val controller = remember { MasterController(scope).also { it.start() } }
    val logo = remember { BitmapPainter(useResource("app_logo.png") { loadImageBitmap(it) }) }
    var visible by remember { mutableStateOf(true) }
    val trayState = rememberTrayState()
    val windowState = rememberWindowState(size = DpSize(1360.dp, 860.dp), placement = WindowPlacement.Maximized)

    controller.notifier = { title, message ->
        trayState.sendNotification(Notification(title, message, Notification.Type.Info))
    }

    // The master keeps running in the tray so phones keep seeing the PC online.
    Tray(
        state = trayState,
        icon = logo,
        tooltip = "FreshFood - Poste principal",
        onAction = { visible = true },
        menu = {
            Item("Ouvrir le poste principal", onClick = { visible = true })
            Item("Quitter", onClick = ::exitApplication)
        }
    )

    if (visible) {
        Window(
            onCloseRequest = { visible = false },
            title = "FreshFood — Poste principal",
            icon = logo,
            state = windowState
        ) {
            window.minimumSize = java.awt.Dimension(1100, 700)
            MasterTheme {
                Surface(Modifier.fillMaxSize(), color = Palette.Background) {
                    when (val p = controller.phase) {
                        Phase.Starting -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                        Phase.SignedOut -> LoginScreen(controller, logo)
                        is Phase.NotMaster -> NotMasterScreen(controller, p)
                        Phase.Master -> Shell(controller, logo)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginScreen(controller: MasterController, logo: BitmapPainter) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var err by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submit() {
        if (busy || email.isBlank() || password.isBlank()) return
        busy = true; err = null
        scope.launch {
            err = controller.login(email, password)
            busy = false
        }
    }

    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Panel(Modifier.width(420.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(logo, null, Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)))
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("FreshFood", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("Poste principal Windows", color = Palette.Muted)
                }
            }
            Spacer(Modifier.height(24.dp))
            OutlinedTextField(email, { email = it }, label = { Text("Email administrateur") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                password, { password = it }, label = { Text("Mot de passe") }, singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                modifier = Modifier.fillMaxWidth()
            )
            if (err != null) {
                Spacer(Modifier.height(10.dp))
                Text(err!!, color = Palette.Error, fontSize = 13.sp)
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = ::submit, enabled = !busy, modifier = Modifier.fillMaxWidth().height(46.dp)) {
                if (busy) CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                else Text("Se connecter")
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "Ce PC pilote l'entreprise. Les téléphones Android sont des extensions qui doivent être approuvées ici.",
                color = Palette.Subtle, fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun NotMasterScreen(controller: MasterController, p: Phase.NotMaster) {
    var confirm by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Panel(Modifier.width(560.dp)) {
            Icon(Icons.Filled.DesktopAccessDisabled, null, tint = Palette.Warning, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(10.dp))
            Text("Ce PC n'est pas le poste principal", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(p.reason, color = Palette.Muted)
            controller.error?.let {
                Spacer(Modifier.height(8.dp)); Text(it, color = Palette.Error, fontSize = 13.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text("Identifiant de ce PC : ${DeviceIdentity.deviceId}", color = Palette.Subtle, fontSize = 12.sp)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (p.canClaim) Button(onClick = { confirm = true }) { Text("Faire de ce PC le poste principal") }
                OutlinedButton(onClick = { controller.logout() }) { Text("Se déconnecter") }
            }
        }
    }
    if (confirm) {
        ConfirmDialog(
            title = "Transférer le poste principal ?",
            message = "L'ancien PC perdra le contrôle des appareils Android. Continuez uniquement si ce PC remplace l'ancien.",
            confirmLabel = "Transférer",
            onConfirm = { controller.claimMaster() },
            onDismiss = { confirm = false }
        )
    }
}

@Composable
private fun Shell(controller: MasterController, logo: BitmapPainter) {
    var section by remember { mutableStateOf(Section.DASHBOARD) }
    Row(Modifier.fillMaxSize()) {
        // ---------- sidebar
        Column(Modifier.width(250.dp).fillMaxHeight().background(Palette.Sidebar).padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(6.dp)) {
                Image(logo, null, Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(controller.settings.brand_name ?: "FreshFood", color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
                    Text("Poste principal", color = Palette.Subtle, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(18.dp))
            Section.entries.forEach { s ->
                val selected = s == section
                val badge = when (s) {
                    Section.DEVICES -> controller.pendingDevices
                    Section.DELIVERIES -> controller.deliveries.count { it.order.status == "PENDING" }
                    else -> 0
                }
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp))
                        .background(if (selected) Palette.Primary else Color.Transparent)
                        .clickable { section = s }
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(s.icon, null, tint = if (selected) Color.White else Palette.Subtle, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(s.label, color = if (selected) Color.White else Color(0xFFCBD5E1), fontSize = 14.sp, modifier = Modifier.weight(1f))
                    if (badge > 0) {
                        Box(Modifier.clip(CircleShape).background(Palette.Warning).padding(horizontal = 7.dp, vertical = 1.dp)) {
                            Text("$badge", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
            }
            Spacer(Modifier.weight(1f))
            // connection + user
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(6.dp)) {
                Box(Modifier.size(9.dp).clip(CircleShape).background(if (controller.online) Palette.Success else Palette.Error))
                Spacer(Modifier.width(8.dp))
                Text(
                    if (controller.online) "En ligne · synchro ${controller.lastSync?.let { Fmt.time(java.time.Instant.ofEpochMilli(it).toString()) } ?: "…"}"
                    else "Hors ligne",
                    color = Palette.Subtle, fontSize = 12.sp
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(6.dp)) {
                Column(Modifier.weight(1f)) {
                    Text(controller.me?.displayName ?: "Admin", color = Color.White, fontSize = 13.sp, maxLines = 1)
                    Text("Administrateur", color = Palette.Subtle, fontSize = 11.sp)
                }
                IconButton(onClick = { controller.logout() }) {
                    Icon(Icons.Filled.Logout, "Déconnexion", tint = Palette.Subtle)
                }
            }
        }

        // ---------- content
        Column(Modifier.weight(1f).fillMaxHeight().padding(24.dp)) {
            ErrorBanner(controller.error) { controller.error = null }
            when (section) {
                Section.DASHBOARD -> DashboardScreen(controller) { section = it }
                Section.NEW_ORDER -> NewOrderScreen(controller) { section = Section.DELIVERIES }
                Section.DELIVERIES -> DeliveriesScreen(controller)
                Section.PRODUCTS -> ProductsScreen(controller)
                Section.PURCHASES -> PurchasesScreen(controller)
                Section.CUSTOMERS -> CustomersScreen(controller)
                Section.STAFF -> StaffScreen(controller)
                Section.DEVICES -> DevicesScreen(controller)
                Section.SETTINGS -> SettingsScreen(controller)
            }
        }
    }
}
