package org.com.bayarair.presentation.viewmodel

import android.graphics.Bitmap
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.com.bayarair.data.model.Customer
import org.com.bayarair.data.repository.RecordRepository
import java.io.ByteArrayOutputStream

class RecordScreenModel(
    private val repo: RecordRepository,
) : StateScreenModel<RecordState>(RecordState()) {
    private val _events = MutableSharedFlow<RecordEvent>()
    val events: SharedFlow<RecordEvent> = _events.asSharedFlow()

    fun load() {
        mutableState.update { it.copy(isLoading = true) }
        screenModelScope.launch {
            val hargaDef = async { repo.getHarga() }
            val custDef = async { repo.getCustomers() }

            val hargaRes = hargaDef.await()
            val custRes = custDef.await()

            hargaRes
                .onSuccess { harga ->
                    mutableState.update { it.copy(hargaPerM3 = harga) }
                }.onFailure {
                    _events.emit(RecordEvent.ShowSnackbar(it.message ?: "Gagal memuat harga"))
                }

            custRes
                .onSuccess { list ->
                    mutableState.update { it.copy(customers = list) }
                }.onFailure {
                    _events.emit(RecordEvent.ShowSnackbar(it.message ?: "Gagal memuat pelanggan"))
                }

            mutableState.update { it.copy(isLoading = false) }
        }
    }

    // ------- Pelanggan -------
    fun setSearchText(text: String) = mutableState.update { it.copy(searchText = text) }

    fun selectCustomer(customerId: String) {
        val c = state.value.customers.firstOrNull { it.id == customerId }
        mutableState.update {
            it.copy(
                selectedCustomerId = customerId,
                searchText = c?.name.orElse(""),
                alamat = c?.alamat.orElse(""),
                hp = c?.hp.orElse(""),
                meterLalu = c?.meterLalu ?: 0,
            )
        }
    }

    fun clearCustomer() =
        mutableState.update {
            it.copy(selectedCustomerId = "", searchText = "", alamat = "", hp = "", meterLalu = 0)
        }

    // ------- Meteran -------
    fun setMeteranText(raw: String) = mutableState.update { it.copy(meteranText = raw.filter(Char::isDigit)) }

    fun setTariff(newTariff: Long) = mutableState.update { it.copy(hargaPerM3 = newTariff) }

    // ------- Biaya lain-lain -------
    fun addOtherFee() {
        val st = state.value
        val available = st.availableTypes
        if (available.isEmpty()) {
            screenModelScope.launch {
                _events.emit(
                    RecordEvent.ShowSnackbar(
                        "Semua jenis biaya (${st.typeOptions.joinToString()}) sudah ditambahkan",
                    ),
                )
            }
            return
        }
        val newFee = OtherFee(type = available.first(), amount = "")
        mutableState.update { it.copy(otherFees = it.otherFees + newFee) }
    }

    fun updateFeeType(
        id: Long,
        newType: String,
    ) {
        val st = state.value
        if (newType in st.usedTypes && st.otherFees.firstOrNull { f -> f.id == id }?.type != newType) {
            screenModelScope.launch {
                _events.emit(RecordEvent.ShowSnackbar("$newType sudah dipakai"))
            }
            return
        }
        mutableState.update {
            it.copy(otherFees = it.otherFees.map { f -> if (f.id == id) f.copy(type = newType) else f })
        }
    }

    fun updateFeeAmount(
        id: Long,
        digits: String,
    ) {
        val clean = digits.filter(Char::isDigit)
        mutableState.update {
            it.copy(otherFees = it.otherFees.map { f -> if (f.id == id) f.copy(amount = clean) else f })
        }
    }

    fun removeFee(id: Long) = mutableState.update { it.copy(otherFees = it.otherFees.filterNot { f -> f.id == id }) }

    fun saveRecord(bitmap: Bitmap?) {
        val st = state.value
        if (st.selectedCustomerId.isBlank()) {
            screenModelScope.launch { _events.emit(RecordEvent.ShowSnackbar("Pilih pelanggan dulu")) }
            return
        }
        if (st.meteranText.isBlank()) {
            screenModelScope.launch { _events.emit(RecordEvent.ShowSnackbar("Isi meteran bulan ini")) }
            return
        }
        if (bitmap == null) {
            screenModelScope.launch { _events.emit(RecordEvent.ShowSnackbar("Ambil foto meteran")) }
            return
        }

        mutableState.update { it.copy(isLoading = true) }
        screenModelScope.launch {
            try {
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
                val photoBytes = stream.toByteArray()

                val fees =
                    st.otherFees.associate {
                        it.type!! to (it.amount.toLongOrNull() ?: 0L)
                    }

                repo
                    .saveRecord(
                        customerId = st.selectedCustomerId,
                        meter = st.meteranText.toInt(),
                        totalAmount = st.totalBayar,
                        evidence = photoBytes,
                        otherFees = fees,
                    ).onSuccess {
                        _events.emit(RecordEvent.ShowSnackbar("Data berhasil disimpan"))
                        _events.emit(RecordEvent.ClearImage)
                        _events.emit(RecordEvent.Saved)
                        clearCustomer()
                        mutableState.update { it.copy(meteranText = "", otherFees = emptyList()) }
                    }.onFailure {
                        _events.emit(RecordEvent.ShowSnackbar(it.message ?: "Gagal menyimpan"))
                    }
            } finally {
                mutableState.update { it.copy(isLoading = false) }
            }
        }
    }
}

private fun String?.orElse(fallback: String) = this ?: fallback

data class OtherFee(
    val id: Long = System.nanoTime(),
    val type: String? = null,
    val amount: String = "",
)

data class RecordState(
    val isLoading: Boolean = false,
    val hargaPerM3: Long = 0L,
    val customers: List<Customer> = emptyList(),
    val searchText: String = "",
    val selectedCustomerId: String = "",
    val alamat: String = "",
    val hp: String = "",
    val meterLalu: Int = 0,
    val meteranText: String = "",
    val otherFees: List<OtherFee> = emptyList(),
    val typeOptions: List<String> = listOf("Denda", "Retribusi", "Materai"),
) {
    val usedTypes: Set<String> get() = otherFees.mapNotNull { it.type }.toSet()
    val availableTypes: List<String> get() = typeOptions.filterNot { it in usedTypes }

    val filteredCustomers: List<Customer>
        get() =
            if (searchText.isBlank()) {
                customers
            } else {
                customers.filter { it.name.contains(searchText, ignoreCase = true) }
            }

    val pemakaian: Long
        get() = ((meteranText.toLongOrNull() ?: 0L) - meterLalu.toLong()).coerceAtLeast(0L)

    val subtotalAir: Long get() = pemakaian * hargaPerM3
    val totalLainLain: Long get() = otherFees.sumOf { it.amount.toLongOrNull() ?: 0L }
    val totalBayar: Long get() = subtotalAir + totalLainLain
}

sealed interface RecordEvent {
    data class ShowSnackbar(
        val message: String,
    ) : RecordEvent

    object Saved : RecordEvent

    object ClearImage : RecordEvent
}
