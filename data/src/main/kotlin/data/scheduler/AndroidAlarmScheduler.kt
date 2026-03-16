package data.scheduler

import domain.models.Alarm
import domain.scheduler.IAlarmScheduler

class AndroidAlarmScheduler : IAlarmScheduler {
    override fun schedule(alarm: Alarm) {
        println("OS: Системный будильник запланирован на ${alarm.time}")
    }

    override fun cancel(alarm: Alarm) {
        println("OS: Будильник отменен")
    }
}
