@file:OptIn(ExperimentalMaterial3Api::class)

package org.com.bayarair.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import kotlinx.coroutines.launch
import org.com.bayarair.platform.PickResult
import org.com.bayarair.platform.decodeImage
import org.com.bayarair.platform.rememberImageGateway
import org.com.bayarair.presentation.component.LoadingOverlay
import org.com.bayarair.presentation.navigation.HomeTab
import org.com.bayarair.presentation.navigation.LocalPreviousTabKey
import org.com.bayarair.presentation.navigation.ProfileTab
import org.com.bayarair.presentation.navigation.root
import org.com.bayarair.presentation.viewmodel.OtherFee
import org.com.bayarair.presentation.viewmodel.RecordEvent
import org.com.bayarair.presentation.viewmodel.RecordViewModel
import org.com.bayarair.utils.formatRupiah
import org.com.bayarair.utils.groupThousands

object RecordScreen : Screen {
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun Content() {
        val vm: RecordViewModel = koinScreenModel()
        val state by vm.state.collectAsState()
        var expanded by remember { mutableStateOf(false) }
        val focusManager = LocalFocusManager.current

        val navigator = LocalNavigator.current

        val snackbarHostState = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        var showFullLoading by remember { mutableStateOf(false) }
        var loadMessage by remember { mutableStateOf("") }

        val imageGateway = rememberImageGateway()
        var image by remember { mutableStateOf<PickResult?>(null) }
        val imageBitmap: ImageBitmap? = remember(image) { image?.let { decodeImage(it.bytes) } }

        LaunchedEffect(Unit) { vm.load(minMs = 1000).join() }

        LaunchedEffect(Unit) {
            vm.events.collect { ev ->
                when (ev) {
                    is RecordEvent.ShowSnackbar -> snackbarHostState.showSnackbar(ev.message)
                    is RecordEvent.Saved -> {
                        image = null
                        navigator?.root()?.push(RecordDetailScreen(ev.url, ev.recordId, false))
                    }

                    is RecordEvent.ShowLoading -> {
                        showFullLoading = true
                        loadMessage = ev.message
                    }

                    is RecordEvent.Idle -> showFullLoading = false
                }
            }
        }

        fun onOpenCamera() {
            scope.launch {
                val ok = imageGateway.ensureCameraPermission()
                if (!ok) {
                    snackbarHostState.showSnackbar("Izin kamera ditolak")
                    return@launch
                }
                image = imageGateway.captureImage()
                if (image == null) {
                    snackbarHostState.showSnackbar("Gagal mengambil foto")
                }
            }
        }

        fun onPickFromGallery() {
            scope.launch {
                image = imageGateway.pickImage()
                if (image == null) {
                    snackbarHostState.showSnackbar("Tidak ada gambar yang dipilih")
                }
            }
        }

        val bgBlue = MaterialTheme.colorScheme.background
        val green = MaterialTheme.colorScheme.tertiaryContainer
        val formBg = MaterialTheme.colorScheme.primaryContainer
        val textOnBg = MaterialTheme.colorScheme.onBackground

        var isRefreshing by remember { mutableStateOf(false) }
        val pullState = rememberPullRefreshState(
            refreshing = isRefreshing,
            onRefresh = {
                scope.launch {
                    isRefreshing = true
                    vm.load(minMs = 500).join()
                    isRefreshing = false
                }
            }
        )

        StickyScaffold(bgBlue = bgBlue, textOnBg = textOnBg, snackbarHostState)
        {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pullRefresh(pullState)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Catat Air!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = textOnBg,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
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
                                .menuAnchor(MenuAnchorType.PrimaryEditable)
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
                                            Icon(
                                                Icons.Outlined.Close,
                                                contentDescription = "Clear"
                                            )
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
                                    text = { Text("Tidak ada pelanggan yang belum tercatat bulan ini") },
                                    onClick = {},
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
                            disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                alpha = 0.6f
                            ),
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
                            disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                alpha = 0.6f
                            ),
                            focusedBorderColor = formBg,
                            unfocusedBorderColor = formBg,
                            disabledBorderColor = Color.LightGray,
                        )
                    )

                    // ====== Meteran Bulan Lalu (disabled) ======
                    Text(
                        "Meteran Bulan Lalu",
                        style = MaterialTheme.typography.labelLarge,
                        color = textOnBg
                    )
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
                            disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                alpha = 0.6f
                            ),
                            focusedBorderColor = formBg,
                            unfocusedBorderColor = formBg,
                            disabledBorderColor = Color.LightGray,
                        )
                    )

                    // ====== Foto Meteran Bulan Ini ======
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Foto Meteran Bulan Ini",
                        style = MaterialTheme.typography.labelLarge,
                        color = textOnBg
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(formBg)
                            .border(1.dp, Color.LightGray, RoundedCornerShape(4.dp))
                            .clickable { onOpenCamera() }
                            .then(
                                if (imageBitmap != null) Modifier.heightIn(max = 400.dp)
                                else Modifier.height(230.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageBitmap == null) {
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
                                bitmap = imageBitmap,
                                contentDescription = "Foto",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight()
                            )
                        }
                    }


                    // Upload dari galeri
                    FilledTonalButton(
                        onClick = { onPickFromGallery() },
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Icon(Icons.Outlined.Upload, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Upload file dari galeri")
                    }

                    // ====== Meteran Bulan Ini ======
                    Text(
                        "Meteran Bulan Ini",
                        style = MaterialTheme.typography.labelLarge,
                        color = textOnBg
                    )
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
                            disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                alpha = 0.6f
                            ),
                            focusedBorderColor = formBg,
                            unfocusedBorderColor = formBg,
                            disabledBorderColor = Color.LightGray,
                        )
                    )

                    // ====== Biaya Lain-lain ======
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
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
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
                                    onTypeChange = { newType ->
                                        vm.updateFeeType(
                                            item.id,
                                            newType
                                        )
                                    },
                                    onAmountChange = { digits ->
                                        vm.updateFeeAmount(
                                            item.id,
                                            digits
                                        )
                                    },
                                    onRemove = { vm.removeFee(item.id) },
                                    formBg = formBg
                                )
                            }
                        }
                    }

                    // ====== Total ======
                    Text(
                        "Total Harga Bayar",
                        style = MaterialTheme.typography.labelLarge,
                        color = textOnBg
                    )
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
                            disabledTextColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                alpha = 0.8f
                            ),
                            focusedBorderColor = formBg,
                            unfocusedBorderColor = formBg,
                            disabledBorderColor = Color.LightGray
                        )
                    )

                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = { vm.saveRecord(image = image?.bytes) },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = green, contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            if (state.isLoading) "Menyimpan..." else "Simpan",
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                }
                if (showFullLoading) {
                    LoadingOverlay(loadMessage)
                } else {
                    PullRefreshIndicator(
                        refreshing = isRefreshing,
                        state = pullState,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
            }
        }
    }
}

@Composable
fun StickyScaffold(
    bgBlue: Color,
    textOnBg: Color,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable () -> Unit
) {
    val tabNavigator = LocalTabNavigator.current
    val prevKey = LocalPreviousTabKey.current.value

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()


    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = bgBlue,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(
                        onClick = {
                            tabNavigator.current = when (prevKey) {
                                HomeTab.key -> HomeTab
                                ProfileTab.key -> ProfileTab
                                else -> HomeTab
                            }
                        },
                        modifier = Modifier
                            .padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Kembali",
                            tint = textOnBg,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bgBlue,
                    scrolledContainerColor = bgBlue,
                    navigationIconContentColor = textOnBg
                ),
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(0)
            )
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { innerPadding ->
        Box(Modifier.padding(innerPadding)) {
            content()
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
    var amountDigits by remember(fee.id) { mutableStateOf(fee.amount) }

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
                Text(
                    "Biaya #${index + 1}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
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
                        .menuAnchor(MenuAnchorType.PrimaryEditable, true)
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
        val prefix = 3
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
