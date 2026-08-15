package com.devsoft.freshfood.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.devsoft.freshfood.data.local.entity.ReturnEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReturnDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReturn(returnOrder: ReturnEntity)

    @Update
    suspend fun updateReturn(returnOrder: ReturnEntity)

    @Query("SELECT * FROM returns ORDER BY date DESC")
    fun getAllReturns(): Flow<List<ReturnEntity>>
}
