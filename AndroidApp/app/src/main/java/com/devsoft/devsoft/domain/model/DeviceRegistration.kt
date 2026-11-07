package com.devsoft.devsoft.domain.model

import kotlinx.serialization.Serializable

/**
 * Answer of the `register_device` RPC (master_extension_architecture.sql).
 * The Windows desktop app is the master station; this phone is an extension whose
 * access and visible modules are decided by the master.
 */
@Serializable
data class DeviceRegistration(
    val device_id: String,
    val status: String,                       // PENDING | APPROVED | REVOKED
    val allowed_modules: List<String> = emptyList(),
    val is_master: Boolean = false,
    val master_configured: Boolean = false,
    val master_heartbeat_at: String? = null,
    val master_online: Boolean = false,
    val app_enabled: Boolean = true
)
