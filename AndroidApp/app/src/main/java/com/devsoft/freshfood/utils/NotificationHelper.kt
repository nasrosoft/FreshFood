package com.devsoft.freshfood.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.devsoft.freshfood.MainActivity
import com.devsoft.freshfood.R

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

    fun showDeliveryNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = System.currentTimeMillis().toInt()
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

        val builder = NotificationCompat.Builder(context, CHANNEL_DELIVERIES_ID)
            .setSmallIcon(R.drawable.app_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
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
