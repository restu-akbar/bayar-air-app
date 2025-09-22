package org.com.bayarair.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSURL
import platform.PDFKit.PDFDocument
import platform.PDFKit.PDFView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PdfViewer(url: String, modifier: Modifier) {
    UIKitView(
        factory = {
            val pdfView = PDFView()
            pdfView.autoScales = true
            val nsUrl = NSURL.URLWithString(url)
            nsUrl?.let {
                val doc = PDFDocument(it)
                pdfView.document = doc
            }
            pdfView
        },
        modifier = modifier,
        update = NoOp,
        onRelease = NoOp,
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true
        )
    )
}
