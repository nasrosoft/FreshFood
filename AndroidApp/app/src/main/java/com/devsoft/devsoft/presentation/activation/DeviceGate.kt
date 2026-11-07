package com.devsoft.devsoft.presentation.activation

import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.devsoft.devsoft.R
import com.devsoft.devsoft.domain.repository.ActivationRepository
import com.devsoft.devsoft.ui.theme.*
import com.devsoft.devsoft.utils.DeviceUtil
import kotlinx.coroutines.delay

/**
 * Access of this phone as decided by the master (Windows) station.
 * [modules] == null means "legacy mode": the backend has no master registry yet, so the app
 * falls back to the old role-based menus.
 */
sealed class DeviceAccess {
    object Checking : DeviceAccess()
    data class Approved(val modules: Set<String>?, val masterOnline: Boolean) : DeviceAccess()
    object Pending : DeviceAccess()
    object Revoked : DeviceAccess()
    data class Error(val message: String) : DeviceAccess()
}

private const val PREFS = "devsoft_device_prefs"
private const val KEY_STATUS = "master_access_status"
private const val KEY_MODULES = "master_access_modules"

private fun cachedAccess(context: Context): DeviceAccess? {
    val p = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    return when (p.getString(KEY_STATUS, null)) {
        "APPROVED" -> DeviceAccess.Approved(
            p.getString(KEY_MODULES, "")!!.split(',').filter { it.isNotBlank() }.toSet(),
            masterOnline = false
        )
        "REVOKED" -> DeviceAccess.Revoked
        else -> null
    }
}

private fun cacheAccess(context: Context, status: String, modules: List<String>) {
    context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        .putString(KEY_STATUS, status)
        .putString(KEY_MODULES, modules.joinToString(","))
        .apply()
}

/** The database migration has not been applied yet: keep working like before. */
private fun isMissingRegistry(e: Throwable): Boolean {
    val m = e.message ?: return false
    return m.contains("register_device") && (m.contains("Could not find", true) || m.contains("PGRST202") || m.contains("does not exist", true))
}

private suspend fun check(context: Context, repo: ActivationRepository, previous: DeviceAccess): DeviceAccess {
    val deviceName = "${Build.MANUFACTURER} ${Build.MODEL}".trim()
    val version = runCatching { context.packageManager.getPackageInfo(context.packageName, 0).versionName }.getOrNull() ?: "1.0"
    val result = repo.registerDevice(DeviceUtil.getDeviceId(context), deviceName, version ?: "1.0")
    return result.fold(
        onSuccess = { reg ->
            cacheAccess(context, reg.status, reg.allowed_modules)
            when (reg.status) {
                // No banner while no master PC has been installed yet
                "APPROVED" -> DeviceAccess.Approved(reg.allowed_modules.toSet(), reg.master_online || !reg.master_configured)
                "REVOKED" -> DeviceAccess.Revoked
                else -> DeviceAccess.Pending
            }
        },
        onFailure = { e ->
            when {
                isMissingRegistry(e) -> DeviceAccess.Approved(null, masterOnline = true)
                // Network blip: keep what we had so a driver on the road is not locked out
                previous is DeviceAccess.Approved -> previous.copy(masterOnline = false)
                previous is DeviceAccess.Revoked || previous is DeviceAccess.Pending -> previous
                else -> cachedAccess(context) ?: DeviceAccess.Error(e.message ?: "Network error")
            }
        }
    )
}

/**
 * Gate shown after login: the phone only opens once the master PC has approved it,
 * and only with the modules the master granted. Re-checked every 30 s and on resume,
 * so a revoke on the PC takes effect quickly.
 */
@Composable
fun DeviceGate(
    activationRepository: ActivationRepository,
    onLogout: () -> Unit,
    content: @Composable (allowedModules: Set<String>?, masterOnline: Boolean) -> Unit
) {
    val context = LocalContext.current
    var access by remember { mutableStateOf<DeviceAccess>(DeviceAccess.Checking) }
    var trigger by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) trigger++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LaunchedEffect(trigger) {
        while (true) {
            access = check(context, activationRepository, access)
            delay(if (access is DeviceAccess.Approved) 30_000L else 10_000L)
        }
    }

    when (val a = access) {
        is DeviceAccess.Checking -> GateMessage(
            title = null, message = stringResource(R.string.device_checking), showSpinner = true,
            onRetry = null, onLogout = null
        )
        is DeviceAccess.Approved -> content(a.modules, a.masterOnline)
        is DeviceAccess.Pending -> GateMessage(
            title = stringResource(R.string.device_waiting_title),
            message = stringResource(R.string.device_waiting_message),
            code = DeviceUtil.getDeviceId(context).take(8).uppercase(),
            showSpinner = true,
            onRetry = { trigger++ }, onLogout = onLogout
        )
        is DeviceAccess.Revoked -> GateMessage(
            title = stringResource(R.string.device_revoked_title),
            message = stringResource(R.string.device_revoked_message),
            code = DeviceUtil.getDeviceId(context).take(8).uppercase(),
            isError = true,
            onRetry = { trigger++ }, onLogout = onLogout
        )
        is DeviceAccess.Error -> GateMessage(
            title = stringResource(R.string.device_error_title),
            message = a.message,
            isError = true,
            onRetry = { trigger++ }, onLogout = onLogout
        )
    }
}

@Composable
private fun GateMessage(
    title: String?,
    message: String,
    code: String? = null,
    showSpinner: Boolean = false,
    isError: Boolean = false,
    onRetry: (() -> Unit)?,
    onLogout: (() -> Unit)?
) {
    Surface(modifier = Modifier.fillMaxSize(), color = AppBackground) {
        Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (isError) "⛔" else "🖥️ ➜ 📱", fontSize = 40.sp)
                Spacer(Modifier.height(16.dp))
                if (title != null) {
                    Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
                        color = if (isError) StatusError else TextDark)
                    Spacer(Modifier.height(8.dp))
                }
                Text(message, style = MaterialTheme.typography.bodyMedium, color = TextMuted, textAlign = TextAlign.Center)
                if (code != null) {
                    Spacer(Modifier.height(16.dp))
                    Box(Modifier.background(PrimaryBlueContainer, RoundedCornerShape(10.dp)).padding(horizontal = 16.dp, vertical = 8.dp)) {
                        Text(stringResource(R.string.device_code, code), fontWeight = FontWeight.Bold, color = OnPrimaryBlueContainer)
                    }
                }
                if (showSpinner) {
                    Spacer(Modifier.height(20.dp))
                    CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.height(24.dp))
                if (onRetry != null) Button(onClick = onRetry) { Text(stringResource(R.string.check_again)) }
                if (onLogout != null) TextButton(onClick = onLogout) { Text(stringResource(R.string.logout), color = StatusError) }
            }
        }
    }
}
