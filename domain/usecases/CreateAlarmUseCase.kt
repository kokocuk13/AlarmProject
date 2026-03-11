package domain.usecases

import domain.models.*
import domain.repository.IAlarmRepository
import domain.scheduler.IAlarmScheduler
import java.time.LocalTime


data class CreateAlarmParams(
    val time: LocalTime, 
    val taskType: String, 
    val difficultyLevel: Int
)

class CreateAlarmUseCase(
    private val repository: IAlarmRepository,
    private val scheduler: IAlarmScheduler
) {
    
    suspend fun invoke(params: CreateAlarmParams): Result<Unit> {
        val alarm = Alarm(
            time = params.time,
            isEnabled = true,
            task = ShakeTask(requiredShakes = params.difficultyLevel, isCompleted = false)
        )

        val result = repository.saveAlarm(alarm)
        if (result.isSuccess) {
            scheduler.schedule(alarm)
        }
        return result
    }
}