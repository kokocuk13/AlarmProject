package presentation.ui

import presentation.viewmodels.AlarmSetupViewModel
import presentation.viewmodels.AlarmUiState
import java.time.LocalTime

class AlarmSetupFragment(private val viewModel: AlarmSetupViewModel) {

    // Имитация нажатия кнопки "Сохранить" (clickSave() из диаграммы последовательности)
    fun onSaveClicked() {
        println("UI: Пользователь нажал 'Сохранить'")
        viewModel.save(LocalTime.now(), 10) // 10 встряхиваний
    }

    // Метод для "отрисовки" состояний
    fun observeState(state: AlarmUiState) {
        when (state) {
            is AlarmUiState.Loading -> {
                println("UI: [ПОКАЗЫВАЕМ PROGRESS BAR] Сохранение...")
            }
            is AlarmUiState.Success -> {
                println("UI: [SUCCESS] Будильник установлен! Закрываем экран.")
            }
            is AlarmUiState.Error -> {
                println("UI: [ОШИБКА] Сообщение: ${state.message}")
            }
            is AlarmUiState.Idle -> {
                println("UI: Экран готов к работе")
            }
        }
    }
}
