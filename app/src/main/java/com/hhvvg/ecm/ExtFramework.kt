package com.hhvvg.ecm

import android.content.Context
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage

class ExtFramework : IXposedHookLoadPackage, IXposedHookZygoteInit {
    companion object {
        const val clipboardServiceName = "com.android.server.clipboard.ClipboardService"
        const val serviceName = "_extendedClipboardService_injected_by_hhvvg"
        const val clipboardImplName = "$clipboardServiceName\$ClipboardImpl"
        const val staticClipboardServiceName = "_staticExtendedClipboardServiceInstance_injected_by_hhvvg"
    }

    override fun handleLoadPackage(p0: XC_LoadPackage.LoadPackageParam) {
        if (p0.packageName != "android") {
            return
        }
        // Inject my own service
        clipboardServiceName.asClass(p0.classLoader)?.afterConstructor(Context::class.java) {
            val context = it.args[0] as Context
            val clipboardService = ExtendedClipboardService(context, it.thisObject)
            setExtraField(serviceName, clipboardService)
        }
    }

    override fun initZygote(p0: IXposedHookZygoteInit.StartupParam) {

    }
}