package data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/** DAO для CRUD-операций с будильниками в Room. */
@Dao
interface AlarmDao {

    /** Вставляет будильник и возвращает сгенерированный id. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: AlarmEntity): Long

    /** Возвращает живой поток всех будильников, отсортированных по времени. */
    @Query("SELECT * FROM alarms ORDER BY hour ASC, minute ASC")
    fun getAllAlarms(): Flow<List<AlarmEntity>>

    /** Удаляет будильник по id. */
    @Query("DELETE FROM alarms WHERE id = :id")
    suspend fun deleteById(id: Long)
}
