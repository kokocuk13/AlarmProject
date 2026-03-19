package data.repository

import data.db.AlarmDao
import data.db.AlarmEntity
import domain.models.Alarm
import domain.models.BarcodeTask
import domain.models.ShakeTask
import domain.repository.IAlarmRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


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

    override suspend fun getAlarmById(id: Long): Result<Alarm?> {
        return try {
            val entity = dao.getById(id)
            Result.success(entity?.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Alarm.toEntity() = AlarmEntity(
        id = id,
        hour = time.hour,
        minute = time.minute,
        isEnabled = isEnabled,
        name = name,
        requiredShakes = (task as? ShakeTask)?.requiredShakes ?: 0,
        taskType = if (task is BarcodeTask) "BARCODE" else "SHAKE",// Определяем тип задачи для хранения
        requiredBarcode = (task as? BarcodeTask)?.requiredBarcode //Проверяем, если задача - BarcodeTask, сохраняем requiredBarcode, иначе null
    )

    private fun AlarmEntity.toDomain() = Alarm(
        id = id,
        time = java.time.LocalTime.of(hour, minute),
        isEnabled = isEnabled,
        task = if (taskType == "BARCODE" && !requiredBarcode.isNullOrBlank()) { // Если тип задачи - BARCODE и requiredBarcode не пустой, создаем BarcodeTask, иначе ShakeTask
            BarcodeTask(requiredBarcode = requiredBarcode, isCompleted = false)
        } else {
            ShakeTask(requiredShakes = requiredShakes, isCompleted = false)
        },
        name = name
    )
}
