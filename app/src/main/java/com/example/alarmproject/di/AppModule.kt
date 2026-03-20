package com.example.alarmproject.di

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.alarmproject.receiver.AlarmReceiver
import data.di.DataModule
import data.scheduler.AndroidAlarmScheduler
import domain.repository.IAlarmRepository
import domain.repository.IBarcodeSensor
import domain.repository.IShakeSensor
import domain.repository.ISavedBarcodeRepository
import domain.scheduler.IAlarmScheduler
import domain.usecases.CreateAlarmUseCase
import domain.usecases.DeleteAlarmUseCase
import domain.usecases.GetAlarmsUseCase
import domain.usecases.GetSavedBarcodesUseCase
import domain.usecases.SaveScannedBarcodeUseCase
import presentation.viewmodels.AlarmListViewModel
import presentation.viewmodels.AlarmSetupViewModel

object AppModule {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val scheduler: IAlarmScheduler by lazy {
        AndroidAlarmScheduler(appContext, AlarmReceiver::class.java)
    }

    internal val repository: IAlarmRepository by lazy {
        DataModule.provideRepository(appContext)
    }

    private val savedBarcodeRepository: ISavedBarcodeRepository by lazy {
        DataModule.provideSavedBarcodeRepository(appContext)
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

    private val getSavedBarcodesUseCase: GetSavedBarcodesUseCase by lazy {
        GetSavedBarcodesUseCase(savedBarcodeRepository)
    }

    private val saveScannedBarcodeUseCase: SaveScannedBarcodeUseCase by lazy {
        SaveScannedBarcodeUseCase(savedBarcodeRepository)
    }

    fun provideShakeSensor(): IShakeSensor =
        DataModule.provideShakeSensor(appContext)

    fun provideBarcodeSensor(lifecycleOwner: LifecycleOwner): IBarcodeSensor =
        DataModule.provideBarcodeScanner(appContext, lifecycleOwner)

    fun provideAlarmSetupViewModelFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(AlarmSetupViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return AlarmSetupViewModel(
                        createAlarmUseCase,
                        getAlarmsUseCase,
                        getSavedBarcodesUseCase,
                        saveScannedBarcodeUseCase
                    ) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }

    fun provideAlarmListViewModelFactory(): ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return AlarmListViewModel(getAlarmsUseCase, deleteAlarmUseCase) as T
            }
        }
}