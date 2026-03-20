package com.example.alarmproject.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.alarmproject.ui.MainActivity
import presentation.utils.MelodyPlayer

class AlarmService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        val alarmId = intent?.getLongExtra("ALARM_ID", -1L) ?: -1L
        val melodyUri = intent?.getStringExtra("ALARM_MELODY_URI")
        val alarmName = intent?.getStringExtra("ALARM_NAME") ?: "Будильник"
        val hour = intent?.getIntExtra("ALARM_HOUR", 0) ?: 0
        val minute = intent?.getIntExtra("ALARM_MINUTE", 0) ?: 0
        val taskType = intent?.getStringExtra(MainActivity.EXTRA_TASK_TYPE)
        val requiredShakes = intent?.getIntExtra(MainActivity.EXTRA_REQUIRED_SHAKES, 20)
        val requiredBarcode = intent?.getStringExtra(MainActivity.EXTRA_REQUIRED_BARCODE)

        if (action == ACTION_STOP) {
            MelodyPlayer.stop()
            stopForeground(true)
            stopSelf()
            return START_NOT_STICKY
        }

        // Запускаем музыку
        MelodyPlayer.start(this, melodyUri)

        // Показываем уведомление (делаем сервис Foreground)
        val notification = createNotification(
            alarmId, alarmName, hour, minute, taskType, requiredShakes, requiredBarcode
        )
        startForeground(alarmId.toInt().coerceAtLeast(1), notification)

        return START_STICKY
    }

    private fun createNotification(
        alarmId: Long,
        name: String,
        hour: Int,
        minute: Int,
        taskType: String?,
        requiredShakes: Int?,
        requiredBarcode: String?
    ): android.app.Notification {
        val channelId = "alarm_service_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId, "Будильник работает", NotificationManager.IMPORTANCE_HIGH
            ).apply {
                setSound(null, null)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_NAME", name)
            putExtra("ALARM_HOUR", hour)
            putExtra("ALARM_MINUTE", minute)
            putExtra(MainActivity.EXTRA_TASK_TYPE, taskType)
            putExtra(MainActivity.EXTRA_REQUIRED_SHAKES, requiredShakes)
            putExtra(MainActivity.EXTRA_REQUIRED_BARCODE, requiredBarcode)
        }

        val pendingIntent = PendingIntent.getActivity(
            this, alarmId.toInt(), activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(name)
            .setContentText("Пора вставать! ($hour:$minute)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true)
            .setOngoing(true)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "STOP_ALARM_SERVICE"
        
        fun start(context: Context, alarmId: Long, name: String, hour: Int, minute: Int, melodyUri: String?, taskType: String?, shakes: Int?, barcode: String?) {
            val intent = Intent(context, AlarmService::class.java).apply {
                putExtra("ALARM_ID", alarmId)
                putExtra("ALARM_NAME", name)
                putExtra("ALARM_HOUR", hour)
                putExtra("ALARM_MINUTE", minute)
                putExtra("ALARM_MELODY_URI", melodyUri)
                putExtra(MainActivity.EXTRA_TASK_TYPE, taskType)
                putExtra(MainActivity.EXTRA_REQUIRED_SHAKES, shakes)
                putExtra(MainActivity.EXTRA_REQUIRED_BARCODE, barcode)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AlarmService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
