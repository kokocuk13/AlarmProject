package domain.models

import java.time.LocalTime

data class Alarm(
    val time: LocalTime,
    val isEnabled: Boolean,
    val task: DismissTask,
    val name: String? = null
)
