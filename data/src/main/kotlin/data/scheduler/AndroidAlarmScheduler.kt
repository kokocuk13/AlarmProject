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
        if (!alarm.isEnabled) {
            cancel(alarm)
            return
        }

        val pendingIntent = buildPendingIntent(alarm, PendingIntent.FLAG_UPDATE_CURRENT)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, alarm.time.hour)
            set(Calendar.MINUTE, alarm.time.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            if (alarm.days.isEmpty()) {
                // Единоразовый будильник
                if (timeInMillis <= System.currentTimeMillis()) {
                    add(Calendar.DAY_OF_MONTH, 1)
                }
            } else {
                // Повторяющийся будильник: ищем ближайший день (0=ПН, 6=ВС)
                val todayCalendar = get(Calendar.DAY_OF_WEEK)
                val todayFormatted = if (todayCalendar == Calendar.SUNDAY) 6 else todayCalendar - 2
                
                var daysUntilNext = -1
                if (alarm.days.contains(todayFormatted) && timeInMillis > System.currentTimeMillis()) {
                    daysUntilNext = 0
                } else {
                    for (i in 1..7) {
                        val nextDay = (todayFormatted + i) % 7
                        if (alarm.days.contains(nextDay)) {
                            daysUntilNext = i
                            break
                        }
                    }
                }
                
                if (daysUntilNext != -1) {
                    add(Calendar.DAY_OF_MONTH, daysUntilNext)
                }
            }
        }

        val triggerAtMillis = calendar.timeInMillis
        Log.d("ALARM_DEBUG", "Scheduling alarm ${alarm.id} for ${calendar.time} (millis: $triggerAtMillis)")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        } else {
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
            it.cancel()
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
