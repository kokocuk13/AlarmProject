package com.example.alarmproject

import android.app.Application
import com.example.alarmproject.di.AppModule
import presentation.di.PresentationDependencies

class AlarmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
        PresentationDependencies.init(
            alarmSetupViewModelFactory = AppModule.provideAlarmSetupViewModelFactory(),
            alarmListViewModelFactory = AppModule.provideAlarmListViewModelFactory(),
            provideShakeSensor = { AppModule.provideShakeSensor() },
            provideBarcodeSensor = { lifecycleOwner -> AppModule.provideBarcodeSensor(lifecycleOwner) }
        )
    }
}
