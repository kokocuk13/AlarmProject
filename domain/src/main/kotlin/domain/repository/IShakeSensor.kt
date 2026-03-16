package domain.repository

interface IShakeSensor {
    fun start(onShake: () -> Unit)
    fun stop()
}