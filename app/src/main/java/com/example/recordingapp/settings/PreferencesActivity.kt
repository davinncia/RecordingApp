package com.example.recordingapp.settings

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.recordingapp.R

class PreferencesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preferences)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings_container, PrefFragment())
            .commit()
    }
}