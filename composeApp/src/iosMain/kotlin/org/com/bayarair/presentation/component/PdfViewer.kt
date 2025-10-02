package org.com.bayarair.presentation.component

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitInteropProperties
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.StableRef
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGPointMake
import platform.Foundation.NSSelectorFromString
import platform.Foundation.NSURL
import platform.PDFKit.PDFDocument
import platform.PDFKit.PDFView
import platform.UIKit.UIActivityIndicatorView
import platform.UIKit.UIActivityIndicatorViewStyleLarge
import platform.UIKit.UIColor
import platform.UIKit.UIControlEventValueChanged
import platform.UIKit.UIRefreshControl
import platform.UIKit.UIScrollView
import platform.darwin.NSObject
import platform.objc.OBJC_ASSOCIATION_RETAIN_NONATOMIC
import platform.objc.objc_getAssociatedObject
import platform.objc.objc_setAssociatedObject

private class RefreshHandler(private val onRefresh: () -> Unit) : NSObject() {
    @OptIn(BetaInteropApi::class)
    @Suppress("unused")
    @ObjCAction
    fun handleRefresh() {
        onRefresh()
    }
}

@OptIn(ExperimentalForeignApi::class)
private val RefreshHandlerKey: COpaquePointer = StableRef.create(Any()).asCPointer()

@OptIn(ExperimentalForeignApi::class)
private val PdfViewKey: COpaquePointer = StableRef.create(Any()).asCPointer()

@OptIn(ExperimentalForeignApi::class)
private val RefreshCtrlKey: COpaquePointer = StableRef.create(Any()).asCPointer()

@OptIn(ExperimentalForeignApi::class)
private val SpinnerKey: COpaquePointer = StableRef.create(Any()).asCPointer()

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun PdfViewer(url: String, modifier: Modifier) {
    UIKitView(
        factory = {
            val scroll = UIScrollView().apply {
                alwaysBounceVertical = true
                backgroundColor = UIColor.clearColor
            }

            val pdfView = PDFView().apply {
                autoScales = true
                displayDirection = 0
                backgroundColor = UIColor.clearColor
                translatesAutoresizingMaskIntoConstraints = false
            }

            val spinner = UIActivityIndicatorView(UIActivityIndicatorViewStyleLarge).apply {
                hidesWhenStopped = true
                startAnimating()
            }
            scroll.addSubview(spinner)

            fun centerSpinner() {
                val size = scroll.bounds.useContents { this.size }
                spinner.center = CGPointMake(size.width / 2.0, size.height / 2.0)
            }

            fun loadDocument(u: String) {
                spinner.startAnimating()
                NSURL.URLWithString(u)?.let { pdfView.document = PDFDocument(it) }
                spinner.stopAnimating()
            }

            scroll.addSubview(pdfView)

            val guide = scroll.contentLayoutGuide
            val frameGuide = scroll.frameLayoutGuide
            pdfView.leadingAnchor.constraintEqualToAnchor(guide.leadingAnchor).active = true
            pdfView.trailingAnchor.constraintEqualToAnchor(guide.trailingAnchor).active = true
            pdfView.topAnchor.constraintEqualToAnchor(guide.topAnchor).active = true
            pdfView.bottomAnchor.constraintEqualToAnchor(guide.bottomAnchor).active = true
            pdfView.widthAnchor.constraintEqualToAnchor(frameGuide.widthAnchor).active = true

            loadDocument(url)

            centerSpinner()

            val refresh = UIRefreshControl()
            val handler = RefreshHandler {
                spinner.startAnimating()
                loadDocument(url)
                refresh.endRefreshing()
                spinner.stopAnimating()
            }

            objc_setAssociatedObject(
                refresh,
                RefreshHandlerKey,
                handler,
                OBJC_ASSOCIATION_RETAIN_NONATOMIC
            )
            objc_setAssociatedObject(scroll, PdfViewKey, pdfView, OBJC_ASSOCIATION_RETAIN_NONATOMIC)
            objc_setAssociatedObject(
                scroll,
                RefreshCtrlKey,
                refresh,
                OBJC_ASSOCIATION_RETAIN_NONATOMIC
            )
            objc_setAssociatedObject(scroll, SpinnerKey, spinner, OBJC_ASSOCIATION_RETAIN_NONATOMIC)

            refresh.addTarget(
                target = handler,
                action = NSSelectorFromString("handleRefresh"),
                forControlEvents = UIControlEventValueChanged
            )
            scroll.refreshControl = refresh

            scroll.layoutIfNeeded()
            centerSpinner()

            scroll
        },
        modifier = modifier,
        update = { scroll ->
            // reposition spinner (misal orientation change)
            val spinner = objc_getAssociatedObject(scroll, SpinnerKey) as? UIActivityIndicatorView
            spinner?.let {
                val size = scroll.bounds.useContents { this.size }
                it.center = CGPointMake(size.width / 2.0, size.height / 2.0)
            }

            val pdfView = objc_getAssociatedObject(scroll, PdfViewKey) as? PDFView
            val currentUrl = pdfView?.document?.documentURL()?.absoluteString ?: ""
            if (currentUrl != url) {
                val spinnerNow = spinner
                spinnerNow?.startAnimating()
                NSURL.URLWithString(url)?.let { pdfView?.document = PDFDocument(it) }
                spinnerNow?.stopAnimating()
            }
        },
        onRelease = { scroll ->
            objc_getAssociatedObject(scroll, RefreshCtrlKey)?.let { refreshAny ->
                objc_setAssociatedObject(
                    refreshAny,
                    RefreshHandlerKey,
                    null,
                    OBJC_ASSOCIATION_RETAIN_NONATOMIC
                )
            }

            objc_setAssociatedObject(scroll, PdfViewKey, null, OBJC_ASSOCIATION_RETAIN_NONATOMIC)
            objc_setAssociatedObject(
                scroll,
                RefreshCtrlKey,
                null,
                OBJC_ASSOCIATION_RETAIN_NONATOMIC
            )
            objc_setAssociatedObject(scroll, SpinnerKey, null, OBJC_ASSOCIATION_RETAIN_NONATOMIC)
        },
        properties = UIKitInteropProperties(
            isInteractive = true,
            isNativeAccessibilityEnabled = true
        )
    )
}
