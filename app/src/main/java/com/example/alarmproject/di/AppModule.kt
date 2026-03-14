package com.example.alarmproject.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.alarmproject.receiver.AlarmReceiver
import data.di.DataModule
import data.scheduler.AndroidAlarmScheduler
import domain.repository.IAlarmRepository
import domain.scheduler.IAlarmScheduler
import domain.usecases.CreateAlarmUseCase
import domain.usecases.DeleteAlarmUseCase
import domain.usecases.GetAlarmsUseCase
import presentation.viewmodels.AlarmListViewModel
import presentation.viewmodels.AlarmSetupViewModel

/**
 * Модуль зависимостей приложения (заменяет MockModule).
 *
 * Инициализируйте через AppModule.init(context) в Application.onCreate().
 * Все зависимости создаются лениво и кешируются как синглтоны.
 */
object AppModule {

    private lateinit var appContext: Context

    /** Вызывать один раз из AlarmApp.onCreate(). */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    // Планировщик через AlarmManager
    private val scheduler: IAlarmScheduler by lazy {
        AndroidAlarmScheduler(appContext, AlarmReceiver::class.java)
    }

    // Репозиторий с реальной Room-базой (AlarmDatabase скрыта внутри :data)
    private val repository: IAlarmRepository by lazy {
        DataModule.provideRepository(appContext)
    }

    // Use cases
    private val createAlarmUseCase: CreateAlarmUseCase by lazy {
        CreateAlarmUseCase(repository, scheduler)
    }

    private val getAlarmsUseCase: GetAlarmsUseCase by lazy {
        GetAlarmsUseCase(repository)
    }

    private val deleteAlarmUseCase: DeleteAlarmUseCase by lazy {
        DeleteAlarmUseCase(repository, scheduler)
    }

    fun provideAlarmSetupViewModelFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AlarmSetupViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AlarmSetupViewModel(createAlarmUseCase) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

    fun provideAlarmListViewModelFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AlarmListViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AlarmListViewModel(getAlarmsUseCase, deleteAlarmUseCase) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
}
