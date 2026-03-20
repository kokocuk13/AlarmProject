//Интерфейс репозитория для сохраненных штрихкодов. Предоставляет методы для сохранения штрихкода и получения списка сохраненных штрихкодов.
package domain.repository

import domain.models.SavedBarcode
import kotlinx.coroutines.flow.Flow

interface ISavedBarcodeRepository {
    suspend fun saveIfNotExists(codeValue: String, alias: String = codeValue): Result<SavedBarcode>
    fun getSavedBarcodes(): Flow<List<SavedBarcode>>
}

