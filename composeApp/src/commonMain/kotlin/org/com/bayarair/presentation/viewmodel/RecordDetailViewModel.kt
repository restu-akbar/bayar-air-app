package org.com.bayarair.presentation.viewmodel

import android.util.Log
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import org.com.bayarair.presentation.viewmodel.RecordDetailEvent
import org.com.bayarair.utils.ReceiptPrinter

class RecordDetailViewModel : ScreenModel {
    private val _events = MutableSharedFlow<RecordDetailEvent>()
    val events: SharedFlow<RecordDetailEvent> = _events.asSharedFlow()

    fun printReceipt(
        printer: ReceiptPrinter?,
        url: String,
    ) {
        if (printer == null) {
            screenModelScope.launch {
                _events.emit(RecordDetailEvent.ShowSnackbar("Fitur printer belum tersedia di device ini"))
            }
            return
        }

        screenModelScope.launch {
            _events.emit(RecordDetailEvent.ShowLoading("Print Struk…"))
            try {
                Log.d(
                    "recorddetail",
                    url,
                )
                val result = printer.printPdfFromUrl(url)
                if (result) {
                    _events.emit(RecordDetailEvent.ShowSnackbar("Struk berhasil dicetak!"))
                    _events.emit(RecordDetailEvent.NavigateNext)
                }
            } catch (e: Throwable) {
                _events.emit(
                    RecordDetailEvent.ShowSnackbar("Gagal cetak: ${e.message ?: "Unknown error"}"),
                )
            } finally {
                _events.emit(RecordDetailEvent.HideLoading)
            }
        }
    }
}

sealed interface RecordDetailEvent {
    data class ShowSnackbar(
        val message: String,
    ) : RecordDetailEvent

    data class ShowLoading(
        val message: String = "Print Struk…",
    ) : RecordDetailEvent

    data object HideLoading : RecordDetailEvent

    data object NavigateNext : RecordDetailEvent
}
