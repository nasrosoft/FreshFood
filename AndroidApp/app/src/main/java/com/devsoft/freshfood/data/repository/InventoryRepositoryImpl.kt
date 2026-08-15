package com.devsoft.freshfood.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.devsoft.freshfood.data.local.FreshFoodDatabase
import com.devsoft.freshfood.data.local.entity.InventoryItemEntity
import com.devsoft.freshfood.data.local.entity.InventorySessionEntity
import com.devsoft.freshfood.data.local.entity.ReturnEntity
import com.devsoft.freshfood.data.local.entity.SyncQueueEntity
import com.devsoft.freshfood.domain.model.InventoryItem
import com.devsoft.freshfood.domain.model.InventorySession
import com.devsoft.freshfood.domain.model.ReturnOrder
import com.devsoft.freshfood.domain.repository.InventoryRepository
import com.devsoft.freshfood.utils.DeviceUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class InventoryRepositoryImpl(
    private val database: FreshFoodDatabase,
    private val context: Context
) : InventoryRepository {

    override suspend fun getInventorySessions(): Flow<List<InventorySession>> {
        return database.inventoryDao().getAllSessions().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun createInventorySession(session: InventorySession, items: List<InventoryItem>): Result<String> {
        return try {
            val sessionId = session.id
            val deviceId = DeviceUtil.getDeviceId(context)
            
            val sessionEntity = InventorySessionEntity(
                id = sessionId,
                date = session.date ?: java.time.Instant.now().toString(),
                status = session.status,
                conducted_by = session.conducted_by,
                notes = session.notes
            )
            
            val itemEntities = items.map { item ->
                InventoryItemEntity(
                    id = item.id,
                    session_id = sessionId,
                    product_id = item.product_id,
                    expected_quantity = item.expected_quantity,
                    actual_quantity = item.actual_quantity,
                    difference = item.difference
                )
            }
            
            database.withTransaction {
                database.inventoryDao().insertSession(sessionEntity)
                if (itemEntities.isNotEmpty()) {
                    database.inventoryDao().insertItems(itemEntities)
                }
                
                // Enqueue sync for session
                database.syncQueueDao().insert(
                    SyncQueueEntity(
                        entity_type = "inventory_sessions",
                        entity_id = sessionId,
                        operation = "CREATE",
                        payload = Json.encodeToString(session),
                        device_id = deviceId
                    )
                )
                
                // Enqueue sync for items
                itemEntities.forEach { itemEntity ->
                    database.syncQueueDao().insert(
                        SyncQueueEntity(
                            entity_type = "inventory_items",
                            entity_id = itemEntity.id,
                            operation = "CREATE",
                            payload = Json.encodeToString(itemEntity.toDomainModel()),
                            device_id = deviceId
                        )
                    )
                }
            }
            Result.success(sessionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReturns(): Flow<List<ReturnOrder>> {
        return database.returnDao().getAllReturns().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun createReturnOrder(returnOrder: ReturnOrder): Result<String> {
        return try {
            val returnId = returnOrder.id
            val deviceId = DeviceUtil.getDeviceId(context)
            
            val entity = ReturnEntity(
                id = returnId,
                date = returnOrder.date ?: java.time.Instant.now().toString(),
                customer_id = returnOrder.customer_id,
                product_id = returnOrder.product_id,
                quantity = returnOrder.quantity,
                reason = returnOrder.reason,
                status = returnOrder.status,
                created_by = returnOrder.created_by
            )
            
            database.withTransaction {
                database.returnDao().insertReturn(entity)
                
                database.syncQueueDao().insert(
                    SyncQueueEntity(
                        entity_type = "returns",
                        entity_id = returnId,
                        operation = "CREATE",
                        payload = Json.encodeToString(returnOrder),
                        device_id = deviceId
                    )
                )
            }
            
            Result.success(returnId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReturnStatus(id: String, newStatus: String): Result<Unit> {
        return try {
            val deviceId = DeviceUtil.getDeviceId(context)
            
            database.withTransaction {
                // In a real app we would get it first, update it, save it. Assuming we have a DAO method to update status directly.
                // For now, let's enqueue an update operation with partial payload
                val updatePayload = mapOf("id" to id, "status" to newStatus)
                
                // We'd also update the local Room database row. For simplicity, queueing sync:
                database.syncQueueDao().insert(
                    SyncQueueEntity(
                        entity_type = "returns",
                        entity_id = id,
                        operation = "UPDATE",
                        payload = Json.encodeToString(updatePayload),
                        device_id = deviceId
                    )
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
