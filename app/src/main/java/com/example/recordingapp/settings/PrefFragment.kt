package com.example.recordingapp.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.recordingapp.R


class PrefFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)
    }

    companion object {
    }
}