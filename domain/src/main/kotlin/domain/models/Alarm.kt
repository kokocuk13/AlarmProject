package domain.models

import java.time.LocalTime

data class Alarm(
    val id: Long = 0L,
    val time: LocalTime,
    val isEnabled: Boolean,
    val task: DismissTask,
    val name: String? = null,
    val days: List<Int> = emptyList()
)
