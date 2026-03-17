package com.example.alarmproject.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.example.alarmproject.R

class MainActivity : AppCompatActivity() {

    private lateinit var navHostFragment: NavHostFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        if (savedInstanceState == null) {
            handleAlarmIntent(intent)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAlarmIntent(intent)
    }

    private fun handleAlarmIntent(intent: Intent?) {
        val navController = navHostFragment.navController
        val taskType = intent?.getStringExtra(EXTRA_TASK_TYPE) ?: return

        when (taskType.uppercase()) {
            TASK_SHAKE -> {
                val requiredShakes = intent.getIntExtra(EXTRA_REQUIRED_SHAKES, 20)
                navController.navigate(
                    presentation.R.id.shakeTaskProgressFragment,
                    bundleOf("required_shakes" to requiredShakes)
                )
            }

            TASK_BARCODE -> {
                val requiredBarcode = intent.getStringExtra(EXTRA_REQUIRED_BARCODE)
                navController.navigate(
                    presentation.R.id.barcodeScanFragment,
                    bundleOf("required_barcode" to requiredBarcode)
                )
            }
        }
    }

    companion object {
        const val EXTRA_TASK_TYPE = "TASK_TYPE"
        const val EXTRA_REQUIRED_SHAKES = "REQUIRED_SHAKES"
        const val EXTRA_REQUIRED_BARCODE = "REQUIRED_BARCODE"

        const val TASK_SHAKE = "SHAKE"
        const val TASK_BARCODE = "BARCODE"
    }
}
