package domain.usecases

import domain.models.Alarm
import domain.repository.IAlarmRepository
import domain.scheduler.IAlarmScheduler

class UpdateAlarmUseCase(
    private val repository: IAlarmRepository,
    private val scheduler: IAlarmScheduler
) {
    suspend operator fun invoke(alarm: Alarm): Result<Unit> {
        val saveResult = repository.saveAlarm(alarm)
        if (saveResult.isSuccess) {
            if (alarm.isEnabled) {
                scheduler.schedule(alarm)
            } else {
                scheduler.cancel(alarm)
            }
        }
        return saveResult.map { }
    }
}
