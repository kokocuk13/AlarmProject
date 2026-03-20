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
    val days: List<Int> = emptyList(),
    val taskType: String = "SHAKE",
    val barcodeValue: String? = null,
    val alarmId: Long = 0L,
    val melodyUri: String? = null
)

class CreateAlarmUseCase(
    private val repository: IAlarmRepository,
    private val scheduler: IAlarmScheduler
) {
    suspend fun invoke(params: CreateAlarmParams): Result<Unit> {
        val task = if (params.taskType == "BARCODE" && !params.barcodeValue.isNullOrBlank()) {
            BarcodeTask(requiredBarcode = params.barcodeValue, isCompleted = false)
        } else {
            ShakeTask(requiredShakes = params.difficultyLevel, isCompleted = false)
        }

        val alarm = Alarm(
            id = params.alarmId,
            time = params.time,
            isEnabled = true,
            task = task,
            name = params.name,
            days = params.days,
            melodyUri = params.melodyUri
        )

        val saveResult = repository.saveAlarm(alarm)
        if (saveResult.isSuccess) {
            val savedAlarm = saveResult.getOrThrow()
            scheduler.schedule(savedAlarm)
        }
        return saveResult.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { Result.failure(it) }
        )
    }
}
