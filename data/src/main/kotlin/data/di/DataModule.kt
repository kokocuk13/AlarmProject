package data.di

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import data.db.AlarmDatabase
import data.repository.AlarmRepositoryImpl
import data.repository.SavedBarcodeRepositoryImpl
import data.sensor.BarcodeSensorImpl
import data.sensor.ShakeSensorImpl
import android.hardware.SensorManager
import domain.repository.IAlarmRepository
import domain.repository.IBarcodeSensor
import domain.repository.IShakeSensor
import domain.repository.ISavedBarcodeRepository


// Без кринжовых LLM коментов
object DataModule {

    fun provideRepository(context: Context): IAlarmRepository {
        val db = AlarmDatabase.getInstance(context)
        return AlarmRepositoryImpl(db.alarmDao())
    }

    fun provideSavedBarcodeRepository(context: Context): ISavedBarcodeRepository { // Это для сохранения отсканированных штрих-кодов, чтобы потом их можно было выбрать при создании будильника с задачей по штрих-коду. Реализовано через отдельную таблицу в БД и отдельный репозиторий, чтобы не засорять основной репозиторий будильников и не усложнять его интерфейс методами, не относящимися к будильникам напрямую.
        val db = AlarmDatabase.getInstance(context)
        return SavedBarcodeRepositoryImpl(db.alarmDao())
    }

    fun provideShakeSensor(context: Context): IShakeSensor {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return ShakeSensorImpl(sensorManager)
    }

    fun provideBarcodeScanner(context: Context, lifecycleOwner: LifecycleOwner): IBarcodeSensor {
        return BarcodeSensorImpl(context, lifecycleOwner)
    }
}