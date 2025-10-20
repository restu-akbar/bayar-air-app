package org.com.bayarair.presentation.viewmodel

import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.com.bayarair.data.model.Customer
import org.com.bayarair.data.repository.CustomerRepository
import org.com.bayarair.data.repository.RecordRepository
import org.com.bayarair.platform.compressImage

class RecordViewModel(
    private val repo: RecordRepository,
    private val custRepo: CustomerRepository,
    private val historyShared: RecordHistoryShared,
    private val statsShared: StatsShared,
) : StateScreenModel<RecordState>(RecordState()) {
    private val _events = MutableSharedFlow<RecordEvent>()
    val events: SharedFlow<RecordEvent> = _events.asSharedFlow()

    fun load() {
        screenModelScope.launch {
            mutableState.update { it.copy(isLoading = true) }
            try {
                val hargaDef = async { repo.getHarga() }
                val custDef = async { custRepo.getCustomers() }

                val hargaRes = hargaDef.await()
                val custRes = custDef.await()

                hargaRes
                    .onSuccess { harga ->
                        mutableState.update {
                            it.copy(
                                air = harga.air,
                                admin = harga.admin,
                            )
                        }
                    }.onFailure { e ->
                        _events.emit(
                            RecordEvent.ShowSnackbar(
                                e.message
                                    ?: "Data harga layanan belum ada, silakan hubungi admin.",
                            ),
                        )
                    }

                custRes
                    .onSuccess { list ->
                        mutableState.update { it.copy(customers = list) }
                    }.onFailure { e ->
                        _events.emit(
                            RecordEvent.ShowSnackbar(
                                e.message
                                    ?: "Belum ada pelanggan yang terdaftar, silakan hubungi admin.",
                            ),
                        )
                    }
            } finally {
                mutableState.update { it.copy(isLoading = false) }
            }
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
                alamat = c?.address.orElse(""),
                hp = c?.hp.orElse(""),
                meterLalu = c?.meterLalu ?: 0,
            )
        }
    }

    fun clearCustomer() =
        mutableState.update {
            it.copy(selectedCustomerId = "", searchText = "", alamat = "", hp = "", meterLalu = 0)
        }

    fun setMeteranText(raw: String) =
        mutableState.update { it.copy(meteranText = raw.filter(Char::isDigit)) }

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

    fun removeFee(id: Long) =
        mutableState.update { it.copy(otherFees = it.otherFees.filterNot { f -> f.id == id }) }

    fun saveRecord(image: ByteArray?) {
        screenModelScope.launch {
            try {
                val st = state.value
                if (st.air <= 0L || st.admin <= 0L) {
                    _events.emit(RecordEvent.ShowSnackbar("Data harga layanan belum ada, silakan hubungi admin."))
                    return@launch
                }
                if (st.selectedCustomerId.isBlank()) {
                    _events.emit(RecordEvent.ShowSnackbar("Pilih pelanggan terlebih dahulu"))
                    return@launch
                }
                if (image == null || image.isEmpty()) {
                    _events.emit(RecordEvent.ShowSnackbar("Ambil foto meteran"))
                    return@launch
                }
                val compressed: ByteArray =
                    compressImage(
                        bytes = image,
                        maxWidth = 1280,
                        maxHeight = 1280,
                        quality = 80,
                    )
                if (st.meteranText.isBlank()) {
                    _events.emit(RecordEvent.ShowSnackbar("Isi meteran bulan ini"))
                    return@launch
                }
                val current = st.meteranText.filter(Char::isDigit).toLongOrNull()
                if (current == null) {
                    _events.emit(RecordEvent.ShowSnackbar("Meteran tidak valid"))
                    return@launch
                }
                if (current < st.meterLalu.toLong()) {
                    _events.emit(RecordEvent.ShowSnackbar("Meteran bulan ini tidak boleh lebih kecil dari bulan lalu (${st.meterLalu})"))
                    return@launch
                }
                mutableState.update { it.copy(loadingMessage = "Menyimpan data..") }
                mutableState.update { it.copy(isLoading = true) }
                val fees: Map<String, Long> =
                    st.otherFees
                        .mapNotNull { f -> f.type?.let { it to (f.amount.toLongOrNull() ?: 0L) } }
                        .toMap()

                repo
                    .saveRecord(
                        customerId = st.selectedCustomerId,
                        meter = current.toInt(),
                        evidence = compressed,
                        otherFees = fees,
                    ).onSuccess { env ->
                        _events.emit(RecordEvent.ShowSnackbar(env.message))
                        resetForm()
                        _events.emit(
                            RecordEvent.Saved(
                                env.data!!.receipt,
                                env.data.id,
                            ),
                        )
                        historyShared.prepend(env.data)
                        statsShared.clearPieChart()
                        statsShared.clearBarChart()
                    }.onFailure { e ->
                        _events.emit(RecordEvent.ShowSnackbar(e.message ?: "Gagal menyimpan"))
                    }
            } catch (t: Throwable) {
                _events.emit(RecordEvent.ShowSnackbar(t.message ?: "Terjadi kesalahan"))
            } finally {
                mutableState.update { it.copy(isLoading = true) }
                mutableState.update { it.copy(loadingMessage = "") }
            }
        }
    }

    fun resetForm() {
        mutableState.update {
            it.copy(
                searchText = "",
                selectedCustomerId = "",
                alamat = "",
                hp = "",
                meterLalu = 0,
                meteranText = "",
                otherFees = emptyList(),
            )
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
    val loadingMessage: String = "",
    val air: Long = 0L,
    val admin: Long = 0L,
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

    val subtotalAir: Long
        get() = (pemakaian * air).let { if (it > 0L) it + admin else it }
    val totalLainLain: Long get() = otherFees.sumOf { it.amount.toLongOrNull() ?: 0L }
    val totalBayar: Long get() = subtotalAir + totalLainLain
}

sealed interface RecordEvent {
    data class ShowSnackbar(
        val message: String,
    ) : RecordEvent

    data class Saved(
        val url: String,
        val recordId: String,
    ) : RecordEvent
}
