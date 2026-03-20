package data.repository

import data.db.AlarmDao
import data.db.AlarmEntity
import domain.models.Alarm
import domain.models.BarcodeTask
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
            val finalId = if (alarm.id == 0L) generatedId else alarm.id
            Result.success(alarm.copy(id = finalId))
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

    override suspend fun getAlarmById(id: Long): Result<Alarm?> {
        return try {
            val entity = dao.getById(id)
            Result.success(entity?.toDomain())
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
        requiredShakes = (task as? ShakeTask)?.requiredShakes ?: 0,
        taskType = when (task) {
            is BarcodeTask -> "BARCODE"
            else -> "SHAKE"
        },
        requiredBarcode = (task as? BarcodeTask)?.requiredBarcode,
        days = days.joinToString(",")
    )

    private fun AlarmEntity.toDomain(): Alarm {
        val parsedDays = if (days.isBlank()) emptyList()
                         else days.split(",").mapNotNull { it.trim().toIntOrNull() }

        val task = if (taskType == "BARCODE" && !requiredBarcode.isNullOrBlank()) {
            BarcodeTask(requiredBarcode = requiredBarcode, isCompleted = false)
        } else {
            ShakeTask(requiredShakes = requiredShakes, isCompleted = false)
        }

        return Alarm(
            id = id,
            time = java.time.LocalTime.of(hour, minute),
            isEnabled = isEnabled,
            task = task,
            name = name,
            days = parsedDays
        )
    }
}
