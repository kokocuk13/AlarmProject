package data.sensor

import android.content.Context
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import domain.repository.IBarcodeSensor
import java.util.concurrent.Executors

@androidx.camera.core.ExperimentalGetImage
class BarcodeSensorImpl(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : IBarcodeSensor {

    private var onScanned: ((String) -> Unit)? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val executor = Executors.newSingleThreadExecutor()

    override fun start(onScanned: (String) -> Unit) {
        this.onScanned = onScanned

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                processImage(imageProxy)
            }

            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                imageAnalysis
            )

        }, ContextCompat.getMainExecutor(context))
    }

    override fun stop() {
        cameraProvider?.unbindAll()
        onScanned = null
        executor.shutdownNow()
    }

    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        val scanner = BarcodeScanning.getClient()

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull { it.rawValue != null }?.let {
                    onScanned?.invoke(it.rawValue!!)
                }
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}