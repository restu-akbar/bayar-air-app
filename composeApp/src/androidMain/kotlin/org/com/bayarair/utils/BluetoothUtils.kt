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

sealed class BondedState {
    object BluetoothOff : BondedState()

    object PermissionDenied : BondedState()

    data class Devices(
        val list: List<BondedDevice>,
    ) : BondedState()
}

fun getBondedDevices(context: Context): BondedState {
    val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val adapter = btManager.adapter ?: return BondedState.BluetoothOff

    if (!adapter.isEnabled) return BondedState.BluetoothOff

    if (Build.VERSION.SDK_INT >= 31) {
        val granted =
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.BLUETOOTH_CONNECT,
            ) == PackageManager.PERMISSION_GRANTED
        if (!granted) return BondedState.PermissionDenied
    }

    return try {
        val devices =
            adapter.bondedDevices
                .map { BondedDevice(it.name ?: "Unknown", it.address) }
                .sortedBy { it.name.lowercase() }
        BondedState.Devices(devices)
    } catch (_: SecurityException) {
        BondedState.PermissionDenied
    }
}
