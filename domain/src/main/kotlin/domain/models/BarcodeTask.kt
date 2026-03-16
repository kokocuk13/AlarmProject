package domain.models

data class BarcodeTask(
    val requiredBarcode: String, // XXX ЗАГЛУШКА
    override val isCompleted: Boolean = false
) : DismissTask(isCompleted)