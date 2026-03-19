package com.example.alarmproject.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import com.example.alarmproject.R

class MainActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_TYPE = "extra_task_type"
        const val EXTRA_REQUIRED_SHAKES = "extra_required_shakes"
        const val TASK_SHAKE = "shake"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "AlarmApp"

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navHostFragment.navController
    }
}