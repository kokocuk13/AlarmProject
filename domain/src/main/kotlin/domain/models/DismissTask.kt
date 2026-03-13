package domain.models

abstract class DismissTask(
    open val isCompleted: Boolean = false
)

data class ShakeTask(
    val requiredShakes: Int,
    override val isCompleted: Boolean = false
) : DismissTask(isCompleted)
