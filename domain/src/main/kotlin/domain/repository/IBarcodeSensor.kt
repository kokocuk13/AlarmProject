package domain.repository

interface IBarcodeSensor {
    fun start(onScanned: (String) -> Unit)
    fun stop()
}