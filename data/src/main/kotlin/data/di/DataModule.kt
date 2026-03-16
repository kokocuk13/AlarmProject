package data.di

import android.content.Context
import data.db.AlarmDatabase
import data.repository.AlarmRepositoryImpl
import domain.repository.IAlarmRepository

/**
 * Фабрика зависимостей слоя данных.
 *
 * Скрывает детали реализации (AlarmDatabase, AlarmRepositoryImpl) от внешних модулей.
 * Внешний код работает только с интерфейсом [IAlarmRepository].
 */
object DataModule {

    /**
     * Создаёт (или возвращает существующий) репозиторий будильников.
     *
     * @param context контекст приложения (applicationContext).
     */
    fun provideRepository(context: Context): IAlarmRepository {
        val db = AlarmDatabase.getInstance(context)
        return AlarmRepositoryImpl(db.alarmDao())
    }
}

