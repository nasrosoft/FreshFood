package com.devsoft.desktop.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.SessionManager
import io.github.jan.supabase.auth.user.UserSession
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json
import java.io.File
import java.net.InetAddress
import java.util.Properties
import java.util.UUID

/** Local folder of the master station: %APPDATA%\FreshFoodMaster */
object AppPaths {
    val dataDir: File by lazy {
        val base = System.getenv("APPDATA") ?: System.getProperty("user.home")
        File(base, "FreshFoodMaster").apply { mkdirs() }
    }
    val sessionFile get() = File(dataDir, "session.json")
    val deviceFile get() = File(dataDir, "device.properties")
    val configFile get() = File(dataDir, "config.properties")
}

/**
 * Supabase project shared with the Android extensions.
 * Defaults are the same values compiled into the Android app; they can be overridden
 * with supabase.url / supabase.key in %APPDATA%\FreshFoodMaster\config.properties.
 */
object BackendConfig {
    private const val DEFAULT_URL = "https://rjlacsdehgxzsghdzbul.supabase.co"
    private const val DEFAULT_KEY = "sb_publishable_oln733bAmVovVJLPQ_BFvQ_AaEr1D0Q"
    const val APP_VERSION = "1.0.0"

    private val props: Properties by lazy {
        Properties().apply {
            val f = AppPaths.configFile
            if (f.exists()) f.inputStream().use { load(it) }
        }
    }
    val url: String get() = props.getProperty("supabase.url")?.trim()?.ifBlank { null } ?: DEFAULT_URL
    val key: String get() = props.getProperty("supabase.key")?.trim()?.ifBlank { null } ?: DEFAULT_KEY
}

/** Stable identity of this Windows install, used for the master role. */
object DeviceIdentity {
    val deviceId: String by lazy {
        val f = AppPaths.deviceFile
        val p = Properties()
        if (f.exists()) f.inputStream().use { p.load(it) }
        p.getProperty("device_id") ?: ("win-" + UUID.randomUUID().toString()).also {
            p.setProperty("device_id", it)
            f.outputStream().use { out -> p.store(out, "FreshFood master station identity - do not edit") }
        }
    }

    val deviceName: String by lazy {
        val host = runCatching { InetAddress.getLocalHost().hostName }.getOrNull()
            ?: System.getenv("COMPUTERNAME") ?: "PC"
        "PC $host"
    }
}

/** Persists the auth session in a file so the admin stays signed in across restarts. */
class FileSessionManager(private val file: File) : SessionManager {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    override suspend fun saveSession(session: UserSession) {
        file.writeText(json.encodeToString(UserSession.serializer(), session))
    }
    override suspend fun loadSession(): UserSession? =
        if (file.exists()) runCatching { json.decodeFromString(UserSession.serializer(), file.readText()) }.getOrNull() else null
    override suspend fun deleteSession() {
        file.delete()
    }
}

object Backend {
    val client: SupabaseClient by lazy {
        createSupabaseClient(supabaseUrl = BackendConfig.url, supabaseKey = BackendConfig.key) {
            install(Auth) {
                sessionManager = FileSessionManager(AppPaths.sessionFile)
                alwaysAutoRefresh = true
                autoLoadFromStorage = true
            }
            install(Postgrest)
            defaultSerializer = KotlinXSerializer(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
                explicitNulls = false
            })
        }
    }
}
