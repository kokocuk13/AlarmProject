package data.sensor

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import domain.repository.IShakeSensor
import kotlin.math.sqrt

class ShakeSensorImpl(
    private val sensorManager: SensorManager
) : IShakeSensor, SensorEventListener {

    private var onShake: (() -> Unit)? = null
    private var lastTime = 0L

    override fun start(onShake: () -> Unit) {
        this.onShake = onShake
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) ?: return
        sensorManager.registerListener(
            this,
            accelerometer,
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun stop() {
        sensorManager.unregisterListener(this)
        onShake = null
    }

    override fun onSensorChanged(event: SensorEvent) {
        val now = System.currentTimeMillis()
        if (now - lastTime < 300) return

        val (x, y, z) = event.values
        // Акселерометр всегда "чувствует"(как же он чувствует) гравитацию, поэтому вычитаем для получения чистой
        val acceleration = sqrt(x * x + y * y + z * z) - SensorManager.GRAVITY_EARTH

        // Сильнее 20 метров в секунду
        if (acceleration > 20f) {
            lastTime = now
            onShake?.invoke()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}