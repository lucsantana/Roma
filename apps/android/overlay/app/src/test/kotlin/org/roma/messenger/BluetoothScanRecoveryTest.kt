// SPDX-License-Identifier: GPL-3.0-or-later
package org.roma.messenger

import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.os.Looper
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class BluetoothScanRecoveryTest {
    private val context = RuntimeEnvironment.getApplication()
    private val events = mutableListOf<String>()
    private val recovery = BluetoothScanRecovery(context,
        suspendScan = { events += "suspend" }, resumeScan = { events += "resume" })

    @After fun cleanup() { recovery.stop() }

    private fun broadcast(state: Int, action: String = BluetoothAdapter.ACTION_STATE_CHANGED) {
        context.sendBroadcast(Intent(action).putExtra(BluetoothAdapter.EXTRA_STATE, state))
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test fun `power cycle resumes scanning immediately without watchdog time advancing`() {
        recovery.start()
        broadcast(BluetoothAdapter.STATE_TURNING_OFF)
        broadcast(BluetoothAdapter.STATE_OFF)
        broadcast(BluetoothAdapter.STATE_TURNING_ON)
        broadcast(BluetoothAdapter.STATE_ON)
        broadcast(BluetoothAdapter.STATE_ON)
        assertEquals(listOf("suspend", "resume"), events)
    }

    @Test fun `off without transition and repeated cycles each recover once`() {
        recovery.start()
        repeat(2) {
            broadcast(BluetoothAdapter.STATE_OFF)
            broadcast(BluetoothAdapter.STATE_ON)
        }
        assertEquals(listOf("suspend", "resume", "suspend", "resume"), events)
    }

    @Test fun `stopped client never resumes from a later power on`() {
        recovery.start()
        broadcast(BluetoothAdapter.STATE_OFF)
        recovery.stop()
        broadcast(BluetoothAdapter.STATE_ON)
        assertEquals(listOf("suspend"), events)
    }

    @Test fun `duplicate registration and unrelated broadcasts do not restart scans`() {
        recovery.start()
        recovery.start()
        broadcast(BluetoothAdapter.STATE_ON)
        broadcast(BluetoothAdapter.STATE_OFF, "org.roma.test.UNRELATED")
        broadcast(BluetoothAdapter.STATE_ON)
        assertEquals(emptyList<String>(), events)
        broadcast(BluetoothAdapter.STATE_OFF)
        broadcast(BluetoothAdapter.STATE_ON)
        assertEquals(listOf("suspend", "resume"), events)
    }
}
