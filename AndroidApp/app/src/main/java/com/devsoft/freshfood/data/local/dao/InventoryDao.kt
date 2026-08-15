package com.devsoft.freshfood.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.devsoft.freshfood.data.local.entity.InventoryItemEntity
import com.devsoft.freshfood.data.local.entity.InventorySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: InventorySessionEntity)

    @Update
    suspend fun updateSession(session: InventorySessionEntity)

    @Query("SELECT * FROM inventory_sessions ORDER BY date DESC")
    fun getAllSessions(): Flow<List<InventorySessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryItemEntity>)

    @Query("SELECT * FROM inventory_items WHERE session_id = :sessionId")
    fun getItemsForSession(sessionId: String): Flow<List<InventoryItemEntity>>
}
