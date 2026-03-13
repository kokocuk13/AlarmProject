package com.example.alarmproject

import android.app.Application
import com.example.alarmproject.di.AppModule

/** Application-класс. Инициализирует модуль зависимостей при старте приложения. */
class AlarmApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppModule.init(this)
    }
}
