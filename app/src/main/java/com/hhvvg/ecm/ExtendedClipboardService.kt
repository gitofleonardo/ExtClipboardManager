package com.hhvvg.ecm

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import com.hhvvg.ecm.ExtFramework.Companion.clipboardImplName

/**
 * @author hhvvg
 */
class ExtendedClipboardService(private val context: Context, private val realClipboardService: Any): IExtClipboardService.Stub() {
    companion object {
        const val bundleBinderKey = "ExtendedClipboardServiceBinder"
        const val intentBundleKey = "ExtendedClipboardServiceBundle"
    }

    init {
        ensureServices()
    }

    private fun ensureServices() {
        provideBinderService()
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

    private val dataStore by lazy {
        ExtConfigurationStore()
    }

    override fun setEnable(enable: Boolean) {
        dataStore.enable = enable
    }

    override fun isEnable(): Boolean = dataStore.enable

    private fun createBinderIntent(binder: IBinder): Intent {
        return Intent().apply {
            val bundle = Bundle()
            bundle.putBinder(bundleBinderKey, binder)
            putExtra(intentBundleKey, bundle)
        }
    }
}
