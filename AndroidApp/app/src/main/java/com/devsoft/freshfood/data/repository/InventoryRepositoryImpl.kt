package com.devsoft.freshfood.data.repository

import com.devsoft.freshfood.domain.model.InventoryItem
import com.devsoft.freshfood.domain.model.InventorySession
import com.devsoft.freshfood.domain.model.ReturnOrder
import com.devsoft.freshfood.domain.repository.InventoryRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class InventoryRepositoryImpl(
    private val supabase: SupabaseClient
) : InventoryRepository {

    override suspend fun getInventorySessions(): Flow<List<InventorySession>> = flow {
        val sessions = supabase.postgrest["inventory_sessions"].select().decodeList<InventorySession>()
        emit(sessions)
    }

    override suspend fun createInventorySession(session: InventorySession, items: List<InventoryItem>): Result<String> {
        return try {
            supabase.postgrest["inventory_sessions"].insert(session)
            if (items.isNotEmpty()) {
                supabase.postgrest["inventory_items"].insert(items)
            }
            Result.success(session.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getReturns(): Flow<List<ReturnOrder>> = flow {
        val returns = supabase.postgrest["returns"].select().decodeList<ReturnOrder>()
        emit(returns)
    }

    override suspend fun createReturnOrder(returnOrder: ReturnOrder): Result<String> {
        return try {
            supabase.postgrest["returns"].insert(returnOrder)
            Result.success(returnOrder.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateReturnStatus(id: String, newStatus: String): Result<Unit> {
        return try {
            supabase.postgrest["returns"].update(
                {
                    set("status", newStatus)
                }
            ) {
                filter { eq("id", id) }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
