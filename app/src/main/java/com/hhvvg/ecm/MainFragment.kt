package com.hhvvg.ecm

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat

/**
 * @author hhvvg
 */
class MainFragment : PreferenceFragmentCompat() {
    private lateinit var enableSwitchPreference: SwitchPreferenceCompat
    private val extService by lazy {
        requireContext().getSystemExtClipboardService()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.main_prefs, rootKey)
        enableSwitchPreference = findPreference("enable_management")!!
        enableSwitchPreference.isChecked = extService?.isEnable ?: false

        setListeners()
    }

    private fun setListeners() {
        enableSwitchPreference.setOnPreferenceChangeListener { _, newValue ->
            extService?.let {
                it.isEnable = newValue as Boolean
            }
            true
        }
    }
}