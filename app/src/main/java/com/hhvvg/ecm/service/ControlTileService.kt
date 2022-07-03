package com.hhvvg.ecm.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.hhvvg.ecm.IExtClipboardService
import com.hhvvg.ecm.receiver.ServiceStateReceiver
import com.hhvvg.ecm.util.getSystemExtClipboardService

/**
 * @author hhvvg
 */
@RequiresApi(Build.VERSION_CODES.N)
class ControlTileService : TileService() {
    companion object {
        const val CONTROL_SERVICE_EVENT_SOURCE = "CONTROL_SERVICE_EVENT_SOURCE"
    }
    private var extService: IExtClipboardService? = null
    private var serviceEnable = false
    private var tile: Tile? = null
    private val stateReceiver = object : ServiceStateReceiver() {
        override fun onServiceStateChanged(enable: Boolean, source: String) {
            if (source != CONTROL_SERVICE_EVENT_SOURCE) {
                serviceEnable = enable
                updateTileState()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        extService = baseContext.getSystemExtClipboardService()
        serviceEnable = extService?.isEnable ?: false
        updateTileState()
        ServiceStateReceiver.registerStateChangedReceiver(baseContext, stateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(stateReceiver)
    }

    override fun onTileAdded() {
        super.onTileAdded()
        tile = qsTile
        updateTileState()
    }

    override fun onTileRemoved() {
        super.onTileRemoved()
        tile = null
    }

    override fun onClick() {
        serviceEnable = !serviceEnable
        extService?.isEnable = serviceEnable
        updateTileState()
        ServiceStateReceiver.sendStateChangedBroadcast(this, serviceEnable, CONTROL_SERVICE_EVENT_SOURCE)
    }

    private fun updateTileState() {
        val service = extService
        val state = if (service == null) {
            Tile.STATE_UNAVAILABLE
        } else if (service.isEnable) {
            Tile.STATE_ACTIVE
        } else {
            Tile.STATE_INACTIVE
        }
        tile?.state = state
    }
}
