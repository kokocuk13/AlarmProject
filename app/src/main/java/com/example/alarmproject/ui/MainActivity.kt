package com.example.alarmproject.ui

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.navigation.fragment.NavHostFragment
import com.example.alarmproject.R
import com.example.alarmproject.service.AlarmService
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
        
        // 1. Пытаемся достать данные из Intent (если пришли из уведомления)
        var taskType = intent?.getStringExtra(EXTRA_TASK_TYPE)
        var alarmId = intent?.getLongExtra("ALARM_ID", -1L) ?: -1L
        var alarmName = intent?.getStringExtra("ALARM_NAME")
        var hour = intent?.getIntExtra("ALARM_HOUR", 0) ?: 0
        var minute = intent?.getIntExtra("ALARM_MINUTE", 0) ?: 0
        var melodyUri = intent?.getStringExtra("ALARM_MELODY_URI")
        var shakes = intent?.getIntExtra(EXTRA_REQUIRED_SHAKES, 20) ?: 20
        var barcode = intent?.getStringExtra(EXTRA_REQUIRED_BARCODE)

        // 2. Если Intent пустой, проверяем, не звонит ли сейчас будильник в сервисе
        if (taskType == null) {
            val serviceData = AlarmService.currentAlarmData
            if (serviceData != null) {
                Log.d("ALARM_DEBUG", "MainActivity: Pulling data from running AlarmService")
                taskType = serviceData.taskType
                alarmId = serviceData.id
                alarmName = serviceData.name
                hour = serviceData.hour
                minute = serviceData.minute
                melodyUri = serviceData.melodyUri
                shakes = serviceData.requiredShakes ?: 20
                barcode = serviceData.requiredBarcode
            }
        }

        // Если данных все еще нет — значит мы просто открыли приложение, ничего не делаем
        if (taskType == null) return

        val bundle = bundleOf(
            "ALARM_ID" to alarmId,
            "name" to (alarmName ?: "Будильник"),
            "hour" to hour,
            "minute" to minute,
            AlarmRingingFragment.ARG_TASK_TYPE to taskType,
            AlarmRingingFragment.ARG_REQUIRED_SHAKES to shakes,
            AlarmRingingFragment.ARG_REQUIRED_BARCODE to barcode,
            "ALARM_MELODY_URI" to melodyUri
        )

        // Переходим на экран звонка
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
