package com.devsoft.freshfood.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.devsoft.freshfood.data.local.entity.SyncQueueEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SyncQueueEntity): Long

    @Update
    suspend fun update(entity: SyncQueueEntity)

    @Query("SELECT * FROM sync_queue WHERE status = :status ORDER BY created_at ASC")
    suspend fun getPendingOperations(status: String = "PENDING"): List<SyncQueueEntity>

    @Query("SELECT * FROM sync_queue WHERE entity_id = :entityId AND status = :status")
    suspend fun getPendingOperationForEntity(entityId: String, status: String = "PENDING"): SyncQueueEntity?

    @Query("DELETE FROM sync_queue WHERE status = 'SYNCED'")
    suspend fun deleteSyncedOperations()
    
    @Query("UPDATE sync_queue SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Query("UPDATE sync_queue SET status = :status, last_error = :error WHERE id = :id")
    suspend fun updateStatusAndError(id: Int, status: String, error: String)
}
