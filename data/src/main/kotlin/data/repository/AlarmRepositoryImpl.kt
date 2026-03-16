package data.repository

import domain.models.Alarm
import domain.repository.IAlarmRepository
import kotlinx.coroutines.delay

class AlarmRepositoryImpl : IAlarmRepository {
    override suspend fun saveAlarm(alarm: Alarm): Result<Unit> {
        println("Data: Преобразование модели Alarm в Entity...")
        delay(1000)
        println("Data: Будильник успешно сохранен в Mock DB")
        return Result.success(Unit)
    }
}
