package com.hhvvg.ecm.ui.data

import android.graphics.drawable.Drawable
import com.hhvvg.ecm.configuration.AutoClearStrategyInfo

open class AppItem(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable,
    val flags: Int,
) {
    constructor(appItem: AppItem): this(appItem.packageName, appItem.appName, appItem.appIcon, appItem.flags)
}
