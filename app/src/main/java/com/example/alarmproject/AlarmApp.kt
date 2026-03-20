package com.example.alarmproject

import android.app.Application
import android.util.Log
import com.example.alarmproject.di.AppModule
import com.example.alarmproject.service.AlarmService
import presentation.di.PresentationDependencies

class AlarmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
        PresentationDependencies.init(
            alarmSetupViewModelFactory = AppModule.provideAlarmSetupViewModelFactory(),
            alarmListViewModelFactory = AppModule.provideAlarmListViewModelFactory(),
            provideShakeSensor = { AppModule.provideShakeSensor() },
            provideBarcodeSensor = { lifecycleOwner -> AppModule.provideBarcodeSensor(lifecycleOwner) },
            stopAlarmService = { 
                Log.d("ALARM_DEBUG", "AlarmApp: stopAlarmService delegate called")
                AlarmService.stop(this) 
            }
        )
    }
}
