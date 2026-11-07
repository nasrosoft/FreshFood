package com.devsoft.desktop.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.devsoft.desktop.MasterController
import com.devsoft.desktop.data.Device
import com.devsoft.desktop.data.DeviceIdentity
import com.devsoft.desktop.data.ExtensionModule
import com.devsoft.desktop.ui.*

// ============================================================ DEVICES (master control of the Android extensions)
@Composable
fun DevicesScreen(c: MasterController) {
    var editing by remember { mutableStateOf<Device?>(null) }
    var toDelete by remember { mutableStateOf<Device?>(null) }
    val users = c.profiles.associateBy { it.id }
    val sorted = c.devices.sortedWith(compareBy({ it.device_id != DeviceIdentity.deviceId }, { it.status != "PENDING" }, { it.device_name ?: "" }))

    Column(Modifier.fillMaxSize()) {
        ScreenHeader(
            "Appareils Android",
            "Les téléphones sont des extensions de ce PC : ils n'ouvrent que les modules que vous leur accordez"
        ) {
            OutlinedButton(onClick = { c.refreshAll() }) { Text("Actualiser") }
        }
        if (c.pendingDevices > 0) {
            Panel(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text("📱 ${c.pendingDevices} téléphone(s) attendent votre approbation.", color = Palette.Warning, fontWeight = FontWeight.SemiBold)
            }
        }
        Panel(Modifier.fillMaxSize(), padding = Dp4.None) {
            Row(Modifier.padding(horizontal = 18.dp, vertical = 12.dp)) {
                HeaderCell("Appareil", 1.6f); HeaderCell("Utilisateur", 1.2f); HeaderCell("Modules accordés", 2f)
                HeaderCell("Vu", 0.9f); HeaderCell("Statut", 0.9f); HeaderCell("Actions", 1.6f)
            }
            HorizontalDivider(color = Palette.Border)
            if (sorted.isEmpty()) EmptyState("Aucun appareil. Les téléphones apparaissent ici après la première connexion d'un utilisateur.")
            LazyColumn {
                items(sorted, key = { it.id }) { d ->
                    val isMe = d.device_id == DeviceIdentity.deviceId
                    Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Row(Modifier.weight(1.6f), verticalAlignment = Alignment.CenterVertically) {
                            Icon(if (d.platform == "WINDOWS") Icons.Filled.Computer else Icons.Filled.PhoneAndroid, null, tint = Palette.Muted)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text((d.device_name ?: d.platform) + if (isMe) " (ce PC)" else "", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                // Same 8-char code the phone shows on its "waiting for approval" screen
                                Text("Code ${d.device_id.take(8).uppercase()} · v${d.app_version ?: "?"}", color = Palette.Subtle, fontSize = 11.sp)
                            }
                        }
                        Cell(d.user_id?.let { users[it]?.displayName } ?: "—", 1.2f)
                        Cell(
                            if (isMe) "Poste principal — accès complet"
                            else d.allowed_modules.mapNotNull { k -> ExtensionModule.entries.firstOrNull { it.key == k }?.label }.joinToString(", ").ifBlank { "Aucun" },
                            2f, color = Palette.Muted
                        )
                        Cell(Fmt.ago(d.last_seen_at), 0.9f, color = Palette.Muted)
                        Box(Modifier.weight(0.9f)) {
                            when {
                                isMe -> StatusChip("Maître", Palette.Primary)
                                d.status == "APPROVED" -> StatusChip("Approuvé", Palette.Success)
                                d.status == "PENDING" -> StatusChip("En attente", Palette.Warning)
                                else -> StatusChip("Révoqué", Palette.Error)
                            }
                        }
                        Row(Modifier.weight(1.6f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (!isMe) {
                                when (d.status) {
                                    "PENDING" -> {
                                        Button(onClick = { c.act({ setDeviceAccess(d.device_id, "APPROVED", d.allowed_modules.ifEmpty { c.settings.default_extension_modules }) }) }) { Text("Approuver") }
                                        OutlinedButton(onClick = { c.act({ setDeviceAccess(d.device_id, "REVOKED", d.allowed_modules) }) }) { Text("Refuser") }
                                    }
                                    "APPROVED" -> {
                                        OutlinedButton(onClick = { editing = d }) { Text("Modules") }
                                        OutlinedButton(onClick = { c.act({ setDeviceAccess(d.device_id, "REVOKED", d.allowed_modules) }) }) { Text("Révoquer", color = Palette.Error) }
                                    }
                                    else -> {
                                        OutlinedButton(onClick = { c.act({ setDeviceAccess(d.device_id, "APPROVED", d.allowed_modules) }) }) { Text("Réactiver") }
                                        TextButton(onClick = { toDelete = d }) { Text("Oublier", color = Palette.Error) }
                                    }
                                }
                            }
                        }
                    }
                    HorizontalDivider(color = Palette.Border.copy(alpha = 0.6f))
                }
            }
        }
    }

    editing?.let { d -> ModulesDialog("Modules de ${d.device_name ?: "l'appareil"}", d.allowed_modules, onSave = { mods ->
        c.act({ setDeviceAccess(d.device_id, d.status, mods) })
    }) { editing = null } }
    toDelete?.let { d ->
        ConfirmDialog("Oublier cet appareil ?", "S'il se reconnecte, il réapparaîtra « en attente ».", "Oublier", destructive = true,
            onConfirm = { c.act({ deleteDevice(d.device_id) }) }, onDismiss = { toDelete = null })
    }
}

@Composable
private fun ModulesDialog(title: String, initial: List<String>, onSave: (List<String>) -> Unit, onClose: () -> Unit) {
    val chosen = remember { mutableStateListOf<String>().apply { addAll(initial) } }
    FormDialog(title = title, confirmEnabled = chosen.isNotEmpty(), onConfirm = { onSave(chosen.toList()); onClose() }, onDismiss = onClose) {
        Text("Cochez ce que ce téléphone peut ouvrir. Utilisateurs, paramètres et appareils restent réservés au PC.", color = Palette.Muted, fontSize = 13.sp)
        ModuleChecklist(chosen)
    }
}

@Composable
private fun ModuleChecklist(chosen: MutableList<String>) {
    ExtensionModule.entries.forEach { m ->
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(m.key in chosen, { on -> if (on) chosen += m.key else chosen -= m.key })
            Text(m.label)
        }
    }
}

// ============================================================ SETTINGS
@Composable
fun SettingsScreen(c: MasterController) {
    val s = c.settings
    var brand by remember(s.brand_name) { mutableStateOf(s.brand_name ?: "") }
    var tagline by remember(s.brand_tagline) { mutableStateOf(s.brand_tagline ?: "") }
    var autoApprove by remember(s.auto_approve_extensions) { mutableStateOf(s.auto_approve_extensions == 1) }
    val defaults = remember(s.default_extension_modules) { mutableStateListOf<String>().apply { addAll(s.default_extension_modules) } }
    var confirmLock by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        ScreenHeader("Paramètres", "Réglages globaux appliqués à tous les appareils")

        Panel(Modifier.fillMaxWidth()) {
            Text("Identité de la marque (Paramétrage)", fontWeight = FontWeight.Bold)
            Text("Affichée dans les menus et les reçus PDF des téléphones.", color = Palette.Muted, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Field("Nom commercial", brand, { brand = it }, Modifier.weight(1f))
                Field("Slogan / description", tagline, { tagline = it }, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Button(enabled = brand.isNotBlank(), onClick = { c.act({ updateBrand(brand.trim(), tagline.trim()) }) }) { Text("Enregistrer") }
        }

        Panel(Modifier.fillMaxWidth()) {
            Text("Politique des extensions Android", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(autoApprove, { autoApprove = it })
                Spacer(Modifier.width(10.dp))
                Text(if (autoApprove) "Les nouveaux téléphones sont approuvés automatiquement" else "Chaque nouveau téléphone doit être approuvé sur ce PC (recommandé)")
            }
            Spacer(Modifier.height(10.dp))
            Text("Modules accordés par défaut à un nouveau téléphone :", fontSize = 13.sp, color = Palette.Muted)
            ModuleChecklist(defaults)
            Spacer(Modifier.height(10.dp))
            Button(enabled = defaults.isNotEmpty(), onClick = { c.act({ setExtensionPolicy(autoApprove, defaults.toList()) }) }) { Text("Enregistrer la politique") }
        }

        Panel(Modifier.fillMaxWidth()) {
            Text("Poste principal", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Text("Identifiant : ${DeviceIdentity.deviceId}", fontSize = 13.sp)
            Text("Nom : ${DeviceIdentity.deviceName}", fontSize = 13.sp)
            Text("Dernier signal envoyé aux téléphones : ${Fmt.ago(s.master_heartbeat_at)}", fontSize = 13.sp, color = Palette.Muted)
            Text("Laissez l'application ouverte (ou réduite dans la barre des tâches) pour que les téléphones voient le PC en ligne.", color = Palette.Subtle, fontSize = 12.sp)
        }

        Panel(Modifier.fillMaxWidth()) {
            Text("Activation de l'application", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusChip(if (s.app_enabled == 1) "Active" else "Bloquée", if (s.app_enabled == 1) Palette.Success else Palette.Error)
                Spacer(Modifier.width(12.dp))
                if (s.app_enabled == 1) OutlinedButton(onClick = { confirmLock = true }) { Text("Bloquer tous les téléphones", color = Palette.Error) }
                else Button(onClick = { c.act({ setAppEnabled(true) }) }) { Text("Réactiver") }
            }
        }
    }

    if (confirmLock) {
        ConfirmDialog("Bloquer tous les téléphones ?", "Les téléphones afficheront l'écran « Paiement requis » jusqu'à la réactivation.", "Bloquer",
            destructive = true, onConfirm = { c.act({ setAppEnabled(false) }) }, onDismiss = { confirmLock = false })
    }
}
