package com.devsoft.freshfood.data.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.devsoft.freshfood.data.local.FreshFoodDatabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

class SyncWorker(
    appContext: Context, 
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = FreshFoodDatabase.getDatabase(applicationContext)
        
        // Normally you'd inject SupabaseClient with Hilt/Dagger, but we instantiate it here for the worker if needed, 
        // or fetch it from a singleton pattern.
        val supabaseClient = createSupabaseClient(
            supabaseUrl = "https://rjlacsdehgxzsghdzbul.supabase.co",
            supabaseKey = "sb_publishable_oln733bAmVovVJLPQ_BFvQ_AaEr1D0Q"
        ) {
            install(Postgrest)
        }
        
        val syncManager = SyncManager(database, supabaseClient, applicationContext)
        
        return try {
            val success = syncManager.syncAll()
            if (success) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
