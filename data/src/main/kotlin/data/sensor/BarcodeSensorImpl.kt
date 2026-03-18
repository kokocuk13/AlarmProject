package data.sensor

import android.content.Context
import android.view.ViewGroup
import androidx.camera.core.ExperimentalGetImage
import androidx.annotation.OptIn
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import domain.repository.IBarcodeSensor
import java.util.concurrent.Executors

class BarcodeSensorImpl(
    private val context: Context,
    private val lifecycleOwner: LifecycleOwner
) : IBarcodeSensor {

    private var onScanned: ((String) -> Unit)? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private val executor = Executors.newSingleThreadExecutor()
    private val scanner = BarcodeScanning.getClient()

    override fun start(onScanned: (String) -> Unit) {
        this.onScanned = onScanned

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)

        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            val preview = Preview.Builder().build()
            
            // PreviewView создается программно, чтобы layout не зависел от класса CameraX.
            val previewView = if (lifecycleOwner is Fragment) {
                val container = lifecycleOwner.view?.findViewById<ViewGroup>(
                    context.resources.getIdentifier(
                        "cameraPreviewContainer",
                        "id",
                        lifecycleOwner.requireContext().packageName
                    )
                )
                if (container == null) {
                    null
                } else {
                    val existing = container.getChildAt(0) as? PreviewView
                    existing ?: PreviewView(container.context).also {
                        container.removeAllViews()
                        container.addView(
                            it,
                            ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        )
                    }
                }
            } else {
                null
            }
            
            previewView?.let { preview.setSurfaceProvider(it.surfaceProvider) }

            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                processImage(imageProxy)
            }

            cameraProvider?.unbindAll()

            cameraProvider?.bindToLifecycle(
                lifecycleOwner,
                CameraSelector.DEFAULT_BACK_CAMERA,
                preview,
                imageAnalysis
            )

        }, ContextCompat.getMainExecutor(context))
    }

    override fun stop() {
        cameraProvider?.unbindAll()
        scanner.close()
        onScanned = null
        executor.shutdownNow()
    }

    @OptIn(markerClass = [ExperimentalGetImage::class])
    private fun processImage(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

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