package com.example.alarmproject.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.alarmproject.di.AppModule
import com.example.alarmproject.service.AlarmService
import com.example.alarmproject.ui.MainActivity
import domain.models.BarcodeTask
import domain.models.ShakeTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver, вызываемый AlarmManager при срабатывании будильника.
 * Теперь он запускает AlarmService для воспроизведения музыки и показа уведомления.
 */
class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("ALARM_ID", -1L)
        Log.d("ALARM_DEBUG", "AlarmReceiver: Received broadcast for ID: $alarmId")
        
        if (alarmId == -1L) {
            Log.e("ALARM_DEBUG", "AlarmReceiver: Error - ALARM_ID is -1")
            return
        }

        val pendingResult = goAsync()
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("ALARM_DEBUG", "AlarmReceiver: Fetching alarm data from DB for ID: $alarmId")
                val result = AppModule.repository.getAlarmById(alarmId)
                val alarm = result.getOrNull()

                if (alarm != null) {
                    Log.d("ALARM_DEBUG", "AlarmReceiver: Alarm found: ${alarm.name}. Starting AlarmService.")
                    
                    val taskType = when (alarm.task) {
                        is ShakeTask -> MainActivity.TASK_SHAKE
                        is BarcodeTask -> MainActivity.TASK_BARCODE
                        else -> MainActivity.TASK_SHAKE
                    }
                    val shakes = (alarm.task as? ShakeTask)?.requiredShakes
                    val barcode = (alarm.task as? BarcodeTask)?.requiredBarcode

                    AlarmService.start(
                        context = context,
                        alarmId = alarm.id,
                        name = alarm.name ?: "Будильник",
                        hour = alarm.time.hour,
                        minute = alarm.time.minute,
                        melodyUri = alarm.melodyUri,
                        taskType = taskType,
                        shakes = shakes,
                        barcode = barcode
                    )
                } else {
                    Log.e("ALARM_DEBUG", "AlarmReceiver: Alarm not found in DB for ID: $alarmId")
                }
            } catch (e: Exception) {
                Log.e("ALARM_DEBUG", "AlarmReceiver: Error processing alarm", e)
            } finally {
                pendingResult.finish()
                Log.d("ALARM_DEBUG", "AlarmReceiver: Finished processing")
            }
        }
    }
}
