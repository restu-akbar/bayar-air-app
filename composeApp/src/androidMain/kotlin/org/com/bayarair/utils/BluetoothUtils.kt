package org.com.bayarair.utils

import android.Manifest
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

data class BondedDevice(
    val name: String,
    val mac: String,
)

fun getBondedDevices(context: Context): List<BondedDevice> {
    val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val adapter = btManager.adapter ?: return emptyList()

    if (Build.VERSION.SDK_INT >= 31) {
        val granted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT,
            ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return emptyList()
    }

    return try {
        adapter.bondedDevices
            .map { BondedDevice(it.name ?: "Unknown", it.address) }
            .sortedBy { it.name.lowercase() }
    } catch (_: SecurityException) {
        emptyList()
    }
}
