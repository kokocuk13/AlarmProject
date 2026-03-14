package domain.usecases

import domain.models.Alarm
import domain.repository.IAlarmRepository
import domain.scheduler.IAlarmScheduler

/**
 * Удаляет будильник из БД и отменяет его системное планирование.
 * Принимает полную модель Alarm, чтобы передать планировщику для отмены PendingIntent.
 */
class DeleteAlarmUseCase(
    private val repository: IAlarmRepository,
    private val scheduler: IAlarmScheduler
) {
    suspend operator fun invoke(alarm: Alarm): Result<Unit> {
        val result = repository.deleteAlarm(alarm.id)
        if (result.isSuccess) {
            // Отменяем системный будильник только при успешном удалении из БД
            scheduler.cancel(alarm)
        }
        return result
    }
}
