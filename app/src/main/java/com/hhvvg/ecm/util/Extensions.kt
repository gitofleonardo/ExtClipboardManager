package com.hhvvg.ecm.util

import android.content.ClipboardManager
import android.content.Context
import android.util.TypedValue
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import com.hhvvg.ecm.IExtClipboardService
import com.hhvvg.ecm.service.ExtendedClipboardService
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import kotlin.reflect.KClass

fun KClass<*>.doAfter(methodName: String, vararg params: KClass<*>, callback: (XC_MethodHook.MethodHookParam) -> Unit) {
    this.java.doAfter(methodName, params = params.map { it.java }.toTypedArray(), callback = callback)
}

fun Class<*>.doAfter(methodName: String, vararg params: Class<*>, callback: (XC_MethodHook.MethodHookParam) -> Unit) {
    XposedHelpers.findAndHookMethod(this, methodName, *params, object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            callback.invoke(param)
        }
    })
}

fun KClass<*>.doBefore(methodName: String, vararg params: KClass<*>, callback: (XC_MethodHook.MethodHookParam) -> Unit) {
    this.java.doBefore(methodName, params = params.map { it.java }.toTypedArray(), callback = callback)
}

fun Class<*>.doBefore(methodName: String, vararg  params: Class<*>, callback: (XC_MethodHook.MethodHookParam) -> Unit) {
    XposedHelpers.findAndHookMethod(this, methodName, *params, object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            callback.invoke(param)
        }
    })
}

fun KClass<*>.afterConstructor(vararg params: KClass<*>, callback: (XC_MethodHook.MethodHookParam) -> Unit) {
    this.java.afterConstructor(params = params.map { it.java }.toTypedArray(), callback = callback)
}

fun Class<*>.afterConstructor(vararg params: Class<*>, callback: (XC_MethodHook.MethodHookParam) -> Unit) {
    XposedHelpers.findAndHookConstructor(this, *params, object : XC_MethodHook() {
        override fun afterHookedMethod(param: MethodHookParam) {
            callback.invoke(param)
        }
    })
}

fun KClass<*>.beforeConstructor(methodName: String, vararg params: KClass<*>, callback: (XC_MethodHook.MethodHookParam) -> Unit) {
    this.java.beforeConstructor(methodName, params = params.map { it.java }.toTypedArray(), callback = callback)
}

fun Class<*>.beforeConstructor(methodName: String, vararg params: Class<*>, callback: (XC_MethodHook.MethodHookParam) -> Unit) {
    XposedHelpers.findAndHookConstructor(this, *params, object : XC_MethodHook() {
        override fun beforeHookedMethod(param: MethodHookParam) {
            callback.invoke(param)
        }
    })
}

fun String.asClass(loader: ClassLoader? = null): Class<*>? {
    return try {
        Class.forName(this, true, loader) as Class<*>
    } catch (e: Exception) {
        XposedBridge.log(e)
        null
    }
}

fun Any.setField(field: String, value: Any?) {
    XposedHelpers.setObjectField(this, field, value)
}

fun Class<*>.setStaticField(field: String, value: Any?) {
    XposedHelpers.setStaticObjectField(this, field, value)
}

fun Any.setStaticField(field: String, value: Any?) {
    this.javaClass.setStaticField(field, value)
}

fun <T> Any.getField(field: String): T? {
    return try {
        XposedHelpers.getObjectField(this, field) as T
    } catch (e: Exception) {
        XposedBridge.log(e)
        null
    }
}

fun <T> Class<*>.getStaticField(field: String): T? {
    return try {
        XposedHelpers.getStaticObjectField(this, field) as T
    }catch (e: Exception) {
        XposedBridge.log(e)
        null
    }
}

fun <T> Any.getStaticField(field: String): T? {
    return this.javaClass.getStaticField(field)
}

fun Any.invokeMethod(methodName: String, paramClazz: Array<Class<*>>, vararg params: Any?): Any? {
    return XposedHelpers.callMethod(this, methodName, paramClazz, *params)
}

fun Any.setExtraField(field: String, value: Any?) {
    XposedHelpers.setAdditionalInstanceField(this, field, value)
}

fun Class<*>.setExtraStaticField(field: String, value: Any?) {
    XposedHelpers.setAdditionalStaticField(this, field, value)
}

fun Any.setExtraStaticField(field: String, value: Any?) {
    this.javaClass.setExtraStaticField(field, value)
}

fun <T> Any.getExtraField(field: String): T? {
    return try {
        XposedHelpers.getAdditionalInstanceField(this, field) as T
    } catch (e: Exception) {
        XposedBridge.log(e)
        null
    }
}

fun <T> Class<*>.getExtraStaticField(field: String): T? {
    return try {
        XposedHelpers.getAdditionalStaticField(this, field) as T
    }catch (e: Exception) {
        XposedBridge.log(e)
        null
    }
}

fun <T> Any.getExtraStaticField(field: String): T? {
    return this.javaClass.getExtraStaticField(field)
}

fun Context.getSystemExtClipboardService(): IExtClipboardService? {
    val cbs = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val data = cbs.primaryClip ?: return null
    if (data.itemCount <= 0) {
        return null
    }
    val clipIntent = data.getItemAt(data.itemCount - 1).intent
    val binder = clipIntent.getBundleExtra(ExtendedClipboardService.intentBundleKey)?.getBinder(
        ExtendedClipboardService.bundleBinderKey
    ) ?: return null
    return IExtClipboardService.Stub.asInterface(binder)
}

@ColorInt
fun Context.themeColor(@AttrRes attrRes: Int): Int = TypedValue()
    .apply { theme.resolveAttribute (attrRes, this, true) }
    .data
