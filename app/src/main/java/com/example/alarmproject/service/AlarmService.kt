package com.example.alarmproject.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.alarmproject.ui.MainActivity
import presentation.utils.MelodyPlayer

class AlarmService : Service() {

    private var vibrator: Vibrator? = null

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
            Log.d("ALARM_DEBUG", "AlarmService: ACTION_STOP received")
            stopAlarm()
            return START_NOT_STICKY
        }

        Log.d("ALARM_DEBUG", "AlarmService: Starting alarm for ID: $alarmId")

        // 1. Создаем уведомление
        val notification = createNotification(
            alarmId, alarmName, hour, minute, taskType, requiredShakes, requiredBarcode
        )

        // 2. СРАЗУ переводим сервис в Foreground, чтобы избежать ForegroundServiceDidNotStartInTimeException.
        // На Android 14+ (API 34) ОБЯЗАТЕЛЬНО указывать foregroundServiceType, если он указан в манифесте.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    alarmId.toInt().coerceAtLeast(1),
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
                )
            } else {
                startForeground(alarmId.toInt().coerceAtLeast(1), notification)
            }
        } catch (e: Exception) {
            Log.e("ALARM_DEBUG", "AlarmService: Failed to startForeground", e)
            stopSelf()
            return START_NOT_STICKY
        }

        // 3. Запускаем музыку и вибрацию
        MelodyPlayer.start(this, melodyUri)
        startVibration()

        return START_STICKY
    }

    private fun startVibration() {
        Log.d("ALARM_DEBUG", "AlarmService: startVibration() called")
        try {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            val pattern = longArrayOf(0, 500, 1000) // задержка 0, вибрация 500мс, пауза 1000мс
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d("ALARM_DEBUG", "AlarmService: Using VibrationEffect (API >= 26)")
                vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
            } else {
                Log.d("ALARM_DEBUG", "AlarmService: Using classic vibrate (API < 26)")
                @Suppress("DEPRECATION")
                vibrator?.vibrate(pattern, 0)
            }
        } catch (e: Exception) {
            Log.e("ALARM_DEBUG", "AlarmService: startVibration() failed", e)
        }
    }

    private fun stopAlarm() {
        Log.d("ALARM_DEBUG", "AlarmService: stopAlarm() called")
        MelodyPlayer.stop()
        vibrator?.cancel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        stopSelf()
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

    override fun onDestroy() {
        vibrator?.cancel()
        super.onDestroy()
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
