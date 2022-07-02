package com.hhvvg.ecm.service

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import com.hhvvg.ecm.*
import com.hhvvg.ecm.ExtFramework.Companion.clipboardImplName
import com.hhvvg.ecm.configuration.AutoClearStrategyInfo
import com.hhvvg.ecm.configuration.ExtConfigurationStore
import com.hhvvg.ecm.util.asClass
import com.hhvvg.ecm.util.doAfter
import com.hhvvg.ecm.util.getField
import com.hhvvg.ecm.util.invokeMethod
import de.robv.android.xposed.XposedBridge
import java.util.concurrent.ScheduledThreadPoolExecutor
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author hhvvg
 */
class ExtendedClipboardService(private val context: Context, private val realClipboardService: Any): IExtClipboardService.Stub() {
    companion object {
        const val bundleBinderKey = "ExtendedClipboardServiceBinder"
        const val intentBundleKey = "ExtendedClipboardServiceBundle"
        const val delayThreadName = "ExtendedClipboardServiceDelayThread"
    }

    private val mLock = realClipboardService.getField<Any>("mLock")!!
    private val dataStore by lazy {
        ExtConfigurationStore()
    }
    private val delayExecutor = ScheduledThreadPoolExecutor(1, DelayThreadFactory())
    private var currentClearTask: Runnable? = null
    private val currentCountDown = AtomicInteger(0)

    private inner class ClearDelayTask(private val packageName: String, private val callingUserUid: Int): Runnable {
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
        clipboardImplName.asClass(context.classLoader)?.doAfter("getPrimaryClip", String::class.java, Int::class.java) {
            val packageName = it.args[0].toString()
            if (packageName == BuildConfig.PACKAGE_NAME) {
                var result = it.result as ClipData?
                val item = ClipData.Item(createBinderIntent(this))
                if (result == null) {
                    result = ClipData("LabelForExt", arrayOf(), item)
                } else {
                    result = ClipData(result)
                    result.addItem(item)
                }
                it.result = result
            }
        }
    }

    private fun provideAutoClearService() {
        val clipImplClazz = clipboardImplName.asClass(context.classLoader) ?: return
        clipImplClazz.doAfter("getPrimaryClip", String::class.java, Int::class.java) {
            val packageName = it.args[0] as String
            val userId = it.args[1] as Int
            XposedBridge.log("package ${packageName} uid ${userId} get clip, count down: ${currentCountDown.get()}")
            if (!dataStore.enable || !dataStore.autoClearEnable) {
                return@doAfter
            }
            if (currentCountDown.get() <= 0) {
                return@doAfter
            }
            if (currentCountDown.decrementAndGet() <= 0) {
                clearClipboard(packageName, userId)
            }
        }
        clipImplClazz.doAfter("setPrimaryClip", ClipData::class.java, String::class.java, Int::class.java) {
            resetReadCount()
            XposedBridge.log("set clipboard, count remain: ${currentCountDown.get()}")
            if (!dataStore.enable || dataStore.autoClearTimeout <= 0) {
                return@doAfter
            }
            val packageName = it.args[1] as String
            val uid = it.args[2] as Int
            currentClearTask?.let { task -> delayExecutor.remove(task) }
            currentClearTask = ClearDelayTask(packageName, uid)
            delayExecutor.schedule(currentClearTask, autoClearTimeout, TimeUnit.SECONDS)
        }
    }

    private fun clearClipboard(packageName: String, userId: Int) {
        val intendingUid = realClipboardService.invokeMethod("getIntendingUid", arrayOf(String::class.java, Int::class.java), packageName, userId)
        synchronized(mLock) {
            realClipboardService.invokeMethod("setPrimaryClipInternalLocked", arrayOf(ClipData::class.java, Int::class.java, String::class.java), null, intendingUid, packageName)
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

    private fun resetReadCount() {
        currentCountDown.set(dataStore.autoClearReadCount)
    }

    override fun setAutoClearTimeout(timeout: Long) {
        dataStore.autoClearTimeout = timeout
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
