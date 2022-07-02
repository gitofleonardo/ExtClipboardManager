package com.hhvvg.ecm.configuration

/**
 * @author hhvvg
 */
data class Configuration(
    var enable: Boolean,
    var autoClearEnable: Boolean,
    var autoClearStrategies: MutableList<AutoClearStrategyInfo>,
    var autoClearTimeout: Long,
    var workMode: Int,
    var readCount: Int,
)