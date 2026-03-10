package domain.models
import java.time.LocalTime

data class Alarm(
    val time: LocalTime,
    val isEnabled: Boolean,
    val task: DismissTask
)

abstract class DismissTask(open val isCompleted: Boolean = false)
data class ShakeTask(val requiredShakes: Int) : DismissTask()