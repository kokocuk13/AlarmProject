package com.example.alarmproject.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.alarmproject.R
import data.repository.AlarmRepositoryImpl
import data.scheduler.AndroidAlarmScheduler
import domain.usecases.CreateAlarmUseCase
import java.time.LocalTime
import kotlinx.coroutines.launch
import presentation.viewmodels.AlarmSetupViewModel
import presentation.viewmodels.AlarmUiState

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: AlarmSetupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Сборка зависимостей без DI-фреймворка.
        val repository = AlarmRepositoryImpl()
        val scheduler = AndroidAlarmScheduler()
        val useCase = CreateAlarmUseCase(repository, scheduler)
        viewModel = AlarmSetupViewModel(useCase)

        val statusText = findViewById<TextView>(R.id.statusText)
        val saveButton = findViewById<Button>(R.id.saveButton)

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                statusText.text = when (state) {
                    is AlarmUiState.Idle -> "Нажмите кнопку чтобы установить будильник"
                    is AlarmUiState.Loading -> "⏳ Сохранение..."
                    is AlarmUiState.Success -> "✅ Будильник установлен на ${LocalTime.now()}!"
                    is AlarmUiState.Error -> "❌ Ошибка: ${state.message}"
                }
            }
        }

        saveButton.setOnClickListener {
            viewModel.save(LocalTime.now(), 10)
        }
    }
}
