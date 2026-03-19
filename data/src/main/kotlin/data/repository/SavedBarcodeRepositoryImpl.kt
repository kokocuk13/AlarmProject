// Сохранение отсканированных штрихкодов в базу данных и предоставление доступа к сохраненным данным. Реализует интерфейс ISavedBarcodeRepository, используя AlarmDao для взаимодействия с базой данных. Методы включают сохранение штрихкода, если он еще не существует, и получение списка всех сохраненных штрихкодов в виде потока данных.
package data.repository

import data.db.AlarmDao
import data.db.SavedBarcodeEntity
import domain.models.BarcodeFormat
import domain.models.SavedBarcode
import domain.repository.ISavedBarcodeRepository
import java.time.Instant
import java.time.ZoneId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SavedBarcodeRepositoryImpl(private val dao: AlarmDao) : ISavedBarcodeRepository {

    override suspend fun saveIfNotExists(codeValue: String, alias: String): Result<SavedBarcode> {
        return try {
            val existing = dao.findSavedBarcodeByValue(codeValue)
            if (existing != null) return Result.success(existing.toDomain())

            val entity = SavedBarcodeEntity(
                codeValue = codeValue,
                format = BarcodeFormat.CODE_128.name,
                alias = alias,
                createdAt = System.currentTimeMillis()
            )
            val id = dao.insertSavedBarcode(entity)
            Result.success(entity.copy(id = id).toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSavedBarcodes(): Flow<List<SavedBarcode>> {
        return dao.getSavedBarcodes().map { list -> list.map { it.toDomain() } }
    }

    private fun SavedBarcodeEntity.toDomain(): SavedBarcode {
        val date = Instant.ofEpochMilli(createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
        return SavedBarcode(
            id = id,
            codeValue = codeValue,
            format = runCatching { BarcodeFormat.valueOf(format) }.getOrDefault(BarcodeFormat.CODE_128),
            alias = alias,
            createdAt = date
        )
    }
}


