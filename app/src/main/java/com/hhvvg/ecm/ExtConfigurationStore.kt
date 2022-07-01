package com.hhvvg.ecm

import android.os.Handler
import android.os.HandlerThread
import com.google.gson.Gson
import java.io.File

/**
 * @author hhvvg
 */
class ExtConfigurationStore {
    companion object {
        private const val dataDirName = "/data/system/ext_clipboard_manager"
        private const val dataFileName = "ext_clipboard_service_configuration.json"
        private const val workThreadName = "ExtendedClipboardServiceConfigurationWorkThread"
    }

    private val dataDir by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val dirFile = File(dataDirName)
        if (!dirFile.exists()) {
            dirFile.mkdir()
        }
        dirFile
    }

    private val dataFile by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        val dataFile = File(dataDir, dataFileName)
        if (!dataFile.exists()) {
            dataFile.createNewFile()
        }
        dataFile
    }

    private val workThread by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        HandlerThread(workThreadName).apply {
            start()
        }
    }

    private val workHandler by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
        Handler(workThread.looper)
    }

    private val gson = Gson()

    private var configuration: Configuration

    var enable: Boolean
        get() {
            return configuration.enable
        }
        set(value) {
            configuration.enable = value
            workHandler.post(this::saveConfiguration)
        }

    init {
        configuration = try {
            val json = readFromFile()
            val gson = Gson()
            gson.fromJson(json, Configuration::class.java)
        }catch (e: Exception) {
            Configuration(enable = false)
        }
    }

    private fun readFromFile(): String {
        return dataFile.readText()
    }

    private fun saveConfiguration() {
        synchronized(configuration) {
            dataFile.writeText(gson.toJson(configuration))
        }
    }
}