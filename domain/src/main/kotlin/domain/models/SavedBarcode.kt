package domain.models

import java.time.LocalDate

data class SavedBarcode(
    val id: Long = 0L,
    val codeValue: String,
    val format: BarcodeFormat,
    val alias: String,
    val createdAt: LocalDate
)
