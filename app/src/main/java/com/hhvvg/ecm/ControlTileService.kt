package com.hhvvg.ecm

import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.annotation.RequiresApi

/**
 * @author hhvvg
 */
@RequiresApi(Build.VERSION_CODES.N)
class ControlTileService : TileService() {
    private var extService: IExtClipboardService? = null
    private var manageEnable = false
        set(value) {
            field = value
            qsTile.state = if (value) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                qsTile.subtitle = if (value) getString(R.string.enabled) else getString(R.string.disabled)
            }
            qsTile.updateTile()
        }

    override fun onCreate() {
        super.onCreate()
        extService = baseContext.getSystemExtClipboardService()
        loadInitialValue()
    }

    override fun onTileAdded() {
        loadInitialValue()
    }

    override fun onClick() {
        manageEnable = !manageEnable
        extService?.isEnable = manageEnable
    }

    private fun loadInitialValue() {
        manageEnable = extService?.isEnable ?: false
        qsTile.state = if (manageEnable) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
    }
}
