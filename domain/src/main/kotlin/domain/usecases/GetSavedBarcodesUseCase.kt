// Получение сохраненных штрихкодов из репозитория. Возвращает поток со списком сохраненных штрихкодов. Добавлен для получения сохраненных штрихкодов и отображения их в UI. Использует репозиторий для доступа к данным.
package domain.usecases

import domain.models.SavedBarcode
import domain.repository.ISavedBarcodeRepository
import kotlinx.coroutines.flow.Flow

class GetSavedBarcodesUseCase(
    private val repository: ISavedBarcodeRepository
) {
    operator fun invoke(): Flow<List<SavedBarcode>> = repository.getSavedBarcodes()
}

