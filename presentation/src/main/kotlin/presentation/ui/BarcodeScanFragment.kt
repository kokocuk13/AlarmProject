package presentation.ui

import android.Manifest
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import domain.repository.IBarcodeSensor
import presentation.R
import presentation.di.PresentationDependencies

class BarcodeScanFragment : Fragment() {

    private var barcodeSensor: IBarcodeSensor? = null
    private var expectedBarcode: String? = null
    private var alarmId: Long = -1L
    private lateinit var statusText: TextView

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startScanning()
            } else {
                statusText.text = "Нужно разрешение для использования камеры."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        expectedBarcode = arguments?.getString(ARG_REQUIRED_BARCODE)
        alarmId = arguments?.getLong(AlarmRingingFragment.ARG_ALARM_ID, -1L) ?: -1L
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_barcode_scan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        statusText = view.findViewById(R.id.barcodeStatusText)

        if (hasCameraPermission()) {
            startScanning()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onStop() {
        super.onStop()
        barcodeSensor?.stop()
    }

    private fun startScanning() {
        statusText.text = "Камера запущена, наведите на штрих-код"
        barcodeSensor = PresentationDependencies.provideBarcodeSensor(this)
        barcodeSensor?.start { scannedValue ->
            if (!isAdded) return@start
            requireActivity().runOnUiThread {
                val expected = expectedBarcode
                val success = expected.isNullOrBlank() || expected == scannedValue
                if (success) {
                    barcodeSensor?.stop()

                    val launchedFromSetup =
                        findNavController().previousBackStackEntry?.destination?.id == R.id.alarmSetupFragment

                    if (!launchedFromSetup && alarmId != -1L) {
                        // Останавливаем сервис через делегат
                        PresentationDependencies.stopAlarmService?.invoke()

                        val notificationManager =
                            requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.cancel(alarmId.toInt())

                        findNavController().navigate(R.id.action_barcode_to_success)
                    } else {
                        findNavController().previousBackStackEntry
                            ?.savedStateHandle
                            ?.set(RESULT_SCANNED_BARCODE, scannedValue)
                        findNavController().navigateUp()
                    }
                } else {
                    statusText.text = "Неверный код: $scannedValue. Попробуйте еще раз."
                }
            }
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val ARG_REQUIRED_BARCODE = "required_barcode"
        const val RESULT_SCANNED_BARCODE = "result_scanned_barcode"
    }
}