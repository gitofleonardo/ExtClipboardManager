package com.hhvvg.ecm.ui.model

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppsModel {
    suspend fun loadPackages(context: Context): List<ApplicationInfo> = withContext(context = Dispatchers.IO) {
        val pm = context.packageManager
        return@withContext pm.getInstalledApplications(PackageManager.GET_META_DATA)
    }
}
