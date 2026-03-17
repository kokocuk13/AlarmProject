package domain.models

data class ShakeTask(
    val requiredShakes: Int,
    override val isCompleted: Boolean = false
) : DismissTask(isCompleted)