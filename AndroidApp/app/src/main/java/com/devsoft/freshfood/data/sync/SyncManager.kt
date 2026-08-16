package com.devsoft.freshfood.data.sync

import android.content.Context
import android.util.Log
import com.devsoft.freshfood.data.local.FreshFoodDatabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.flow.first

class SyncManager(
    private val localDb: FreshFoodDatabase,
    private val supabase: SupabaseClient,
    private val context: Context
) {
    private val syncMutex = Mutex()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun syncAll(): Boolean {
        if (syncMutex.isLocked) return false
        
        syncMutex.withLock {
            try {
                // 1. Push local changes
                pushLocalChanges()
                
                // 2. Pull remote changes
                pullRemoteChanges()

                return true
            } catch (e: Exception) {
                Log.e("SyncManager", "Sync failed: ${e.message}")
                return false
            }
        }
    }

    private suspend fun pushLocalChanges() {
        val pendingOps = localDb.syncQueueDao().getPendingOperations("PENDING")
        val failedOps = localDb.syncQueueDao().getPendingOperations("FAILED")
        val allOpsToPush = pendingOps + failedOps
        
        for (op in allOpsToPush) {
            try {
                localDb.syncQueueDao().updateStatus(op.id, "SYNCING")
                
                // Simple representation of pushing to Supabase
                when (op.operation) {
                    "CREATE" -> {
                        val payload = json.decodeFromString<JsonObject>(op.payload!!)
                        val cleanPayloadMap = payload.toMutableMap().apply {
                            remove("deleted_at")
                        }
                        supabase.postgrest[op.entity_type].insert(JsonObject(cleanPayloadMap))
                    }
                    "UPDATE" -> {
                        val payload = json.decodeFromString<JsonObject>(op.payload!!)
                        // Strip out fields that shouldn't be updated (PK and timestamps) to prevent Postgres errors
                        val cleanPayloadMap = payload.toMutableMap().apply {
                            remove("id")
                            remove("created_at")
                            remove("updated_at")
                            remove("deleted_at")
                        }
                        val finalPayload = JsonObject(cleanPayloadMap)
                        
                        supabase.postgrest[op.entity_type].update(finalPayload) {
                            filter { eq("id", op.entity_id) }
                        }
                    }
                    "DELETE" -> {
                        // Assuming soft delete is handled via an UPDATE with deleted_at
                        val payload = json.decodeFromString<JsonObject>(op.payload!!)
                        supabase.postgrest[op.entity_type].update(payload) {
                            filter { eq("id", op.entity_id) }
                        }
                    }
                }
                
                localDb.syncQueueDao().updateStatus(op.id, "SYNCED")
            } catch (e: Exception) {
                // Update status to FAILED and also save the exact error message!
                localDb.syncQueueDao().updateStatusAndError(op.id, "FAILED", e.message ?: "Unknown error")
                Log.e("SyncManager", "Failed to sync operation ${op.id}: ${e.message}")
            }
        }
        
        // Clean up
        localDb.syncQueueDao().deleteSyncedOperations()
    }

    private suspend fun pullRemoteChanges() {
        // Fetch all entity IDs that have pending or failed sync operations.
        // We will NOT overwrite these locally until they are successfully pushed.
        val pendingOps = localDb.syncQueueDao().getPendingOperations("PENDING")
        val failedOps = localDb.syncQueueDao().getPendingOperations("FAILED")
        val pendingEntityIds = (pendingOps + failedOps).map { it.entity_id }.toSet()

        // Example for products:
        try {
            val remoteProducts = supabase.postgrest["products"].select().decodeList<com.devsoft.freshfood.domain.model.Product>()
            val remoteEntities = remoteProducts.map { com.devsoft.freshfood.data.local.entity.ProductEntity.fromDomainModel(it) }
            
            // Use remote stock as source of truth for products that don't have pending operations
            val entitiesToInsert = remoteEntities.filter { !pendingEntityIds.contains(it.id) }
            
            if (entitiesToInsert.isNotEmpty()) {
                localDb.productDao().insertProducts(entitiesToInsert)
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to pull products: ${e.message}")
        }
        
        // Customers...
        try {
            val remoteCustomers = supabase.postgrest["customers"].select().decodeList<com.devsoft.freshfood.domain.model.Customer>()
            val entities = remoteCustomers.map { com.devsoft.freshfood.data.local.entity.CustomerEntity.fromDomainModel(it) }
            
            // Only insert customers that do NOT have a pending local change
            val entitiesToInsert = entities.filter { !pendingEntityIds.contains(it.id) }
            if (entitiesToInsert.isNotEmpty()) {
                localDb.customerDao().insertCustomers(entitiesToInsert)
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to pull customers: ${e.message}")
        }
        
        // Profiles
        try {
            val remoteProfiles = supabase.postgrest["profiles"].select().decodeList<com.devsoft.freshfood.data.local.entity.ProfileEntity>()
            localDb.profileDao().insertProfiles(remoteProfiles)
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to pull profiles: ${e.message}")
        }
        
        // Notifications
        try {
            val remoteNotifs = supabase.postgrest["notifications"].select().decodeList<com.devsoft.freshfood.data.local.entity.NotificationEntity>()
            
            // Check if any are new by filtering those not currently in DB
            val existingNotifsFlow = localDb.notificationDao().getAllNotifications()
            
            localDb.notificationDao().insertNotifications(remoteNotifs)
            
            // Trigger Android notifications for unread ones (assuming background sync or opened app)
            val unreadNotifs = remoteNotifs.filter { !it.is_read }
            if (unreadNotifs.isNotEmpty()) {
                val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel("delivery_channel", "Delivery Notifications", NotificationManager.IMPORTANCE_HIGH)
                    notificationManager.createNotificationChannel(channel)
                }
                
                unreadNotifs.forEach { notif ->
                    val builder = NotificationCompat.Builder(context, "delivery_channel")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(notif.title)
                        .setContentText(notif.message)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        
                    notificationManager.notify(notif.id.hashCode(), builder.build())
                }
            }
        } catch (e: Exception) {
            Log.e("SyncManager", "Failed to pull notifications: ${e.message}")
        }
    }
}
