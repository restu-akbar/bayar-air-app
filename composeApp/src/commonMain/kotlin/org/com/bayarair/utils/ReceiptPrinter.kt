package org.com.bayarair.utils

import androidx.compose.runtime.staticCompositionLocalOf

interface ReceiptPrinter {
    suspend fun printPdfFromUrl(url: String)
}

val LocalReceiptPrinter = staticCompositionLocalOf<ReceiptPrinter?> { null }
