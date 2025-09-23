package org.com.bayarair

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import org.com.bayarair.core.AppEvents
import org.com.bayarair.data.local.clearPrinterMac
import org.com.bayarair.data.local.loadSavedPrinterMac
import org.com.bayarair.data.local.savePrinterMac
import org.com.bayarair.presentation.component.PrinterPickerDialog
import org.com.bayarair.print.BluetoothEscPosPrinter
import org.com.bayarair.utils.LocalReceiptPrinter
import org.com.bayarair.utils.ReceiptPrinter
import org.koin.java.KoinJavaComponent.get

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val appEvents: AppEvents = get(AppEvents::class.java)

        setContent {
            val context = LocalContext.current
            var mac by remember { mutableStateOf(loadSavedPrinterMac(context)) }
            var showPicker by remember { mutableStateOf(false) }

            fun hasBtConnect() =
                Build.VERSION.SDK_INT < 31 ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT,
                    ) == PackageManager.PERMISSION_GRANTED

            fun hasBtScan() =
                Build.VERSION.SDK_INT < 31 ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_SCAN,
                    ) == PackageManager.PERMISSION_GRANTED

            var canShowPicker by remember { mutableStateOf(hasBtConnect() && hasBtScan()) }

            val permLauncher =
                rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions(),
                ) { result ->
                    val connectGranted =
                        result[Manifest.permission.BLUETOOTH_CONNECT] == true || Build.VERSION.SDK_INT < 31
                    val scanGranted =
                        result[Manifest.permission.BLUETOOTH_SCAN] == true || Build.VERSION.SDK_INT < 31

                    canShowPicker = connectGranted && scanGranted
                    if (canShowPicker) {
                        showPicker = true
                    }
                }

            val promptPrinter =
                remember {
                    object : ReceiptPrinter {
                        override suspend fun printPdfFromUrl(url: String): Boolean {
                            if (Build.VERSION.SDK_INT >= 31 && (!hasBtConnect() || !hasBtScan())) {
                                permLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.BLUETOOTH_CONNECT,
                                        Manifest.permission.BLUETOOTH_SCAN,
                                    ),
                                )
                            } else {
                                showPicker = true
                            }
                            return false
                        }
                    }
                }

            val isPaired = mac?.let { isPrinterBondedSafe(context, it) } ?: false
            val connectGranted = hasBtConnect()

            val printer: ReceiptPrinter =
                when {
                    mac != null && isPaired -> BluetoothEscPosPrinter(context, mac!!)
                    !connectGranted -> promptPrinter
                    hasBondedPrintersSafe(context) -> promptPrinter
                    else -> PromptToPairPrinter(context)
                }

            LaunchedEffect(mac, isPaired) {
                if (mac != null && !isPaired) {
                    clearPrinterMac(context)
                    mac = null
                    showPicker = true
                }
            }

            CompositionLocalProvider(LocalReceiptPrinter provides printer) {
                App(appEvents)
                if (showPicker) {
                    if (!canShowPicker) {
                        if (Build.VERSION.SDK_INT >= 31) {
                            permLauncher.launch(
                                arrayOf(
                                    Manifest.permission.BLUETOOTH_CONNECT,
                                    Manifest.permission.BLUETOOTH_SCAN,
                                ),
                            )
                        } else {
                            showPicker = true
                        }
                    } else {
                        PrinterPickerDialog(
                            context = context,
                            onDismiss = { showPicker = false },
                            onPicked = { newMac ->
                                savePrinterMac(context, newMac)
                                mac = newMac
                                showPicker = false
                            },
                        )
                    }
                }
            }
        }
    }
}

private class PromptToPairPrinter(
    private val context: Context,
) : ReceiptPrinter {
    override suspend fun printPdfFromUrl(url: String): Boolean {
        Toast
            .makeText(
                context,
                "Belum ada printer. Silakan lakukan pairing printer terlebih dahulu.",
                Toast.LENGTH_LONG,
            ).show()
        val intent =
            Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        context.startActivity(intent)
        return false
    }
}

@SuppressLint("MissingPermission")
private fun safeBondedDevices(context: Context): Set<BluetoothDevice> {
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

private fun isPrinterBondedSafe(
    context: Context,
    mac: String,
): Boolean = safeBondedDevices(context).any { it.address.equals(mac, ignoreCase = true) }

private fun hasBondedPrintersSafe(context: Context): Boolean = safeBondedDevices(context).isNotEmpty()
