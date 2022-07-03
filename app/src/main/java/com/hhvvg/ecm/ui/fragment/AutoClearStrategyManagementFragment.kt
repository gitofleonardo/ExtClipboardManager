package com.hhvvg.ecm.ui.fragment

import android.os.Bundle
import android.text.InputType
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
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
    private val navController by lazy {
        findNavController()
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.auto_clear_strategy_pref, rootKey)
        if (service == null) {
            findPreference<PreferenceScreen>("root_key")?.isEnabled = false
            return
        }
        setupWorkModePref()
        setupReadCountPref()
        setupContentExclusionPref()
        setupListPref()
    }

    private fun setupListPref() {
        blacklistPref.setOnPreferenceClickListener {
            openModeListFragment()
            true
        }
        whitelistPref.setOnPreferenceClickListener {
            openModeListFragment()
            true
        }
    }

    private fun openModeListFragment() {
        val action = AutoClearStrategyManagementFragmentDirections.actionAutoClearStrategyFragmentToWorkModeListFragment()
        navController.navigate(action)
    }

    private fun setupContentExclusionPref() {
        exclusionPref.setOnPreferenceClickListener {
            val action = AutoClearStrategyManagementFragmentDirections.actionAutoClearStrategyFragmentToAutoClearExclusionListFragment()
            navController.navigate(action)
            true
        }
    }

    private fun setupReadCountPref() {
        readCountPref.summary = getString(R.string.auto_clear_read_count_summary, "$readCount")
        readCountPref.setOnPreferenceClickListener {
            val dialog = InputBottomSheetDialog.Builder(requireContext())
                .setMaxLines(1)
                .setTitle(getString(R.string.auto_clear_read_count_title))
                .setInputType(InputType.TYPE_CLASS_NUMBER)
                .setHint(getString(R.string.auto_clear_read_count_hint))
                .setText("$readCount")
                .build()
            lifecycleScope.launch {
                when(val result = dialog.showDialog()) {
                    is InputBottomSheetDialog.ActionResult.ConfirmResult -> {
                        val count = result.result.toString().toIntOrNull()
                        if (count != null && count > 0) {
                            readCount = count
                            readCountPref.summary = getString(R.string.auto_clear_read_count_summary, "$count")
                        }
                    }
                    else -> {
                        // Do nothing
                    }
                }
            }
            true
        }
    }

    private fun setupWorkModePref() {
        if (workMode in 0..1) {
            workModePref.value = "$workMode"
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