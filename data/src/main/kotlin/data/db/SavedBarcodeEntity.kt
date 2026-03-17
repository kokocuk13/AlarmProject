package data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_barcodes")
data class SavedBarcodeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val codeValue: String,
    val format: String,
    val alias: String,
    val createdAt: Long
)
