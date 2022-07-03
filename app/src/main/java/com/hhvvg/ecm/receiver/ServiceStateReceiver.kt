package com.hhvvg.ecm.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter

abstract class ServiceStateReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when(intent.action) {
            ACTION_STATE_CHANGED -> {
                val enable = intent.getBooleanExtra(EXTRA_STATE_ENABLE, false)
                val source = intent.getStringExtra(EXTRA_EVENT_SOURCE) ?: ""
                onServiceStateChanged(enable, source)
            }
        }
    }

    abstract fun onServiceStateChanged(enable: Boolean, source: String)

    companion object {
        const val ACTION_STATE_CHANGED = "com.hhvvg.ecm.service.STATE_CHANGED"
        const val EXTRA_STATE_ENABLE = "EXTRA_STATE_ENABLE"
        const val EXTRA_EVENT_SOURCE = "EXTRA_EVENT_SOURCE"

        fun sendStateChangedBroadcast(context: Context, enable: Boolean, source: String) {
            val intent = Intent(ACTION_STATE_CHANGED)
            intent.putExtra(EXTRA_STATE_ENABLE, enable)
            intent.putExtra(EXTRA_EVENT_SOURCE, source)
            context.sendBroadcast(intent)
        }

        fun registerStateChangedReceiver(context: Context, receiver: BroadcastReceiver) {
            val intentFilter = IntentFilter(ACTION_STATE_CHANGED)
            context.registerReceiver(receiver, intentFilter)
        }

    }
}
