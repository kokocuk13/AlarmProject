package data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import domain.models.Alarm
import domain.scheduler.IAlarmScheduler
import java.util.Calendar


class AndroidAlarmScheduler(
    private val context: Context,
    private val receiverClass: Class<out BroadcastReceiver>
) : IAlarmScheduler {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override fun schedule(alarm: Alarm) {
        val now = Calendar.getInstance()

        val pendingIntent = buildPendingIntent(alarm, PendingIntent.FLAG_UPDATE_CURRENT)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.time.hour)
            set(Calendar.MINUTE, alarm.time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_MONTH, 1)
                Log.d("ALARM_DEBUG", "Target time already passed today. Scheduled for tomorrow.")
            }
        }

        val triggerAtMillis = calendar.timeInMillis
        Log.d("ALARM_DEBUG", "Scheduling alarm ${alarm.id} for ${calendar.time} (millis: $triggerAtMillis)")
        Log.d("ALARM_DEBUG", "Current system time: ${System.currentTimeMillis()}")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            Log.w("ALARM_DEBUG", "Exact alarms NOT allowed. Using inexact fallback.")
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Log.d("ALARM_DEBUG", "Using setExactAndAllowWhileIdle")
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
            Log.d("ALARM_DEBUG", "Using setExact")
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    override fun cancel(alarm: Alarm) {
        Log.d("ALARM_DEBUG", "Cancelling alarm ${alarm.id}")
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
        }

        return PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
