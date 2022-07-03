package com.hhvvg.ecm.configuration

/**
 * @author hhvvg
 */
class Configuration{
    var enable: Boolean = false
    var autoClearEnable: Boolean = false
    var autoClearStrategies: MutableList<AutoClearStrategyInfo> = mutableListOf()
    var autoClearTimeout: Long = -1
    var workMode: Int = WORK_MODE_WHITELIST
    var readCount: Int = 1
    var autoClearAppBlacklist: MutableList<String> = mutableListOf()
    var autoClearAppWhitelist: MutableList<String> = mutableListOf()
    var autoClearContentExclusionList: MutableList<String> = mutableListOf()

    companion object {
        const val WORK_MODE_WHITELIST = 0
        const val WORK_MODE_BLACKLIST = 1
    }
}