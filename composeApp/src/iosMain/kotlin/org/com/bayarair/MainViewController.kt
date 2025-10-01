package org.com.bayarair

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import org.com.bayarair.core.AppEvents
import org.com.bayarair.platform.IosDI
import org.com.bayarair.print.IosReceiptPrinter
import org.com.bayarair.utils.LocalReceiptPrinter
import org.com.bayarair.utils.ReceiptPrinter


fun MainViewController() = ComposeUIViewController {
    val appEvents: AppEvents = IosDI.appEvents
    val printer: ReceiptPrinter = IosReceiptPrinter()

    CompositionLocalProvider(LocalReceiptPrinter provides printer) {
        App(appEvents)
    }
}
