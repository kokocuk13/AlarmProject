package data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room-сущность для хранения будильника в SQLite.
 * Маппируется в/из domain.models.Alarm через расширения в AlarmRepositoryImpl.
 * days — строка с номерами дней через запятую, например "0,1,2"
 * taskType — "SHAKE" или "BARCODE"
 */
@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean,
    val name: String?,
    val requiredShakes: Int,
    val taskType: String = "SHAKE",
    val requiredBarcode: String? = null,
    val days: String = ""
)
