package com.example.alarmproject.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.alarmproject.di.AppModule
import com.example.alarmproject.ui.MainActivity
import domain.models.Alarm
import domain.models.ShakeTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver, вызываемый AlarmManager при срабатывании будильника.
 * Получает свежие данные о будильнике из БД и показывает уведомление.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("ALARM_ID", -1L)
        Log.d("ALARM_DEBUG", "AlarmReceiver: Received broadcast for ID: $alarmId")
        
        if (alarmId == -1L) {
            Log.e("ALARM_DEBUG", "AlarmReceiver: Error - ALARM_ID is -1")
            return
        }

        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("ALARM_DEBUG", "AlarmReceiver: Fetching alarm data from DB for ID: $alarmId")
                val result = AppModule.repository.getAlarmById(alarmId)
                val alarm = result.getOrNull()

                if (alarm != null) {
                    Log.d("ALARM_DEBUG", "AlarmReceiver: Alarm found: ${alarm.name}. Showing notification.")
                    showNotification(context, alarm)
                } else {
                    Log.e("ALARM_DEBUG", "AlarmReceiver: Alarm not found in DB for ID: $alarmId")
                }
            } catch (e: Exception) {
                Log.e("ALARM_DEBUG", "AlarmReceiver: Error processing alarm", e)
            } finally {
                pendingResult.finish()
                Log.d("ALARM_DEBUG", "AlarmReceiver: Finished processing")
            }
        }
    }

    private fun showNotification(context: Context, alarm: Alarm) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "alarm_channel"

        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_NAME", alarm.name)
            putExtra("ALARM_HOUR", alarm.time.hour)
            putExtra("ALARM_MINUTE", alarm.time.minute)
            
            when (val task = alarm.task) {
                is ShakeTask -> {
                    putExtra(MainActivity.EXTRA_TASK_TYPE, MainActivity.TASK_SHAKE)
                    putExtra(MainActivity.EXTRA_REQUIRED_SHAKES, task.requiredShakes)
                }
            }
        }

        val contentIntent = PendingIntent.getActivity(
            context,
            alarm.id.toInt(),
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Будильники",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Уведомления о срабатывании будильников"
                setSound(null, null) // Звук будет управляться отдельно
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("Будильник: ${alarm.name ?: "Пора вставать!"}")
            .setContentText("Нажмите, чтобы отключить")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false) // НЕ удалять автоматически
            .setOngoing(true) // Сделать уведомление постоянным
            .setContentIntent(contentIntent)
            .setFullScreenIntent(contentIntent, true)
            .build()

        notificationManager.notify(alarm.id.toInt(), notification)
    }
}
