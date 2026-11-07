package com.devsoft.desktop

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.devsoft.desktop.data.*
import com.devsoft.desktop.ui.Fmt
import kotlinx.coroutines.*

sealed class Phase {
    data object Starting : Phase()
    data object SignedOut : Phase()
    /** Signed in but this PC cannot act as master (not admin, or another PC holds the role). */
    data class NotMaster(val reason: String, val canClaim: Boolean) : Phase()
    data object Master : Phase()
}

/**
 * Central state of the master station. Holds the shared data, runs the heartbeat that tells
 * the phones the PC is online, and polls Supabase so the PC sees what drivers do in real time.
 */
class MasterController(private val scope: CoroutineScope) {
    val repo = Repository()

    var phase by mutableStateOf<Phase>(Phase.Starting); private set
    var me by mutableStateOf<Profile?>(null); private set
    var registration by mutableStateOf<DeviceRegistration?>(null); private set
    var settings by mutableStateOf(AppSetting()); private set

    var products by mutableStateOf<List<Product>>(emptyList()); private set
    var customers by mutableStateOf<List<Customer>>(emptyList()); private set
    var profiles by mutableStateOf<List<Profile>>(emptyList()); private set
    var deliveries by mutableStateOf<List<DeliveryOrderWithDetails>>(emptyList()); private set
    var sales by mutableStateOf<List<Sale>>(emptyList()); private set
    var devices by mutableStateOf<List<Device>>(emptyList()); private set

    var online by mutableStateOf(true); private set
    var lastSync by mutableStateOf<Long?>(null); private set
    var error by mutableStateOf<String?>(null)

    /** Desktop notification sink (set by the tray). */
    var notifier: (title: String, message: String) -> Unit = { _, _ -> }

    val drivers get() = profiles.filter { it.role == "DELIVERY" }
    val pendingDevices get() = devices.count { it.status == "PENDING" }

    private var loops: Job? = null
    private var knownStatuses: Map<String, String>? = null
    private var knownPendingDevices: Set<String>? = null

    fun start() {
        scope.launch {
            val uid = runCatching { repo.restoreSessionUserId() }.getOrNull()
            if (uid == null) phase = Phase.SignedOut else enterAfterLogin(uid)
        }
    }

    suspend fun login(email: String, password: String): String? {
        return try {
            repo.login(email.trim(), password)
            val uid = repo.currentUserId() ?: return "Connexion impossible"
            enterAfterLogin(uid)
            null
        } catch (e: Exception) {
            friendly(e)
        }
    }

    fun logout() {
        loops?.cancel()
        scope.launch {
            repo.logout()
            me = null; registration = null; knownStatuses = null; knownPendingDevices = null
            phase = Phase.SignedOut
        }
    }

    private suspend fun enterAfterLogin(uid: String) {
        val profile = runCatching { repo.getProfile(uid) }.getOrNull()
        me = profile
        if (profile?.role != "ADMIN") {
            phase = Phase.NotMaster("Seul un administrateur peut utiliser le poste principal.", canClaim = false)
            return
        }
        val reg = try {
            repo.registerThisStation()
        } catch (e: Exception) {
            phase = Phase.NotMaster(
                "Enregistrement du poste impossible : ${friendly(e)}\n" +
                    "Vérifiez que le script master_extension_architecture.sql a été exécuté dans Supabase.",
                canClaim = false
            )
            return
        }
        registration = reg
        if (reg.is_master) {
            phase = Phase.Master
            startLoops()
        } else {
            phase = Phase.NotMaster(
                "Un autre PC est déjà le poste principal de l'entreprise.\n" +
                    "Vous pouvez transférer le rôle de poste principal sur ce PC (par exemple si l'ancien PC a été remplacé).",
                canClaim = true
            )
        }
    }

    fun claimMaster() {
        scope.launch {
            try {
                repo.claimMaster()
                repo.currentUserId()?.let { enterAfterLogin(it) }
            } catch (e: Exception) {
                error = friendly(e)
            }
        }
    }

    private fun startLoops() {
        loops?.cancel()
        loops = scope.launch {
            launch { refreshAll() }
            // Heartbeat: phones show "PC principal en ligne" while this runs
            launch {
                while (isActive) {
                    delay(30_000)
                    online = runCatching { repo.heartbeat() }.isSuccess
                }
            }
            // Live pipeline: deliveries + devices every 10 s, the rest every 60 s
            launch {
                var tick = 0
                while (isActive) {
                    delay(10_000)
                    tick++
                    refreshLive()
                    if (tick % 6 == 0) refreshReference()
                }
            }
        }
    }

    fun refreshAll() {
        scope.launch {
            refreshReference()
            refreshLive()
        }
    }

    private suspend fun refreshReference() {
        runCatching { repo.getSettings() }.onSuccess { settings = it }
        runCatching { repo.getProducts() }.onSuccess { products = it }
        runCatching { repo.getCustomers() }.onSuccess { customers = it }
        runCatching { repo.getProfiles() }.onSuccess { profiles = it }
        runCatching { repo.getSales() }.onSuccess { sales = it }
    }

    private suspend fun refreshLive() {
        val d = runCatching { repo.getDeliveries() }
        val dev = runCatching { repo.getDevices() }
        online = d.isSuccess
        d.onSuccess { list ->
            notifyDeliveryChanges(list)
            deliveries = list
            lastSync = System.currentTimeMillis()
        }
        dev.onSuccess { list ->
            notifyNewDevices(list)
            devices = list
        }
    }

    private fun notifyDeliveryChanges(list: List<DeliveryOrderWithDetails>) {
        val now = list.associate { it.order.id to it.order.status }
        val before = knownStatuses
        if (before != null) {
            list.forEach { d ->
                val old = before[d.order.id]
                if (old != null && old != d.order.status) {
                    val who = d.driver?.displayName ?: "Livreur"
                    val client = d.customer?.name ?: "client"
                    when (d.order.status) {
                        "DELIVERED" -> notifier("Livraison terminée ✅", "$who a livré ${Fmt.shortId(d.order.id)} chez $client")
                        "OUT_FOR_DELIVERY" -> notifier("Livraison en route 🚚", "$who est parti chez $client")
                        "CANCELLED" -> notifier("Livraison annulée", "${Fmt.shortId(d.order.id)} pour $client")
                    }
                }
            }
        }
        knownStatuses = now
    }

    private fun notifyNewDevices(list: List<Device>) {
        val pending = list.filter { it.status == "PENDING" }.map { it.device_id }.toSet()
        val before = knownPendingDevices
        if (before != null) {
            (pending - before).forEach { id ->
                val dev = list.first { it.device_id == id }
                notifier("Nouvel appareil Android 📱", "${dev.device_name ?: "Téléphone"} demande l'accès. Ouvrez « Appareils » pour l'approuver.")
            }
        }
        knownPendingDevices = pending
    }

    /** Runs a write, reports errors in the banner, then refreshes the data. */
    fun act(block: suspend Repository.() -> Unit, onDone: () -> Unit = {}) {
        scope.launch {
            try {
                repo.block()
                onDone()
            } catch (e: Exception) {
                error = friendly(e)
            }
            refreshReference()
            refreshLive()
        }
    }

    fun friendly(e: Throwable): String {
        val msg = e.message ?: e.toString()
        return when {
            msg.contains("Invalid login credentials", true) -> "Email ou mot de passe incorrect."
            msg.contains("UnknownHost", true) || msg.contains("Unable to resolve", true) ||
                msg.contains("ConnectException", true) -> "Pas de connexion Internet."
            else -> msg.lineSequence().firstOrNull { it.isNotBlank() }?.take(300) ?: msg
        }
    }
}
