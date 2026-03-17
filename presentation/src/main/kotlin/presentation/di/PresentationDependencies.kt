package presentation.di

import androidx.lifecycle.ViewModelProvider

object PresentationDependencies {
    lateinit var alarmSetupViewModelFactory: ViewModelProvider.Factory
        private set

    lateinit var alarmListViewModelFactory: ViewModelProvider.Factory
        private set

    fun init(
        alarmSetupViewModelFactory: ViewModelProvider.Factory,
        alarmListViewModelFactory: ViewModelProvider.Factory
    ) {
        this.alarmSetupViewModelFactory = alarmSetupViewModelFactory
        this.alarmListViewModelFactory = alarmListViewModelFactory
    }
}
