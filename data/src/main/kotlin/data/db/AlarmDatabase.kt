package data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room-база данных приложения.
 * Синглтон — используйте AlarmDatabase.getInstance(context) для получения экземпляра.
 */
@Database(entities = [AlarmEntity::class, SavedBarcodeEntity::class], version = 2, exportSchema = false) // version 2 — добавлено поле requiredBarcode для поддержки будильников с задачей по штрих-коду и saveBarcode добавил сущность для сохранения отсканированных штрих-кодов
abstract class AlarmDatabase : RoomDatabase() {

    abstract fun alarmDao(): AlarmDao

    companion object {
        @Volatile
        private var INSTANCE: AlarmDatabase? = null

        fun getInstance(context: Context): AlarmDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AlarmDatabase::class.java,
                    "alarm_database"
                )
                    .fallbackToDestructiveMigration()
                    .build().also { INSTANCE = it }
            }
        }
    }
}
