package com.devsoft.devsoft.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.devsoft.devsoft.MainActivity
import com.devsoft.devsoft.R

object NotificationHelper {

    const val CHANNEL_DELIVERIES_ID = "channel_deliveries"
    const val CHANNEL_ALERTS_ID = "channel_alerts"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Deliveries Channel (High Importance for Driver)
            val deliveriesChannel = NotificationChannel(
                CHANNEL_DELIVERIES_ID,
                "Livraisons / Deliveries",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications pour les nouvelles commandes et livraisons assignées"
                enableLights(true)
                enableVibration(true)
            }

            // Alerts Channel (Default Importance for Admin)
            val alertsChannel = NotificationChannel(
                CHANNEL_ALERTS_ID,
                "Alertes Système / System Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alertes de stock faible et mises à jour"
                enableLights(true)
            }

            notificationManager.createNotificationChannel(deliveriesChannel)
            notificationManager.createNotificationChannel(alertsChannel)
        }
    }

    private const val PREFS_NAME = "devsoft_notifications_prefs"
    private const val KEY_NOTIFIED_ORDERS = "notified_delivery_order_ids"
    private const val KEY_USER_ID = "logged_in_user_id"
    private const val KEY_USER_ROLE = "logged_in_user_role"
    private const val WORK_NAME_DELIVERY = "delivery_notification_periodic_worker"

    fun persistCurrentUser(context: Context, userId: String?, role: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_USER_ROLE, role)
            .apply()
    }

    fun getPersistedUserId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_ID, null)
    }

    fun getPersistedUserRole(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_ROLE, null)
    }

    fun scheduleBackgroundDeliverySync(context: Context) {
        val constraints = androidx.work.Constraints.Builder()
            .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
            .build()

        val periodicRequest = androidx.work.PeriodicWorkRequestBuilder<com.devsoft.devsoft.workers.DeliveryNotificationWorker>(
            15, java.util.concurrent.TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        androidx.work.WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME_DELIVERY,
            androidx.work.ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )

        // Also trigger an immediate check
        val oneTimeRequest = androidx.work.OneTimeWorkRequestBuilder<com.devsoft.devsoft.workers.DeliveryNotificationWorker>()
            .setConstraints(constraints)
            .build()
        androidx.work.WorkManager.getInstance(context).enqueue(oneTimeRequest)
    }

    fun cancelBackgroundDeliverySync(context: Context) {
        androidx.work.WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME_DELIVERY)
    }

    fun isOrderNotified(context: Context, orderId: String): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val notifiedSet = prefs.getStringSet(KEY_NOTIFIED_ORDERS, emptySet()) ?: emptySet()
        return notifiedSet.contains(orderId)
    }

    fun markOrderAsNotified(context: Context, orderId: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val current = prefs.getStringSet(KEY_NOTIFIED_ORDERS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(orderId)
        prefs.edit().putStringSet(KEY_NOTIFIED_ORDERS, current).apply()
    }

    fun showDeliveryNotification(
        context: Context,
        title: String,
        message: String,
        orderId: String? = null,
        notificationId: Int = (orderId?.hashCode() ?: System.currentTimeMillis().toInt())
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (!orderId.isNullOrBlank()) {
                putExtra("EXTRA_ORDER_ID", orderId)
                putExtra("EXTRA_DESTINATION", "delivery_details")
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_DELIVERIES_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        if (!orderId.isNullOrBlank()) {
            markOrderAsNotified(context, orderId)
        }

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun showAlertNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = (System.currentTimeMillis() % 10000).toInt()
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ALERTS_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            val notificationManager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                notificationManager.notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
