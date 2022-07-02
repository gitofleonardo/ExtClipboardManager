package com.hhvvg.ecm.ui.viewmodel

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hhvvg.ecm.configuration.AutoClearStrategyInfo
import com.hhvvg.ecm.util.getSystemExtClipboardService
import com.hhvvg.ecm.ui.data.AutoClearAppItem
import com.hhvvg.ecm.ui.model.AppsModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AutoClearStrategyViewModel : ViewModel() {
    private val appsModel = AppsModel()

    val appItems = MutableLiveData<List<AutoClearAppItem>>()
    private val currentItems = ArrayList<AutoClearAppItem>()
    private var currentShowSystemPackages: Boolean = false
    private var currentFilterText = ""

    fun loadApps(context: Context) {
        val service = context.getSystemExtClipboardService()
        viewModelScope.launch {
            val apps = appsModel.loadPackages(context)
            val strategies = service?.autoClearStrategies ?: emptyList()
            val beforeFiltering = createAppItemList(context.packageManager, apps, strategies)
            currentItems.clear()
            currentItems.addAll(beforeFiltering)
            appItems.value = filter(currentItems, currentShowSystemPackages, currentFilterText)
        }
    }

    fun filter(showSystemPackages: Boolean? = null, filterText: String? = null) {
        if (showSystemPackages != null) {
            currentShowSystemPackages = showSystemPackages
        }
        if (filterText != null) {
            currentFilterText = filterText
        }
        viewModelScope.launch(Dispatchers.Default) {
            appItems.postValue(filter(currentItems, currentShowSystemPackages, currentFilterText))
        }
    }

    private fun filter(items: List<AutoClearAppItem>, showSystemPackages: Boolean, filterText: String): List<AutoClearAppItem> {
        return items.filter {
            val filterSystemPassed = if (showSystemPackages) {
                true
            } else {
                it.flags and ApplicationInfo.FLAG_SYSTEM == 0
            }
            val filterNamePassed = filterText.isEmpty() || it.appName.contains(filterText) || it.packageName.contains(filterText)
            filterSystemPassed && filterNamePassed
        }
    }

    private suspend fun createAppItemList(pm: PackageManager, apps: List<ApplicationInfo>, strategies: List<AutoClearStrategyInfo>): List<AutoClearAppItem> = withContext(Dispatchers.Default) {
        return@withContext apps.map {
            AutoClearAppItem(
                it.packageName,
                it.loadLabel(pm).toString(),
                it.loadIcon(pm),
                strategies.find { strategy->
                    strategy.packageName == it.packageName
                } ?: AutoClearStrategyInfo(it.packageName),
                it.flags
            )
        }
    }
}
