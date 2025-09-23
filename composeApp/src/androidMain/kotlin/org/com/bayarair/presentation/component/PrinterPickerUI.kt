package org.com.bayarair.presentation.component

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.com.bayarair.data.local.savePrinterMac
import org.com.bayarair.utils.BondedState
import org.com.bayarair.utils.getBondedDevices

@Composable
fun PrinterPickerDialog(
    context: Context,
    onDismiss: () -> Unit,
    onPicked: (mac: String) -> Unit
) {
    val bondedState by remember { mutableStateOf(getBondedDevices(context)) }
    var selected by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Printer Bluetooth") },
        text = {
            when (bondedState) {
                BondedState.BluetoothOff -> Text(
                    "Bluetooth sedang mati.\nAktifkan Bluetooth untuk melihat perangkat yang sudah dipasangkan."
                )

                BondedState.PermissionDenied -> Text(
                    "Izin Bluetooth belum diberikan.\nBerikan izin untuk menampilkan daftar printer."
                )

                is BondedState.Devices -> {
                    val devices = (bondedState as BondedState.Devices).list
                    if (devices.isEmpty()) {
                        Text(
                            "Belum ada perangkat yang dipasangkan.\n" +
                                    "Lakukan pairing printer di pengaturan Bluetooth."
                        )
                    } else {
                        LazyColumn(
                            Modifier
                                .fillMaxWidth()
                                .heightIn(max = 320.dp)
                        ) {
                            items(devices) { d ->
                                val isSelected = d.mac == selected
                                ListItem(
                                    headlineContent = { Text(d.name) },
                                    supportingContent = { Text(d.mac) },
                                    trailingContent = {
                                        if (isSelected) Icon(Icons.Default.Check, contentDescription = null)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selected = d.mac }
                                        .background(
                                            if (isSelected) Color.LightGray
                                            else MaterialTheme.colorScheme.primaryContainer
                                        )
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

        },
        confirmButton = {
            TextButton(
                enabled = selected != null,
                onClick = {
                    val mac = selected!!
                    savePrinterMac(context, mac)
                    onPicked(mac)
                }
            ) { Text("Simpan") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Batal") }
        }
    )
}
