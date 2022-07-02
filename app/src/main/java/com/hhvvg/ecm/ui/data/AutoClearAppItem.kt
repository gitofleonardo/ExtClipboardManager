package com.hhvvg.ecm.ui.data

import android.graphics.drawable.Drawable
import com.hhvvg.ecm.configuration.AutoClearStrategyInfo

data class AutoClearAppItem(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable,
    val strategy: AutoClearStrategyInfo,
    val flags: Int,
)
