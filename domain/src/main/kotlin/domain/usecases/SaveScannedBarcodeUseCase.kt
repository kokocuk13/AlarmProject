//Use Case для сохранения отсканированного штрихкода. Проверяет, существует ли уже штрихкод с таким значением, и если нет, сохраняет его в репозитории. Возвращает результат операции, который может быть успешным с сохраненным штрихкодом или неудачным с ошибкой. Нужен для чистого разделения логики сохранения штрихкода от остальной части приложения.
package domain.usecases

import domain.models.SavedBarcode
import domain.repository.ISavedBarcodeRepository

class SaveScannedBarcodeUseCase(
    private val repository: ISavedBarcodeRepository
) {
    suspend operator fun invoke(codeValue: String, alias: String = codeValue): Result<SavedBarcode> {
        return repository.saveIfNotExists(codeValue = codeValue, alias = alias)
    }
}

