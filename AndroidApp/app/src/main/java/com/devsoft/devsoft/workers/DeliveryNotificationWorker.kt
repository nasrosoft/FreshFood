package com.devsoft.devsoft.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.devsoft.devsoft.data.repository.DeliveryRepositoryImpl
import com.devsoft.devsoft.utils.NotificationHelper
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json

class DeliveryNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userId = NotificationHelper.getPersistedUserId(applicationContext)
        val userRole = NotificationHelper.getPersistedUserRole(applicationContext)

        if (userId.isNullOrBlank() || userRole != "DELIVERY") {
            return Result.success()
        }

        return try {
            val supabaseClient = createSupabaseClient(
                supabaseUrl = "https://rjlacsdehgxzsghdzbul.supabase.co",
                supabaseKey = "sb_publishable_oln733bAmVovVJLPQ_BFvQ_AaEr1D0Q"
            ) {
                install(Postgrest)
                defaultSerializer = KotlinXSerializer(Json {
                    ignoreUnknownKeys = true
                    coerceInputValues = true
                })
            }

            val deliveryRepo = DeliveryRepositoryImpl(supabaseClient)
            val deliveries = deliveryRepo.getDeliveries().first()

            val activeDeliveries = deliveries.filter {
                (it.order.delivery_employee_id == userId || it.order.delivery_employee_id == "00000000-0000-0000-0000-000000000000") &&
                (it.order.status == "ASSIGNED" || it.order.status == "PENDING" || it.order.status == "OUT_FOR_DELIVERY")
            }

            for (delivery in activeDeliveries) {
                val orderId = delivery.order.id
                if (!NotificationHelper.isOrderNotified(applicationContext, orderId)) {
                    val shortId = orderId.take(8).uppercase()
                    val customerName = delivery.customer?.name ?: "Client"
                    NotificationHelper.showDeliveryNotification(
                        context = applicationContext,
                        title = "Nouvelle livraison assignée 🚚",
                        message = "Commande #$shortId de $customerName prête pour la livraison.",
                        orderId = orderId
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
