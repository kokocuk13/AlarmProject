package domain.repository

import domain.models.Alarm
import kotlinx.coroutines.flow.Flow

interface IAlarmRepository {
    /** Сохраняет будильник в БД. Возвращает сохранённую модель с назначенным id. */
    suspend fun saveAlarm(alarm: Alarm): Result<Alarm>

    /** Возвращает живой поток всех будильников (обновляется при изменениях в БД). */
    fun getAlarms(): Flow<List<Alarm>>

    /** Удаляет будильник по id. */
    suspend fun deleteAlarm(id: Long): Result<Unit>
}
