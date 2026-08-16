package com.devsoft.freshfood.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devsoft.freshfood.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles WHERE role = :role")
    fun getProfilesByRole(role: String): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(profiles: List<ProfileEntity>)
    
    @Query("DELETE FROM profiles")
    suspend fun clearAll()

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfileById(id: String)
}
