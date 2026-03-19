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

    /** Возвращает будильник по id. */
    @Query("SELECT * FROM alarms WHERE id = :id")
    suspend fun getById(id: Long): AlarmEntity?
    //Новые операции для штрих-кода
    /** Сохраняет отсканированный штрих-код. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedBarcode(barcode: SavedBarcodeEntity): Long

    /** Возвращает ранее сохраненные штрих-коды (сначала новые). */
    @Query("SELECT * FROM saved_barcodes ORDER BY createdAt DESC")
    fun getSavedBarcodes(): Flow<List<SavedBarcodeEntity>>

    /** Ищет уже сохраненный штрих-код по значению. */
    @Query("SELECT * FROM saved_barcodes WHERE codeValue = :codeValue LIMIT 1")
    suspend fun findSavedBarcodeByValue(codeValue: String): SavedBarcodeEntity?
}
