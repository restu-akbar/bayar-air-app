@file:OptIn(ExperimentalMaterial3Api::class)

package org.com.bayarair.presentation.screens

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.Manifest
import android.content.Context
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.automirrored.outlined.ArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.currentOrThrow
import org.com.bayarair.data.model.Customer
import org.com.bayarair.utils.formatRupiah
import org.com.bayarair.presentation.viewmodel.RecordScreenModel
import org.com.bayarair.presentation.viewmodel.RecordEvent
import org.com.bayarair.presentation.viewmodel.OtherFee
import org.com.bayarair.utils.groupThousands
import org.com.bayarair.presentation.navigation.HomeTab

object RecordScreen : Screen {
    @Composable
    override fun Content() {
        val vm: RecordScreenModel = koinScreenModel()
        val state by vm.state.collectAsState()
        val tabNavigator = LocalTabNavigator.current

        val context = LocalContext.current
        val focusManager = LocalFocusManager.current
        var expanded by remember { mutableStateOf(false) }
        var photoUri by remember { mutableStateOf<Uri?>(null) }
        var bitmap by remember { mutableStateOf<Bitmap?>(null) }
        
        val snackbarHostState = remember { SnackbarHostState() }
        
        LaunchedEffect(Unit) { vm.load() }
        LaunchedEffect(Unit) {
            vm.events.collect { ev ->
                when (ev) {
                    is RecordEvent.ShowSnackbar -> snackbarHostState.showSnackbar(ev.message)
                    RecordEvent.ClearImage -> bitmap = null
                    RecordEvent.Saved -> tabNavigator.current = HomeTab
                }
            }
        }
        
        val takePictureLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                val uri = photoUri ?: return@rememberLauncherForActivityResult
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    bitmap = BitmapFactory.decodeStream(stream)
                }
            }
        }
        
        fun openCamera() {
            val imageFile = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "meter_${System.currentTimeMillis()}.jpg"
            )
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                imageFile
            )
            photoUri = uri
            takePictureLauncher.launch(uri)
        }
        
        val cameraPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) openCamera()
        }
        
        val pickImageLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {
                context.contentResolver.openInputStream(it)?.use { stream ->
                    bitmap = BitmapFactory.decodeStream(stream)
                }
            }
        }

        val bgBlue = MaterialTheme.colorScheme.background
        val green = MaterialTheme.colorScheme.tertiaryContainer
        val formBg = MaterialTheme.colorScheme.primaryContainer
        val textOnBg = MaterialTheme.colorScheme.onBackground

        Scaffold(
            containerColor = bgBlue,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Judul
                Text(
                    text = "Catat Air!",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = textOnBg,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                // ====== Pelanggan (dropdown) ======
                Text("Pelanggan", style = MaterialTheme.typography.labelLarge, color = textOnBg)

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = state.searchText,
                        onValueChange = {
                            vm.setSearchText(it)
                            if (!expanded) expanded = true
                        },
                        singleLine = true,
                        placeholder = { Text("Cari nama pelanggan…") },
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = {
                            Row(
                                modifier = Modifier.width(96.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (state.searchText.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            vm.clearCustomer()
                                            expanded = false
                                            focusManager.clearFocus()
                                        }
                                    ) {
                                        Icon(Icons.Outlined.Close, contentDescription = "Clear")
                                    }
                                } else {
                                    Spacer(Modifier.size(60.dp))
                                }
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = formBg,
                            unfocusedContainerColor = formBg,
                            disabledContainerColor = formBg,
                            focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            focusedBorderColor = formBg,
                            unfocusedBorderColor = formBg,
                            disabledBorderColor = formBg,
                            cursorColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        val items = state.filteredCustomers
                        if (items.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("Tidak ditemukan") },
                                onClick = { },
                                enabled = false
                            )
                        } else {
                            items.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.name) },
                                    onClick = {
                                        vm.selectCustomer(c.id)
                                        expanded = false
                                        focusManager.clearFocus()
                                    }
                                )
                            }
                        }
                    }
                }

                // ====== Alamat (disabled) ======
                Text("Alamat", style = MaterialTheme.typography.labelLarge, color = textOnBg)
                OutlinedTextField(
                    value = state.alamat,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    singleLine = false,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = formBg,
                        unfocusedContainerColor = formBg,
                        disabledContainerColor = Color.LightGray,
                        focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                        focusedBorderColor = formBg,
                        unfocusedBorderColor = formBg,
                        disabledBorderColor = Color.LightGray,
                    )
                )

                // ====== Nomor HP (disabled) ======
                Text("Nomor HP", style = MaterialTheme.typography.labelLarge, color = textOnBg)
                OutlinedTextField(
                    value = state.hp,
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = formBg,
                        unfocusedContainerColor = formBg,
                        disabledContainerColor = Color.LightGray,
                        focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                        focusedBorderColor = formBg,
                        unfocusedBorderColor = formBg,
                        disabledBorderColor = Color.LightGray,
                    )
                )

                // ====== Meteran Bulan Lalu (disabled) ======
                Text("Meteran Bulan Lalu", style = MaterialTheme.typography.labelLarge, color = textOnBg)
                OutlinedTextField(
                    value = state.meterLalu.toString(),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = formBg,
                        unfocusedContainerColor = formBg,
                        disabledContainerColor = Color.LightGray,
                        focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                        focusedBorderColor = formBg,
                        unfocusedBorderColor = formBg,
                        disabledBorderColor = Color.LightGray,
                    ),
                )

                // ====== Foto Meteran Bulan Ini ======
                Spacer(Modifier.height(3.dp))
                Text("Foto Meteran Bulan Ini", style = MaterialTheme.typography.labelLarge, color = textOnBg)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(formBg)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                        .clickable { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }
                        .then(
                            if (bitmap != null) Modifier.heightIn(max = 400.dp)
                            else Modifier.height(230.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap == null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Outlined.PhotoCamera,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Ketuk untuk ambil foto",
                                color = MaterialTheme.colorScheme.secondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Foto",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentHeight()
                        )
                    }
                }

                // Tombol upload (di bawah box)
                FilledTonalButton(
                    onClick = { pickImageLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Icon(Icons.Outlined.Upload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Upload file dari galeri")
                }

                // ====== Input angka meteran (trigger biaya lain-lain) ======
                Text("Meteran Bulan Ini", style = MaterialTheme.typography.labelLarge, color = textOnBg)
                OutlinedTextField(
                    value = state.meteranText,
                    onValueChange = { vm.setMeteranText(it) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = state.selectedCustomerId.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = formBg,
                        unfocusedContainerColor = formBg,
                        disabledContainerColor = Color.LightGray,
                        focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                        focusedBorderColor = formBg,
                        unfocusedBorderColor = formBg,
                        disabledBorderColor = Color.LightGray,
                    ),
                )

                // ====== Biaya Lain-lain (opsional) ======
                AnimatedVisibility(visible = state.meteranText.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Biaya Lain-lain (opsional)",
                            style = MaterialTheme.typography.titleSmall,
                            color = textOnBg
                        )

                        FilledTonalButton(
                            onClick = { vm.addOtherFee() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Outlined.Add, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Tambah Biaya")
                        }

                        state.otherFees.forEachIndexed { index, item ->
                            BiayaItemRow(
                                index = index,
                                fee = item,
                                typeOptions = state.typeOptions,
                                usedTypes = state.usedTypes,
                                onTypeChange = { newType -> vm.updateFeeType(item.id, newType) },
                                onAmountChange = { digits -> vm.updateFeeAmount(item.id, digits) },
                                onRemove = { vm.removeFee(item.id) },
                                formBg = formBg
                            )
                        }
                    }
                }

                // ====== Total Harga Bayar (readonly) ======
                Text("Total Harga Bayar", style = MaterialTheme.typography.labelLarge, color = textOnBg)
                OutlinedTextField(
                    value = formatRupiah(state.totalBayar),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    singleLine = true,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = formBg,
                        unfocusedContainerColor = formBg,
                        disabledContainerColor = Color.LightGray,
                        focusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        focusedBorderColor = formBg,
                        unfocusedBorderColor = formBg,
                        disabledBorderColor = Color.LightGray
                    ),
                )

                // ====== Tombol simpan ======
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { vm.saveRecord(bitmap) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = green,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        if (state.isLoading) "Memuat..." else "Simpan",
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BiayaItemRow(
    index: Int,
    fee: OtherFee,
    typeOptions: List<String>,
    usedTypes: Set<String>,
    onTypeChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onRemove: () -> Unit,
    formBg: Color
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedType by remember(fee.id) { mutableStateOf(fee.type.orEmpty()) }
    var amountDigits by remember(fee.id) { mutableStateOf(fee.amount) } // digit-only

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Biaya #${index + 1}", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onRemove) {
                    Icon(Icons.Outlined.Delete, contentDescription = "Hapus")
                }
            }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = selectedType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Jenis") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = formBg,
                        unfocusedContainerColor = formBg
                    )
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    typeOptions.forEach { opt ->
                        val alreadyUsedByOther = opt in usedTypes && opt != selectedType
                        DropdownMenuItem(
                            text = { Text(opt) },
                            onClick = {
                                if (!alreadyUsedByOther) {
                                    selectedType = opt
                                    onTypeChange(opt)
                                    expanded = false
                                }
                            },
                            enabled = !alreadyUsedByOther
                        )
                    }
                }
            }

            // amountDigits = digit-only
            OutlinedTextField(
                value = amountDigits,
                onValueChange = { input ->
                    val digits = input.filter(Char::isDigit)
                    amountDigits = digits
                    onAmountChange(digits)
                },
                singleLine = true,
                label = { Text("Nominal (Rp)") },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                visualTransformation = RupiahVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = formBg,
                    unfocusedContainerColor = formBg
                ),
                placeholder = { Text("Rp 0") }
            )
        }
    }
}

private class RupiahVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text.filter(Char::isDigit)
        val grouped = groupThousands(raw)
        val out = "Rp " + grouped
        val prefix = 3 // "Rp "
        val n = raw.length

        val orig2trans = IntArray(n + 1)
        val firstGroup = if (n % 3 == 0) 3 else n % 3
        for (k in 0..n) {
            val sepBefore = if (k <= firstGroup) 0 else ((k - firstGroup - 1) / 3) + 1
            orig2trans[k] = prefix + k + sepBefore
        }

        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val k = offset.coerceIn(0, n)
                return orig2trans[k]
            }
            override fun transformedToOriginal(offset: Int): Int {
                val t = offset.coerceAtLeast(0)
                // cari k terbesar dengan orig2trans[k] <= t
                var k = 0
                for (i in 0..n) {
                    if (orig2trans[i] <= t) k = i else break
                }
                return k
            }
        }
        return TransformedText(AnnotatedString(out), mapping)
    }
}

