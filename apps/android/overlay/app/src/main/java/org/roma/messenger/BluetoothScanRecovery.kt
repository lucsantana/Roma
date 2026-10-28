// SPDX-License-Identifier: GPL-3.0-or-later
package org.roma.messenger

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat

/** Observe the protected adapter-state broadcast only while the BLE client is active. */
internal class BluetoothScanRecovery(
    private val context: Context,
    private val suspendScan: () -> Unit,
    private val resumeScan: () -> Unit,
) {
    private var registered = false
    private var awaitingPowerOn = false
    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (!registered || intent?.action != BluetoothAdapter.ACTION_STATE_CHANGED) return
            when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                BluetoothAdapter.STATE_TURNING_OFF, BluetoothAdapter.STATE_OFF -> {
                    if (!awaitingPowerOn) {
                        awaitingPowerOn = true
                        suspendScan()
                    }
                }
                BluetoothAdapter.STATE_ON -> {
                    if (awaitingPowerOn) {
                        awaitingPowerOn = false
                        resumeScan()
                    }
                }
            }
        }
    }

    fun start() {
        if (registered) return
        // Bluetooth broadcasts originate from a privileged Bluetooth process.
        // ACTION_STATE_CHANGED is a protected system broadcast.
        ContextCompat.registerReceiver(context, receiver,
            IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED), ContextCompat.RECEIVER_EXPORTED)
        registered = true
    }

    fun stop() {
        if (!registered) return
        registered = false
        awaitingPowerOn = false
        context.unregisterReceiver(receiver)
    }
}
