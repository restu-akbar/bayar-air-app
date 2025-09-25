@file:Suppress("MissingPermission")

package org.com.bayarair.print

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import kotlinx.coroutines.suspendCancellableCoroutine
import org.com.bayarair.data.local.clearPrinterMac
import org.com.bayarair.data.local.loadSavedPrinterMac
import org.com.bayarair.data.local.savePrinterMac
import org.com.bayarair.utils.ReceiptPrinter
import kotlin.coroutines.resume

class UnifiedReceiptPrinter(
    private val activity: ComponentActivity,
) : ReceiptPrinter {
    @RequiresApi(Build.VERSION_CODES.P)
    override suspend fun printPdfFromUrl(
        url: String,
        forcePick: Boolean,
    ): Boolean {
        ensureBtPermissions()
        ensureBluetoothEnabled()

        val mac =
            if (forcePick) {
                pickPrinterOrThrowAndSave()
            } else {
                resolvePrinterMacOrThrow()
            }

        val impl = BluetoothEscPosPrinter(activity, mac)
        val ok = impl.printPdfFromUrl(url)
        if (!ok) throw Exception("Terjadi kesalahan saat mengirim data ke printer")
        return true
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private suspend fun pickPrinterOrThrowAndSave(): String {
        while (true) {
            val bonded = safeBondedDevices(activity).sortedBy { it.name?.lowercase() ?: "" }
            if (bonded.isEmpty()) {
                openBluetoothSettingsAndAwaitReturn()
                continue
            }
            when (val res = showPickerDialogAwait(bonded)) {
                is PickerResult.Selected -> {
                    savePrinterMac(activity, res.mac)
                    return res.mac
                }
                PickerResult.GoToSettings -> {
                    openBluetoothSettingsAndAwaitReturn()
                }
                PickerResult.Cancel -> throw Exception("Pemilihan printer dibatalkan")
            }
        }
    }

    private suspend fun ensureBtPermissions() {
        if (Build.VERSION.SDK_INT < 31) return

        val perms =
            arrayOf(
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
            )

        // sudah granted semua?
        val alreadyGranted =
            perms.all {
                ContextCompat.checkSelfPermission(
                    activity,
                    it,
                ) == PermissionChecker.PERMISSION_GRANTED
            }
        if (alreadyGranted) return

        val granted =
            suspendCancellableCoroutine<Boolean> { cont ->
                var launcher: androidx.activity.result.ActivityResultLauncher<Array<String>>? = null
                launcher =
                    activity.activityResultRegistry.register(
                        "req_bt_perms_${hashCode()}",
                        ActivityResultContracts.RequestMultiplePermissions(),
                    ) { result ->
                        // cek dari callback (dan fallback cek langsung)
                        val ok =
                            perms.all { p ->
                                result[p] == true ||
                                    ContextCompat.checkSelfPermission(
                                        activity,
                                        p,
                                    ) == PermissionChecker.PERMISSION_GRANTED
                            }
                        launcher?.unregister()
                        cont.resume(ok)
                    }
                cont.invokeOnCancellation { launcher?.unregister() }
                launcher.launch(perms)
            }

        if (!granted) throw Exception("Izin Bluetooth ditolak")
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private suspend fun ensureBluetoothEnabled() {
        val mgr = activity.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter = mgr.adapter ?: throw Exception("Bluetooth tidak tersedia di perangkat ini")
        if (adapter.isEnabled) return

        val enabled =
            suspendCancellableCoroutine<Boolean> { cont ->
                var launcher: androidx.activity.result.ActivityResultLauncher<Intent>? = null
                launcher =
                    activity.activityResultRegistry.register(
                        "req_bt_enable_${hashCode()}",
                        ActivityResultContracts.StartActivityForResult(),
                    ) {
                        launcher?.unregister()
                        // bisa cek adapter.isEnabled atau resultCode == Activity.RESULT_OK
                        cont.resume(adapter.isEnabled)
                    }
                cont.invokeOnCancellation { launcher?.unregister() }
                launcher.launch(Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE))
            }

        if (!enabled) throw Exception("Bluetooth belum diaktifkan")
    }

    @RequiresApi(Build.VERSION_CODES.P)
    private suspend fun resolvePrinterMacOrThrow(): String {
        loadSavedPrinterMac(activity)?.let { mac ->
            if (isPrinterBondedSafe(activity, mac)) return mac
            clearPrinterMac(activity)
        }

        while (true) {
            val bonded = safeBondedDevices(activity).sortedBy { it.name?.lowercase() ?: "" }

            if (bonded.isEmpty()) {
                openBluetoothSettingsAndAwaitReturn()
                continue
            }

            when (val res = showPickerDialogAwait(bonded)) {
                is PickerResult.Selected -> {
                    savePrinterMac(activity, res.mac)
                    return res.mac
                }

                PickerResult.GoToSettings -> {
                    openBluetoothSettingsAndAwaitReturn()
                }

                PickerResult.Cancel -> throw Exception("Pemilihan printer dibatalkan")
            }
        }
    }

    private suspend fun showPickerDialogAwait(devices: List<BluetoothDevice>): PickerResult =
        suspendCancellableCoroutine<PickerResult> { cont ->
            val names = devices.map { "${it.name ?: "Unknown"} (${it.address})" }.toTypedArray()
            var checkedIdx = -1

            val dialog =
                AlertDialog
                    .Builder(activity)
                    .setTitle("Pilih Printer Bluetooth")
                    .setSingleChoiceItems(names, checkedIdx) { _, which -> checkedIdx = which }
                    .setNegativeButton("Batal") { d, _ ->
                        d.dismiss()
                        cont.resume(PickerResult.Cancel)
                    }.setNeutralButton("Pair di Settings") { d, _ ->
                        d.dismiss()
                        cont.resume(PickerResult.GoToSettings)
                    }.setPositiveButton("Gunakan") { d, _ ->
                        d.dismiss()
                        if (checkedIdx >= 0) {
                            cont.resume(PickerResult.Selected(devices[checkedIdx].address))
                        } else {
                            cont.resume(PickerResult.Cancel)
                        }
                    }.setCancelable(false)
                    .create()

            dialog.show()
            cont.invokeOnCancellation { runCatching { dialog.dismiss() } }
        }

    @RequiresApi(Build.VERSION_CODES.P)
    private suspend fun openBluetoothSettingsAndAwaitReturn() {
        var launcher: androidx.activity.result.ActivityResultLauncher<Intent>? = null
        suspendCancellableCoroutine<Unit> { cont ->
            launcher =
                activity.activityResultRegistry.register(
                    "open_bt_settings_${hashCode()}",
                    ActivityResultContracts.StartActivityForResult(),
                ) {
                    launcher?.unregister()
                    cont.resume(Unit)
                }
            cont.invokeOnCancellation { launcher?.unregister() }
            launcher!!.launch(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
        }
    }
}

@SuppressLint("MissingPermission")
fun safeBondedDevices(context: Context): Set<BluetoothDevice> {
    val mgr = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val adapter = mgr.adapter ?: return emptySet()

    if (Build.VERSION.SDK_INT >= 31 &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
        != PackageManager.PERMISSION_GRANTED
    ) {
        return emptySet()
    }

    return try {
        adapter.bondedDevices ?: emptySet()
    } catch (_: SecurityException) {
        emptySet()
    }
}

fun isPrinterBondedSafe(
    context: Context,
    mac: String,
): Boolean = safeBondedDevices(context).any { it.address.equals(mac, ignoreCase = true) }

fun hasBondedPrintersSafe(context: Context): Boolean = safeBondedDevices(context).isNotEmpty()

private sealed class PickerResult {
    data class Selected(
        val mac: String,
    ) : PickerResult()

    data object GoToSettings : PickerResult()

    data object Cancel : PickerResult()
}
