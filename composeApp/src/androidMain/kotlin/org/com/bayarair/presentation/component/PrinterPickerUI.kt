package org.com.bayarair.presentation.component

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.com.bayarair.data.local.savePrinterMac
import org.com.bayarair.utils.getBondedDevices

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
                LazyColumn(Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)) {
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
