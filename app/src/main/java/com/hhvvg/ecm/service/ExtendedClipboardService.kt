package com.hhvvg.ecm.service

import android.annotation.TargetApi
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import com.hhvvg.ecm.BuildConfig
import com.hhvvg.ecm.ExtFramework.Companion.clipboardImplName
import com.hhvvg.ecm.IExtClipboardService
import com.hhvvg.ecm.configuration.AutoClearStrategyInfo
import com.hhvvg.ecm.configuration.Configuration
import com.hhvvg.ecm.configuration.ExtConfigurationStore
import com.hhvvg.ecm.util.asClass
import com.hhvvg.ecm.util.doAfter
import com.hhvvg.ecm.util.getField
import com.hhvvg.ecm.util.invokeMethod
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author hhvvg
 */
class ExtendedClipboardService(
    private val context: Context,
    private val realClipboardService: Any
) : IExtClipboardService.Stub() {
    companion object {
        const val bundleBinderKey = "ExtendedClipboardServiceBinder"
        const val intentBundleKey = "ExtendedClipboardServiceBundle"
        const val delayThreadName = "ExtendedClipboardServiceDelayThread"
    }

    private val mLock = realClipboardService.getField<Any>("mLock")

    private val dataStore by lazy {
        ExtConfigurationStore()
    }

    private val delayExecutor = ScheduledThreadPoolExecutor(1, DelayThreadFactory())
    private var currentClearTask: Runnable? = null
    private val currentCountDown = AtomicInteger(0)

    private inner class ClearDelayTask(
        private val packageName: String,
        private val callingUserUid: Int
    ) : Runnable {
        override fun run() {
            clearClipboard(packageName, callingUserUid)
        }
    }

    private class DelayThreadFactory : ThreadFactory {
        override fun newThread(r: Runnable?): Thread {
            return Thread(r, delayThreadName)
        }
    }

    init {
        ensureServices()
    }

    private fun ensureServices() {
        provideBinderService()
        provideAutoClearService()
    }

    private fun provideBinderService() {
        clipboardImplName
            .asClass(context.classLoader)
            ?.doAfter("getPrimaryClip", String::class.java, Int::class.java) {
                val packageName = it.args[0].toString()
                if (packageName == BuildConfig.PACKAGE_NAME) {
                    onServiceRequirement(it)
                }
            }
    }

    private fun provideAutoClearService() {
        val clipImplClazz = clipboardImplName.asClass(context.classLoader) ?: return
        clipImplClazz.doAfter("getPrimaryClip", String::class.java, Int::class.java) {
            val packageName = it.args[0] as String
            val userId = it.args[1] as Int
            val clipData = it.result as ClipData?
            onPrimaryClipGet(clipData, packageName, userId)
        }
        clipImplClazz.doAfter(
            "setPrimaryClip",
            ClipData::class.java,
            String::class.java,
            Int::class.java
        ) {
            val data = it.args[0] as ClipData
            val packageName = it.args[1] as String
            val uid = it.args[2] as Int
            onClipboardSet(data, packageName, uid)
        }
    }

    private fun onClipboardSet(data: ClipData, packageName: String, userId: Int) {
        resetReadCount()
        if (!dataStore.enable || dataStore.autoClearTimeout <= 0) {
            return
        }
        scheduleAutoClearTimeoutTask(packageName, userId)
    }

    private fun rescheduleCurrentAutoClearTimeoutTask() {
        removeCurrentAutoClearTask()
        currentClearTask?.let {
            delayExecutor.schedule(it, autoClearTimeout, TimeUnit.SECONDS)
        }
    }

    private fun scheduleAutoClearTimeoutTask(packageName: String, userId: Int) {
        removeCurrentAutoClearTask()
        currentClearTask = ClearDelayTask(packageName, userId)
        delayExecutor.schedule(currentClearTask, autoClearTimeout, TimeUnit.SECONDS)
    }

    private fun removeCurrentAutoClearTask() {
        currentClearTask?.let { task -> delayExecutor.remove(task) }
    }

    private fun onServiceRequirement(param: XC_MethodHook.MethodHookParam) {
        var result = param.result as ClipData?
        val item = ClipData.Item(createBinderIntent(this))
        if (result == null) {
            result = ClipData("LabelForExt", arrayOf(), item)
        } else {
            result = ClipData(result)
            result.addItem(item)
        }
        param.result = result
    }

    private fun onPrimaryClipGet(clipData: ClipData?, packageName: String, userId: Int) {
        executeAutoClearIfPossible(clipData, packageName, userId)
    }

    private fun executeAutoClearIfPossible(clipData: ClipData?, packageName: String, userId: Int) {
        XposedBridge.log("package $packageName uid $userId get clip, count down: ${currentCountDown.get()}")
        if (!dataStore.enable || !dataStore.autoClearEnable) {
            return
        }
        if (packageName == BuildConfig.PACKAGE_NAME) {
            return
        }
        if (clipDataExclude(clipData)) {
            return
        }
        when(dataStore.autoClearWorkMode) {
            Configuration.WORK_MODE_WHITELIST -> {
                if (matchesWhitelist(packageName)) {
                    return
                }
                countDownAndClearIfPossible(packageName, userId)
            }
            Configuration.WORK_MODE_BLACKLIST -> {
                if (matchesBlacklist(packageName)) {
                    countDownAndClearIfPossible(packageName, userId)
                }
            }
        }
    }

    private fun countDownAndClearIfPossible(packageName: String, userId: Int) {
        if (currentCountDown.get() <= 0) {
            return
        }
        if (currentCountDown.decrementAndGet() <= 0) {
            clearClipboard(packageName, userId)
        }
    }

    private fun clipDataExclude(clipData: ClipData?): Boolean {
        return clipData != null &&
                clipData.itemCount > 0 &&
                clipContentMatchesExclusion(clipData.getItemAt(0).text.toString())
    }

    private fun clipContentMatchesExclusion(content: String): Boolean {
        for (item in dataStore.autoClearContentExclusionList) {
            if (content.contains(item) || content.matches(Regex(item))) {
                return true
            }
        }
        return false
    }

    private fun matchesWhitelist(packageName: String): Boolean {
        for(item in dataStore.autoClearAppWhitelist) {
            if (packageName.contains(item) || packageName.matches(Regex(item))) {
                return true
            }
        }
        return false
    }

    private fun matchesBlacklist(packageName: String): Boolean {
        for(item in dataStore.autoClearAppBlacklist) {
            if (packageName.contains(item) || packageName.matches(Regex(item))) {
                return true
            }
        }
        return false
    }

    private fun clearClipboard(packageName: String, userId: Int) {
        val intendingUid = getIntendingUid(packageName, userId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            clearPrimaryClipSAndLater(packageName, intendingUid)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            clearPrimaryClipPAndLater(intendingUid)
        }
    }

    private fun getIntendingUid(packageName: String, userId: Int): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            realClipboardService.invokeMethod(
                "getIntendingUid",
                arrayOf(String::class.java, Int::class.java),
                packageName,
                userId
            ) as Int
        } else {
            Binder.getCallingUid()
        }
    }


    @TargetApi(Build.VERSION_CODES.P)
    private fun clearPrimaryClipPAndLater(intendingUserId: Int) {
        realClipboardService.invokeMethod(
            "setPrimaryClipInternal",
            arrayOf(ClipData::class.java, Int::class.java),
            null,
            intendingUserId
        )
    }

    @TargetApi(Build.VERSION_CODES.S)
    private fun clearPrimaryClipSAndLater(packageName: String, intendingUserId: Int) {
        mLock?.let {
            realClipboardService.invokeMethod(
                "setPrimaryClipInternalLocked",
                arrayOf(ClipData::class.java, Int::class.java, String::class.java),
                null,
                intendingUserId,
                packageName
            )
        }
    }

    override fun setEnable(enable: Boolean) {
        dataStore.enable = enable
    }

    override fun isEnable(): Boolean = dataStore.enable

    override fun setAutoClearEnable(enable: Boolean) {
        dataStore.autoClearEnable = enable
    }

    override fun isAutoClearEnable(): Boolean = dataStore.autoClearEnable

    override fun getAutoClearWorkMode(): Int = dataStore.autoClearWorkMode

    override fun setAutoClearWorkMode(mode: Int) {
        dataStore.autoClearWorkMode = mode
    }

    override fun getAutoClearReadCount(): Int = dataStore.autoClearReadCount

    override fun setAutoClearReadCount(count: Int) {
        dataStore.autoClearReadCount = count
        resetReadCount()
    }

    override fun setAutoClearAppWhitelist(exclusions: MutableList<String>) {
        dataStore.autoClearAppWhitelist = exclusions
    }

    override fun setAutoClearAppBlacklist(exclusions: MutableList<String>) {
        dataStore.autoClearAppBlacklist = exclusions
    }

    override fun getAutoClearAppBlacklist(): List<String> = dataStore.autoClearAppBlacklist

    override fun getAutoClearAppWhitelist(): List<String> = dataStore.autoClearAppWhitelist

    override fun setAutoClearContentExclusionList(exclusions: List<String>) {
        dataStore.autoClearContentExclusionList = exclusions
    }

    override fun getAutoClearContentExclusionList(): List<String> = dataStore.autoClearContentExclusionList

    private fun resetReadCount() {
        currentCountDown.set(dataStore.autoClearReadCount)
    }

    override fun setAutoClearTimeout(timeout: Long) {
        dataStore.autoClearTimeout = timeout
        rescheduleCurrentAutoClearTimeoutTask()
    }

    override fun getAutoClearTimeout(): Long {
        return dataStore.autoClearTimeout
    }

    override fun getAutoClearStrategies(): List<AutoClearStrategyInfo> = dataStore.autoClearStrategy

    override fun addAutoClearStrategy(strategy: AutoClearStrategyInfo) {
        dataStore.addAutoClearStrategy(strategy)
    }

    override fun removeStrategy(packageName: String) {
        dataStore.removeAutoClearStrategy(packageName)
    }

    private fun createBinderIntent(binder: IBinder): Intent {
        return Intent().apply {
            val bundle = Bundle()
            bundle.putBinder(bundleBinderKey, binder)
            putExtra(intentBundleKey, bundle)
        }
    }
}
