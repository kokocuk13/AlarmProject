package data.repository

import data.db.AlarmDao
import data.db.AlarmEntity
import domain.models.Alarm
import domain.models.ShakeTask
import domain.repository.IAlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Реальная реализация репозитория, хранящая будильники в Room (SQLite).
 *
 * @param dao DAO для работы с таблицей alarms.
 */
class AlarmRepositoryImpl(private val dao: AlarmDao) : IAlarmRepository {

    override suspend fun saveAlarm(alarm: Alarm): Result<Alarm> {
        return try {
            val entity = alarm.toEntity()
            val generatedId = dao.insert(entity)
            Result.success(alarm.copy(id = generatedId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getAlarms(): Flow<List<Alarm>> =
        dao.getAllAlarms().map { entities -> entities.map { it.toDomain() } }

    override suspend fun deleteAlarm(id: Long): Result<Unit> {
        return try {
            dao.deleteById(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ─── Маппинг ──────────────────────────────────────────────────────────────

    private fun Alarm.toEntity() = AlarmEntity(
        id = id,
        hour = time.hour,
        minute = time.minute,
        isEnabled = isEnabled,
        name = name,
        requiredShakes = (task as? ShakeTask)?.requiredShakes ?: 0
    )

    private fun AlarmEntity.toDomain() = Alarm(
        id = id,
        time = java.time.LocalTime.of(hour, minute),
        isEnabled = isEnabled,
        task = ShakeTask(requiredShakes = requiredShakes, isCompleted = false),
        name = name
    )
}
