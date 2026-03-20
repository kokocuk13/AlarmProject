package presentation.di

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModelProvider
import domain.repository.IBarcodeSensor
import domain.repository.IShakeSensor

object PresentationDependencies {
    lateinit var alarmSetupViewModelFactory: ViewModelProvider.Factory
        private set

    lateinit var alarmListViewModelFactory: ViewModelProvider.Factory
        private set

    lateinit var provideShakeSensor: () -> IShakeSensor
        private set

    lateinit var provideBarcodeSensor: (LifecycleOwner) -> IBarcodeSensor
        private set

    var stopAlarmService: (() -> Unit)? = null

    fun init(
        alarmSetupViewModelFactory: ViewModelProvider.Factory,
        alarmListViewModelFactory: ViewModelProvider.Factory,
        provideShakeSensor: () -> IShakeSensor,
        provideBarcodeSensor: (LifecycleOwner) -> IBarcodeSensor,
        stopAlarmService: () -> Unit
    ) {
        this.alarmSetupViewModelFactory = alarmSetupViewModelFactory
        this.alarmListViewModelFactory = alarmListViewModelFactory
        this.provideShakeSensor = provideShakeSensor
        this.provideBarcodeSensor = provideBarcodeSensor
        this.stopAlarmService = stopAlarmService
    }
}