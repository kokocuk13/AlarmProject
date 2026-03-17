package data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import domain.models.Alarm
import domain.scheduler.IAlarmScheduler
import java.util.Calendar

/**
 * Реализация планировщика будильников через Android AlarmManager.
 *
 * @param context контекст приложения (applicationContext).
 * @param receiverClass класс BroadcastReceiver, который сработает по будильнику.
 *   Передаётся снаружи, чтобы избежать циклической зависимости data → app.
 */
class AndroidAlarmScheduler(
    private val context: Context,
    private val receiverClass: Class<out BroadcastReceiver>
) : IAlarmScheduler {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm) {
        val pendingIntent = buildPendingIntent(alarm, PendingIntent.FLAG_UPDATE_CURRENT)

        // Вычисляем время срабатывания: если время уже прошло сегодня — ставим на завтра
        val triggerAtMillis = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.time.hour)
            set(Calendar.MINUTE, alarm.time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }.timeInMillis

        // Проверка разрешения на точные будильники для Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Если разрешение не получено, используем обычный setAndAllowWhileIdle (не гарантирует точность до секунды)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
            return
        }

        // setExactAndAllowWhileIdle обеспечивает срабатывание даже в Doze-режиме (API 23+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    override fun cancel(alarm: Alarm) {
        val intent = Intent(context, receiverClass)

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        pendingIntent?.let {
            alarmManager.cancel(it)
        }
    }

    private fun buildPendingIntent(alarm: Alarm, flags: Int): PendingIntent {
        val intent = Intent(context, receiverClass).apply {
            putExtra("ALARM_ID", alarm.id)
            putExtra("ALARM_NAME", alarm.name ?: "Будильник")
        }

        return PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
