package com.example.sentinel_ip.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.sentinel_ip.R

object NotificationHelper {

    private const val CHANNEL_ID = "sentinel_threat_alerts"
    private const val CHANNEL_NAME = "Sentinel-IP Threat Alerts"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for critical threat score spikes on bookmarked targets"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    fun sendThreatAlert(context: Context, ipAddress: String, score: Int) {
        createNotificationChannel(context)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Critical Threat Alert: $ipAddress")
            .setContentText("Bookmarked IP $ipAddress abuse confidence score reached $score%!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(ipAddress.hashCode(), builder.build())
    }
}
