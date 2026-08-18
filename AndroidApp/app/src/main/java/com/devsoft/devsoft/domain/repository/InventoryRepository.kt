package com.devsoft.devsoft.domain.repository

import com.devsoft.devsoft.domain.model.InventoryItem
import com.devsoft.devsoft.domain.model.InventorySession
import com.devsoft.devsoft.domain.model.ReturnOrder
import kotlinx.coroutines.flow.Flow

interface InventoryRepository {
    // Inventory
    suspend fun getInventorySessions(): Flow<List<InventorySession>>
    suspend fun createInventorySession(session: InventorySession, items: List<InventoryItem>): Result<String>
    
    // Returns
    suspend fun getReturns(): Flow<List<ReturnOrder>>
    suspend fun createReturnOrder(returnOrder: ReturnOrder): Result<String>
    suspend fun updateReturnStatus(id: String, newStatus: String): Result<Unit>
}
