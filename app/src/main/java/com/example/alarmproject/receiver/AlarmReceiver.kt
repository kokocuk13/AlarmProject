package com.example.alarmproject.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

/**
 * BroadcastReceiver, вызываемый AlarmManager при срабатывании будильника.
 * Показывает уведомление пользователю.
 *
 * В дальнейшем здесь можно запускать Foreground Service для воспроизведения звука/вибрации.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("ALARM_ID", -1L)
        val alarmName = intent.getStringExtra("ALARM_NAME") ?: "Будильник"
        showNotification(context, alarmId, alarmName)
    }

    private fun showNotification(context: Context, alarmId: Long, alarmName: String) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "alarm_channel"

        // Создаём канал уведомлений (необходимо для Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Будильники",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о срабатывании будильников"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Будильник сработал!")
            .setContentText(alarmName)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(alarmId.toInt(), notification)
    }
}
