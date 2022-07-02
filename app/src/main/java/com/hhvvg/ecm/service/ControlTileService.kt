package com.hhvvg.ecm.service

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi
import com.hhvvg.ecm.IExtClipboardService
import com.hhvvg.ecm.getSystemExtClipboardService

/**
 * @author hhvvg
 */
@RequiresApi(Build.VERSION_CODES.N)
class ControlTileService : TileService() {
    private var extService: IExtClipboardService? = null
    private var serviceEnable = false
    private var tile: Tile? = null

    override fun onCreate() {
        super.onCreate()
        extService = baseContext.getSystemExtClipboardService()
        serviceEnable = extService?.isEnable ?: false
        updateTileState()
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
