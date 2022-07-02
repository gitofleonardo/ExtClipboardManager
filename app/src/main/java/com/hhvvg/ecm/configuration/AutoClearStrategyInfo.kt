package com.hhvvg.ecm.configuration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class AutoClearStrategyInfo(
    var packageName: String,
    var clearFlag: Int,
    var count: Int,
    var regex: String,
    var contains: String
): Parcelable {
    constructor(packageName: String) : this(packageName, 0, 0, "", "")

    companion object {
        const val FLAG_CLEAR_IMMEDIATELY = 1
        const val FLAG_CLEAR_IGNORE = 1 shl 1
        const val FLAG_CLEAR_COUNT = 1 shl 2
        const val FLAG_CLEAR_REGEX = 1 shl 3
        const val FLAG_CLEAR_CONTAINS = 1 shl 4
    }
}
