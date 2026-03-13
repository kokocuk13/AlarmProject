package domain.repository
import domain.models.Alarm

interface IAlarmRepository {
    suspend fun saveAlarm(alarm: Alarm): Result<Unit>
}