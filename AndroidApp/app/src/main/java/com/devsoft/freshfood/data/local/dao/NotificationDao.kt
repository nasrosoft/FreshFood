package com.devsoft.freshfood.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devsoft.freshfood.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY created_at DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationEntity>)
    
    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}
