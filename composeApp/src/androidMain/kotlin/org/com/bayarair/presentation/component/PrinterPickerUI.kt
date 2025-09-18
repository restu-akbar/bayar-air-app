package org.com.bayarair.presentation.component

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.com.bayarair.utils.getBondedDevices
import org.com.bayarair.data.local.savePrinterMac

@Composable
fun PrinterPickerDialog(
    context: Context,
    onDismiss: () -> Unit,
    onPicked: (mac: String) -> Unit
) {
    val devices by remember { mutableStateOf(getBondedDevices(context)) }
    var selected by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pilih Printer Bluetooth") },
        text = {
            if (devices.isEmpty()) {
                Text("Tidak ada perangkat yang sudah dipasangkan.\nBuka pengaturan Bluetooth dan lakukan pairing terlebih dahulu.")
            } else {
                LazyColumn(Modifier.fillMaxWidth().heightIn(max = 320.dp)) {
                    items(devices) { d ->
                        ListItem(
                            headlineContent = { Text(d.name) },
                            supportingContent = { Text(d.mac) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selected = d.mac }
                        )
                        HorizontalDivider()
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
