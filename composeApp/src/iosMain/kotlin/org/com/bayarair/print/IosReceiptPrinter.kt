package org.com.bayarair.print

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.com.bayarair.utils.ReceiptPrinter
import platform.Foundation.NSData
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.Foundation.NSURLSession
import platform.Foundation.dataTaskWithRequest
import platform.UIKit.UIApplication
import platform.UIKit.UINavigationController
import platform.UIKit.UIPrintInfo
import platform.UIKit.UIPrintInfoOutputType
import platform.UIKit.UIPrintInteractionController
import platform.UIKit.UITabBarController
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import kotlin.coroutines.resume

class IosReceiptPrinter : ReceiptPrinter {

    override suspend fun printPdfFromUrl(
        url: String,
        forcePick: Boolean
    ): Boolean {
        val pdfData = downloadPdf(url) ?: return false
        return presentPrintController(pdfData, jobName = lastPath(url))
    }
    private suspend fun downloadPdf(url: String): NSData? =
        suspendCancellableCoroutine { cont ->
            val nsUrl = NSURL.URLWithString(url)
            if (nsUrl == null) {
                cont.resume(null); return@suspendCancellableCoroutine
            }
            val request = NSURLRequest.requestWithURL(nsUrl)
            val task =
                NSURLSession.sharedSession.dataTaskWithRequest(request) { data, response, error ->
                    if (error != null) {
                        cont.resume(null)
                    } else {
                        cont.resume(data)
                    }
                }
            task.resume()
            cont.invokeOnCancellation { task.cancel() }
        }

    private fun lastPath(url: String): String =
        (NSURL.URLWithString(url)?.lastPathComponent) ?: "Receipt.pdf"

    private suspend fun presentPrintController(pdfData: NSData, jobName: String): Boolean =
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { cont ->
                val controller = UIPrintInteractionController.sharedPrintController()
                val info = UIPrintInfo.printInfo()
                info.outputType = UIPrintInfoOutputType.UIPrintInfoOutputGeneral
                info.jobName = jobName
                controller.printInfo = info

                controller.printingItem = pdfData

                val presenter = topViewController() ?: run {
                   cont.resume(false); return@suspendCancellableCoroutine
                }

                controller.presentAnimated(true) { _, completed, error ->
                    val ok = (completed && error == null)
                    cont.resume(ok)
                }
            }
        }

    private fun topViewController(): UIViewController? {
        val app = UIApplication.sharedApplication
        val keyWin = app.keyWindow ?: app.windows.firstOrNull() as? UIWindow
        var top = keyWin?.rootViewController ?: return null
        while (true) {
            top = when {
                top.presentedViewController != null -> top.presentedViewController!!
                top is UINavigationController && top.visibleViewController != null -> top.visibleViewController!!
                top is UITabBarController && top.selectedViewController != null -> top.selectedViewController!!
                else -> return top
            }
        }
    }
}
