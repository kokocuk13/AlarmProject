package domain.usecases

import domain.models.Alarm
import domain.repository.IAlarmRepository
import kotlinx.coroutines.flow.Flow

/** Возвращает живой поток (Flow) всех будильников из репозитория. */
class GetAlarmsUseCase(private val repository: IAlarmRepository) {
    operator fun invoke(): Flow<List<Alarm>> = repository.getAlarms()
}
