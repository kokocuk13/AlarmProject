package com.example.alarmproject.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.example.alarmproject.R
import presentation.ui.AlarmRingingFragment

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_TYPE = "extra_task_type"
        const val EXTRA_REQUIRED_SHAKES = "extra_required_shakes"
        const val EXTRA_REQUIRED_BARCODE = "extra_required_barcode"
        const val TASK_SHAKE = "shake"
        const val TASK_BARCODE = "barcode"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "AlarmApp"

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
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val taskType = intent?.getStringExtra(EXTRA_TASK_TYPE) ?: return
        
        val alarmId = intent.getLongExtra("ALARM_ID", -1L)
        val alarmName = intent.getStringExtra("ALARM_NAME") ?: "Будильник"
        val hour = intent.getIntExtra("ALARM_HOUR", 0)
        val minute = intent.getIntExtra("ALARM_MINUTE", 0)
        val melodyUri = intent.getStringExtra("ALARM_MELODY_URI")

        val bundle = bundleOf(
            "ALARM_ID" to alarmId,
            "name" to alarmName,
            "hour" to hour,
            "minute" to minute,
            AlarmRingingFragment.ARG_TASK_TYPE to taskType,
            AlarmRingingFragment.ARG_REQUIRED_SHAKES to intent.getIntExtra(EXTRA_REQUIRED_SHAKES, 20),
            AlarmRingingFragment.ARG_REQUIRED_BARCODE to intent.getStringExtra(EXTRA_REQUIRED_BARCODE),
            "ALARM_MELODY_URI" to melodyUri
        )

        navController.navigate(
            presentation.R.id.alarmRingingFragment,
            bundle
        )
    }

    /**
     * Метод для вызова из фрагментов через requireActivity()
     */
    fun dismissAlarmNotification(alarmId: Long) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(alarmId.toInt())
    }
}
