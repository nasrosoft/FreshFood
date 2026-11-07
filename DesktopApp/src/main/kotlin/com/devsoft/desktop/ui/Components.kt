package com.devsoft.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Same palette as the Android app (ui/theme/Color.kt)
object Palette {
    val Primary = Color(0xFF2563EB)
    val PrimaryDark = Color(0xFF1D4ED8)
    val PrimaryContainer = Color(0xFFEFF6FF)
    val OnPrimaryContainer = Color(0xFF1E40AF)
    val Background = Color(0xFFF8FAFC)
    val Surface = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF1F5F9)
    val Border = Color(0xFFE2E8F0)
    val Text = Color(0xFF0F172A)
    val Muted = Color(0xFF64748B)
    val Subtle = Color(0xFF94A3B8)
    val Success = Color(0xFF10B981)
    val Warning = Color(0xFFF59E0B)
    val Error = Color(0xFFEF4444)
    val Info = Color(0xFF3B82F6)
    val Sidebar = Color(0xFF0F172A)
}

private val LightColors = lightColorScheme(
    primary = Palette.Primary,
    onPrimary = Color.White,
    primaryContainer = Palette.PrimaryContainer,
    onPrimaryContainer = Palette.OnPrimaryContainer,
    background = Palette.Background,
    surface = Palette.Surface,
    surfaceVariant = Palette.SurfaceVariant,
    onSurface = Palette.Text,
    onSurfaceVariant = Palette.Muted,
    outline = Palette.Border,
    error = Palette.Error
)

@Composable
fun MasterTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = LightColors, content = content)
}

object Fmt {
    private val dateTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
    private val time = DateTimeFormatter.ofPattern("HH:mm")

    fun money(v: Double): String = String.format(Locale.FRANCE, "%,.2f DA", v)

    fun parse(ts: String?): OffsetDateTime? {
        if (ts.isNullOrBlank()) return null
        return runCatching { OffsetDateTime.parse(ts) }.getOrNull()
            ?: runCatching { Instant.parse(ts).atZone(ZoneId.systemDefault()).toOffsetDateTime() }.getOrNull()
            ?: runCatching { OffsetDateTime.parse(ts.replace(" ", "T") + if (ts.contains('+')) "" else "Z") }.getOrNull()
    }

    fun dateTime(ts: String?): String =
        parse(ts)?.atZoneSameInstant(ZoneId.systemDefault())?.format(dateTime) ?: "-"

    fun time(ts: String?): String =
        parse(ts)?.atZoneSameInstant(ZoneId.systemDefault())?.format(time) ?: "-"

    /** "il y a 3 min" style relative time. */
    fun ago(ts: String?): String {
        val t = parse(ts) ?: return "jamais"
        val s = (System.currentTimeMillis() - t.toInstant().toEpochMilli()) / 1000
        return when {
            s < 60 -> "à l'instant"
            s < 3600 -> "il y a ${s / 60} min"
            s < 86400 -> "il y a ${s / 3600} h"
            else -> "il y a ${s / 86400} j"
        }
    }

    fun shortId(id: String) = "#" + id.take(8).uppercase()
}

object DeliveryStatus {
    fun label(s: String) = when (s.uppercase()) {
        "PENDING" -> "En attente"
        "ASSIGNED" -> "Assignée"
        "OUT_FOR_DELIVERY" -> "En route"
        "DELIVERED" -> "Livrée"
        "PARTIALLY_DELIVERED" -> "Partielle"
        "CANCELLED" -> "Annulée"
        else -> s
    }

    fun color(s: String) = when (s.uppercase()) {
        "PENDING" -> Palette.Warning
        "ASSIGNED" -> Palette.Info
        "OUT_FOR_DELIVERY" -> Palette.Primary
        "DELIVERED" -> Palette.Success
        "CANCELLED" -> Palette.Error
        else -> Palette.Muted
    }

    val active = setOf("PENDING", "ASSIGNED", "OUT_FOR_DELIVERY")
}

@Composable
fun StatusChip(text: String, color: Color) {
    Box(
        Modifier.clip(RoundedCornerShape(50)).background(color.copy(alpha = 0.12f)).padding(horizontal = 10.dp, vertical = 3.dp)
    ) {
        Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun Panel(modifier: Modifier = Modifier, padding: Dp4 = Dp4.Normal, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Palette.Surface)
            .border(1.dp, Palette.Border, RoundedCornerShape(14.dp))
            .padding(if (padding == Dp4.Normal) 18.dp else 0.dp),
        content = content
    )
}

enum class Dp4 { Normal, None }

@Composable
fun ScreenHeader(title: String, subtitle: String? = null, actions: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(bottom = 18.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Palette.Text)
            if (subtitle != null) Text(subtitle, color = Palette.Muted, fontSize = 14.sp)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically, content = actions)
    }
}

@Composable
fun KpiCard(title: String, value: String, hint: String? = null, accent: Color = Palette.Primary, modifier: Modifier = Modifier) {
    Panel(modifier) {
        Text(title, color = Palette.Muted, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = accent)
        if (hint != null) {
            Spacer(Modifier.height(4.dp))
            Text(hint, color = Palette.Subtle, fontSize = 12.sp)
        }
    }
}

@Composable
fun EmptyState(text: String) {
    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
        Text(text, color = Palette.Muted)
    }
}

@Composable
fun ErrorBanner(message: String?, onDismiss: () -> Unit) {
    if (message == null) return
    Row(
        Modifier.fillMaxWidth().padding(bottom = 12.dp).clip(RoundedCornerShape(10.dp))
            .background(Palette.Error.copy(alpha = 0.1f)).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(message, color = Palette.Error, modifier = Modifier.weight(1f), fontSize = 13.sp)
        TextButton(onClick = onDismiss) { Text("Fermer") }
    }
}

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "Confirmer",
    destructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = { onConfirm(); onDismiss() },
                colors = if (destructive) ButtonDefaults.buttonColors(containerColor = Palette.Error) else ButtonDefaults.buttonColors()
            ) { Text(confirmLabel) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}

/** Simple table header cell / row helpers. */
@Composable
fun RowScope.HeaderCell(text: String, weight: Float) {
    Text(text, Modifier.weight(weight), color = Palette.Muted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
fun RowScope.Cell(text: String, weight: Float, bold: Boolean = false, color: Color = Palette.Text) {
    Text(
        text, Modifier.weight(weight), color = color, fontSize = 14.sp,
        fontWeight = if (bold) FontWeight.SemiBold else FontWeight.Normal, maxLines = 2
    )
}

fun String.toDoubleOrZero() = replace(',', '.').trim().toDoubleOrNull() ?: 0.0
fun String.toIntOrZero() = trim().toIntOrNull() ?: 0
