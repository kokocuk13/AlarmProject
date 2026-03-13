package data.repository

import domain.models.Alarm
import domain.repository.IAlarmRepository
import kotlinx.coroutines.delay

class AlarmRepositoryImpl : IAlarmRepository {
    
    override suspend fun saveAlarm(alarm: Alarm): Result<Unit> {
        // Имитируем шаг "mapToEntity" и "insert" из твоей диаграммы
        println("Data: Преобразование модели Alarm в Entity...")
        
        // Пункт 3 критериев: обработка состояний (loading)
        delay(1000) // Имитируем секундную задержку записи в БД
        
        println("Data: Будильник успешно сохранен в Mock DB")
        return Result.success(Unit)
    }
}
