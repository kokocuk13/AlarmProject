package domain.usecases

import domain.models.Alarm
import domain.models.BarcodeTask
import domain.models.ShakeTask
import domain.repository.IAlarmRepository
import domain.scheduler.IAlarmScheduler
import java.time.LocalTime

data class CreateAlarmParams(
    val time: LocalTime,
    val difficultyLevel: Int,
    val name: String? = null,
    val barcodeValue: String? = null //Если указано, будет создана задача BarcodeTask, иначе ShakeTask
)

class CreateAlarmUseCase(
    private val repository: IAlarmRepository,
    private val scheduler: IAlarmScheduler
) {
    suspend fun invoke(params: CreateAlarmParams): Result<Unit> {
        val alarm = Alarm(
            time = params.time,
            isEnabled = true,
            task = if (!params.barcodeValue.isNullOrBlank()) { //Если передано значение для штрихкода, создаем задачу BarcodeTask, иначе ShakeTask
                BarcodeTask(requiredBarcode = params.barcodeValue, isCompleted = false)
            } else {
                ShakeTask(requiredShakes = params.difficultyLevel, isCompleted = false)
            },
            name = params.name
        )

        val saveResult = repository.saveAlarm(alarm)
        if (saveResult.isSuccess) {
            // Планируем будильник с реальным id, назначенным базой данных
            val savedAlarm = saveResult.getOrThrow()
            scheduler.schedule(savedAlarm)
        }
        return saveResult.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
    }
}
