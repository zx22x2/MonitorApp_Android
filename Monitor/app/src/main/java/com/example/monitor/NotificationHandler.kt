package com.example.monitor

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class NotificationHandler(context: Context) {
    private val context = context
    private val channelID = "channelID"
    private val channelName = "channel name"
    private var manager: NotificationManager? = null
    private var channel: NotificationChannel? = null

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            channel = NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH)
            manager!!.createNotificationChannel(channel!!)
        }
    }

    fun send(data: String) {
        val builder = NotificationCompat.Builder(context, channelID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("裝置通知")
            .setContentText(data)
            .setAutoCancel(true)

        val managerCompat = NotificationManagerCompat.from(context)
        managerCompat.notify(0, builder.build())
    }
}