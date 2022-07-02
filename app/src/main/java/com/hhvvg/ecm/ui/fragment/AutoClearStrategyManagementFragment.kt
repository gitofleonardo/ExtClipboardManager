package com.hhvvg.ecm.ui.fragment

import android.os.Bundle
import android.text.InputType
import androidx.lifecycle.lifecycleScope
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceScreen
import com.hhvvg.ecm.R
import com.hhvvg.ecm.ui.view.InputBottomSheetDialog
import com.hhvvg.ecm.util.getSystemExtClipboardService
import kotlinx.coroutines.launch

class AutoClearStrategyManagementFragment : PreferenceFragmentCompat() {
    private val workModePref by lazy {
        findPreference<ListPreference>("work_mode_key")!!
    }
    private val readCountPref by lazy {
        findPreference<Preference>("auto_clear_count_key")!!
    }
    private val exclusionPref by lazy {
        findPreference<Preference>("auto_clear_exclude")!!
    }
    private val whitelistPref by lazy {
        findPreference<Preference>("whitelist_key")!!
    }
    private val blacklistPref by lazy {
        findPreference<Preference>("blacklist_key")!!
    }
    private val service by lazy {
        requireContext().getSystemExtClipboardService()
    }
    private var workMode: Int
        get() = service?.autoClearWorkMode ?: 0
        set(value) {
            service?.autoClearWorkMode = value
        }
    private var readCount: Int
        get() = service?.autoClearReadCount ?: 1
        set(value) {
            service?.autoClearReadCount = value
        }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.auto_clear_strategy_pref, rootKey)
        if (service == null) {
            findPreference<PreferenceScreen>("root_key")?.isEnabled = false
            return
        }
        setupWorkModePref()
        setupReadCountPref()
    }

    private fun setupReadCountPref() {
        readCountPref.summary = getString(R.string.auto_clear_read_count_summary, "$readCount")
        readCountPref.setOnPreferenceClickListener {
            val dialog = InputBottomSheetDialog.Builder(requireContext())
                .setMaxLines(1)
                .setTitle(getString(R.string.auto_clear_read_count_title))
                .setInputType(InputType.TYPE_CLASS_NUMBER)
                .setHint(getString(R.string.auto_clear_read_count_hint))
                .setOnCancelResult("")
                .build()
            lifecycleScope.launch {
                val result = dialog.showDialog().toString().toIntOrNull() ?: -1
                if (result > 0) {
                    readCount = result
                    readCountPref.summary = getString(R.string.auto_clear_read_count_summary, "$readCount")
                }
            }
            true
        }
    }

    private fun setupWorkModePref() {
        val arr = resources.getStringArray(R.array.work_mode_entry_keys)
        if (workMode in 0..1) {
            workModePref.setDefaultValue(arr[workMode])
        }
        updateWorkModePref()
        workModePref.setOnPreferenceChangeListener { _, newValue ->
            val value = newValue.toString().toIntOrNull() ?: 0
            workMode = value
            updateWorkModePref()
            true
        }
    }

    private fun updateWorkModePref() {
        when(workMode) {
            0 -> {
                whitelistPref.isVisible = true
                blacklistPref.isVisible = false
            }
            1 -> {
                whitelistPref.isVisible = false
                blacklistPref.isVisible = true
            }
        }
    }
}