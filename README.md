# AlarmProject

Мобильное Android-приложение «умный будильник», который нельзя выключить случайно.
Сигнал отключается только после выполнения задания — встряхивания телефона или сканирования штрих-кода.
Это снижает риск повторного засыпания.

---

## Команда

| Участник | Роль |
|---|---|
| Глуховский Станислав | Тимлид, архитектура, интеграция |
| Самойлов Сергей | Splash и Onboarding экраны |
| Бондарев Матвей | Главный экран, список будильников |
| Срибный Фёдор | Экран настройки будильника |

---

## Стек технологий

| Слой | Технология |
|---|---|
| Язык | Kotlin |
| Архитектура | Clean Architecture (domain / data / presentation) |
| UI | Fragment + XML layouts |
| Навигация | Jetpack Navigation Component |
| ViewModel | AndroidX ViewModel + StateFlow |
| Асинхронность | Kotlin Coroutines |
| DI | Ручная сборка зависимостей (без фреймворка) |
| Моки | In-memory реализации репозитория и планировщика |

---

## Архитектура

Проект построен по принципу Clean Architecture с тремя слоями:

```
UI Layer (presentation)
      |
Domain Layer (domain)
      |
Data Layer (data)
```

### Диаграмма пакетов
- **UI Layer** — Fragments (AlarmSetupFragment) + ViewModels (AlarmSetupViewModel)
- **Domain Layer** — Models (Alarm, DismissTask, ShakeTask), Use Cases (CreateAlarmUseCase), интерфейсы IAlarmRepository, IAlarmScheduler
- **Data Layer** — Repository (AlarmRepositoryImpl), Scheduler (AndroidAlarmScheduler), локальные/камера/системные источники данных

### Диаграмма классов (ключевые связи)
- `AlarmSetupFragment` использует `AlarmSetupViewModel`
- `AlarmSetupViewModel` создаёт `CreateAlarmParams` и вызывает `CreateAlarmUseCase`
- `CreateAlarmUseCase` использует `IAlarmRepository` и `IAlarmScheduler`, создаёт `Alarm` с `ShakeTask`
- `AlarmRepositoryImpl` реализует `IAlarmRepository`, использует `AlarmDao`
- `AndroidAlarmScheduler` реализует `IAlarmScheduler`

### Диаграмма последовательностей (сценарий создания будильника)
1. Пользователь нажимает «Сохранить» — `AlarmSetupFragment.clickSave()`
2. Fragment вызывает `AlarmSetupViewModel.save()`
3. ViewModel вызывает `CreateAlarmUseCase.invoke(params)`
4. UseCase создаёт объект `Alarm(time, ShakeTask)`
5. UseCase вызывает `AlarmRepository.saveAlarm(alarm)`
6. Repository маппит в Entity и сохраняет через `AlarmDao.insert(entity)`
7. При ошибке: Repository бросает Exception — UseCase возвращает `Result.Error` — ViewModel вызывает `showError()` на Fragment
8. При успехе: Repository возвращает id — UseCase вызывает `IAlarmScheduler.schedule(alarm)` — ViewModel получает `Result.Success` — Fragment закрывается `closeScreen()`

---

## Структура проекта

```
AlarmProject/
├── app/                        # Точка входа, MainActivity, манифест
├── domain/                     # Бизнес-логика
│   └── src/main/kotlin/domain/
│       ├── models/             # Alarm, DismissTask, ShakeTask
│       ├── repository/         # IAlarmRepository
│       ├── scheduler/          # IAlarmScheduler
│       └── usecases/           # CreateAlarmUseCase, CreateAlarmParams
├── data/                       # Реализация (моки)
│   └── src/main/kotlin/data/
│       ├── repository/         # AlarmRepositoryImpl (мок)
│       └── scheduler/          # AndroidAlarmScheduler (мок)
└── presentation/               # UI слой
    └── src/main/kotlin/presentation/
        ├── ui/                 # SplashFragment, OnboardingFragment,
        │                       # AlarmListFragment, AlarmSetupFragment
        └── viewmodels/         # AlarmSetupViewModel, AlarmUiState
```

---

## Где находятся моки

Моковые реализации находятся в модуле `data/`:

**`data/src/main/kotlin/data/repository/AlarmRepositoryImpl.kt`**
Имитирует сохранение будильника с задержкой 1 секунда вместо реальной БД:
```kotlin
override suspend fun saveAlarm(alarm: Alarm): Result<Unit> {
    delay(1000) // имитация записи в БД
    return Result.success(Unit)
}
```

**`data/src/main/kotlin/data/scheduler/AndroidAlarmScheduler.kt`**
Мок планировщика — логирует вызов без реального AlarmManager.

Состояния UI (`AlarmUiState`) в `presentation/viewmodels/AlarmSetupViewModel.kt`:

| Состояние | Описание |
|---|---|
| `Idle` | Начальное состояние |
| `Loading` | Идёт сохранение (задержка 1 сек) |
| `Success` | Будильник успешно сохранён |
| `Error` | Ошибка сохранения |

---

## Реализованный end-to-end сценарий

### Создание будильника

Шаги для проверки:

1. Запустить приложение
2. **Splash** — логотип, автоматический переход (1.5 сек)
3. **Onboarding** — нажать кнопку «Начать» *(показывается только при первом запуске)*
4. **Home (пусто)** — экран с текстом «Добавьте свой первый будильник», нажать «+»
5. **Настройка будильника:**
   - Выбрать время (прокрутить часы и минуты)
   - Выбрать дни недели (нажать на кнопки ПН–ВС, выбранные подсвечиваются фиолетовым)
   - Выбрать способ отключения: «Трясти телефон» или «Штрих-код»
   - При выборе тряски — настроить количество встряхиваний слайдером (10–50)
   - Ввести название будильника
   - Нажать «Сохранить»
6. **Home (список)** — новый будильник появляется в списке с переключателем вкл/выкл

Ожидаемый результат: будильник отображается с указанным временем, названием и днями недели. Переключатель меняет цвет при включении/выключении.

---

## Как запустить приложение

### Требования
- Android Studio Hedgehog или новее
- JDK 17
- Android SDK 34
- Устройство или эмулятор с Android 8.0+ (API 26+)

### Шаги

```bash
# 1. Клонировать репозиторий
git clone https://github.com/kokocuk13/AlarmProject.git
cd AlarmProject

# 2. Переключиться на ветку develop
git checkout develop
```

3. Открыть в Android Studio: File -> Open -> папка AlarmProject
4. Дождаться синхронизации Gradle
5. Нажать Run (Shift+F10)

### Готовый APK
```
app/build/intermediates/apk/debug/app-debug.apk
```

---

## Ветки и распределение задач

| Ветка | Участник | Задача |
|---|---|---|
| `develop` | Глуховский Станислав | Интеграция, архитектура, навигация, README |
| `feature/splash-onboarding` | Самойлов Сергей | SplashFragment, OnboardingFragment |
| `feature/alarm-list` | Бондарев Матвей | AlarmListFragment, AlarmAdapter |
| `feature/alarm-setup` | Срибный Фёдор | AlarmSetupFragment, AlarmSetupViewModel |
