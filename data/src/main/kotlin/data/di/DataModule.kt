package data.di

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import data.db.AlarmDatabase
import data.repository.AlarmRepositoryImpl
import data.sensor.BarcodeSensorImpl
import data.sensor.ShakeSensorImpl
import android.hardware.SensorManager
import domain.repository.IAlarmRepository
import domain.repository.IBarcodeSensor
import domain.repository.IShakeSensor


// Без кринжовых LLM коментов
object DataModule {

    fun provideRepository(context: Context): IAlarmRepository {
        val db = AlarmDatabase.getInstance(context)
        return AlarmRepositoryImpl(db.alarmDao())
    }

    fun provideShakeSensor(context: Context): IShakeSensor {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return ShakeSensorImpl(sensorManager)
    }

    fun provideBarcodeScanner(context: Context, lifecycleOwner: LifecycleOwner): IBarcodeSensor {
        return BarcodeSensorImpl(context, lifecycleOwner)
    }
}