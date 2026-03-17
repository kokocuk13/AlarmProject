package data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = AlarmEntity::class,
            parentColumns = ["id"],
            childColumns = ["alarm_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SavedBarcodeEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetBarcodeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val alarm_id: Long,
    val isCompleted: Boolean,
    val task_type: String, // "SHAKE" or "BARCODE"
    val requiredShakes: Int?,
    val targetBarcodeId: Long?
)
