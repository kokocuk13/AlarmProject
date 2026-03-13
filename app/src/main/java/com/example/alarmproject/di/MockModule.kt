package com.example.alarmproject.di

import domain.models.Alarm
import domain.repository.IAlarmRepository
import domain.scheduler.IAlarmScheduler
import domain.usecases.CreateAlarmUseCase
import presentation.viewmodels.AlarmSetupViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object MockModule {
    private val repository = object : IAlarmRepository {
        override suspend fun saveAlarm(alarm: Alarm): Result<Unit> {
            println("Mock: Saving alarm at ${alarm.time}")
            return Result.success(Unit)
        }
    }

    private val scheduler = object : IAlarmScheduler {
        override fun schedule(alarm: Alarm) {
            println("Mock: Scheduling alarm at ${alarm.time}")
        }
        override fun cancel(alarm: Alarm) {
            println("Mock: Cancelling alarm")
        }
    }

    private val createAlarmUseCase = CreateAlarmUseCase(repository, scheduler)

    fun provideAlarmSetupViewModelFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AlarmSetupViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AlarmSetupViewModel(createAlarmUseCase) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
