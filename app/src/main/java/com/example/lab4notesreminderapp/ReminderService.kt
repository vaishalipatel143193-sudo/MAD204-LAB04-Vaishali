package com.example.lab4notesreminderapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.IBinder
import androidx.core.app.NotificationCompat

class ReminderService : Service() {

    private val CHANNEL_ID = "reminder_channel"
    private val NOTIF_ID = 1001

    override fun onCreate() {
        super.onCreate()
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // wait 5 seconds then show notification
        Handler(mainLooper).postDelayed({
            showNotification()
            stopSelf()
        }, 5000)

        return START_NOT_STICKY
    }

    private fun createChannel() {
        val name = "Reminders"
        val desc = "Channel for note reminders"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance)
        channel.description = desc
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun showNotification() {
        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        val notif = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Reminder")
            .setContentText("Check your notes!")
            .setAutoCancel(true)
            .build()
        nm.notify(NOTIF_ID, notif)
    }

    override fun onBind(intent: Intent?): IBinder? = null
}