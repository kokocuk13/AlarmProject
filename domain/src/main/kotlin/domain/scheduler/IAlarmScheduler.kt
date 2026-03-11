package domain.scheduler

import domain.models.Alarm

interface IAlarmScheduler {
    fun schedule(alarm: Alarm)
    fun cancel(alarm: Alarm)
}
