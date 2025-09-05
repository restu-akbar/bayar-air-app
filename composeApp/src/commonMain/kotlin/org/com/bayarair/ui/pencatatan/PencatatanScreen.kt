package org.com.bayarair.ui.pencatatan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PencatatanScreen(
    rootNavController: NavController, paddingValues: PaddingValues
)
{
    var nama by remember { mutableStateOf("") }
    var id_pelanggan by remember { mutableStateOf("") }
    var meteran by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var isChecked by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(paddingValues),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Pencatatan",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        )
        //
//        Switch(
//            checked = isChecked,
//            onCheckedChange = { isChecked = it }
//        )


        OutlinedTextField(

            value = id_pelanggan,
            onValueChange = {
                id_pelanggan = it
            },
            label = {
                Text(
                    text = "Data id_pelanggan",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )


        )

        OutlinedTextField(
            value = nama,
            onValueChange = {
                nama = it
            },
            label = {
                Text(
                    text = "Nama costumer",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.medium,



        )

        OutlinedTextField(
            value = meteran,
            onValueChange = {
                meteran = it
            },
            label = {
                Text(
                    text = "Data meteran",
                    style = TextStyle(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )

                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                cursorColor = MaterialTheme.colorScheme.primary
            ),
            shape = MaterialTheme.shapes.medium,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            )
        )

        Button(
            onClick = { showPreview = true }
        ) {
            Text("Upload foto")
        }

        Button(
            onClick = { showDialog = true }
        ) {
            Text("Submit")
        }

        // submit button (untuk sekarang) jadi menampilkan data data nya saja
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Data yang Dimasukkan") },
                text = {
                    Text(
                        """
                        ID Pelanggan: $id_pelanggan
                        Nama: $nama
                        Meteran: $meteran
                        """.trimIndent()
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("OK")
                    }
                }
            )
        }

        if (showPreview) {
            AlertDialog(
                onDismissRequest = { showPreview = false },
                title = { Text("Data Kamera nanti akan di tampilkan preview nya") },
                text = {
                    Text(
                       text="Buka kamera"
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showPreview  = false }) {
                        Text("OK")
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }

}