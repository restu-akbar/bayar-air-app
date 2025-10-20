package org.com.bayarair.platform


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreGraphics.CGRectMake
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.*
import platform.MobileCoreServices.UTTypeImage
import platform.PhotosUI.*
import platform.UIKit.*
import kotlin.coroutines.resume
import kotlin.math.min

private fun topController(): UIViewController {
    val keyWindow = UIApplication.sharedApplication.keyWindow
        ?: UIApplication.sharedApplication.windows.firstObject as? UIWindow
    var top = keyWindow?.rootViewController
    while (true) {
        top = when {
            top?.presentedViewController != null -> top?.presentedViewController
            top is UINavigationController && top.visibleViewController != null -> top.visibleViewController
            top is UITabBarController && top.selectedViewController != null -> top.selectedViewController
            else -> return top!!
        }
    }
}

private class IosImageGateway : ImageGateway {

    override suspend fun ensureCameraPermission(): Boolean {
        return true
    }

    override suspend fun pickImage(): PickResult? =
        suspendCancellableCoroutine { cont ->
            val config = PHPickerConfiguration().apply {
                selectionLimit = 1
                filter = PHPickerFilter.imagesFilter()
            }
            val picker = PHPickerViewController(configuration = config)

            class Delegate : NSObject(), PHPickerViewControllerDelegateProtocol {
                override fun picker(picker: PHPickerViewController, didFinishPicking: List<*>) {
                    picker.dismissViewControllerAnimated(true, completion = null)
                    val result = didFinishPicking.firstOrNull() as? PHPickerResult
                    if (result == null) {
                        cont.resume(null); return
                    }
                    result.itemProvider.loadDataRepresentationForTypeIdentifier(UTTypeImage.identifier) { data, error ->
                        if (data != null) {
                            val bytes = (data as NSData).toByteArray()
                            cont.resume(PickResult(bytes, "image/*"))
                        } else {
                            cont.resume(null)
                        }
                    }
                }
            }

            val delegate = Delegate()
            picker.delegate = delegate
            objc_setAssociatedObject(picker, "delegate_key", delegate, OBJC_ASSOCIATION_RETAIN)
            topController().presentViewController(picker, animated = true, completion = null)
        }

    override suspend fun captureImage(): PickResult? =
        suspendCancellableCoroutine { cont ->
            if (!UIImagePickerController.isSourceTypeAvailable(
                    UIImagePickerControllerSourceTypeCamera
                )
            ) {
                cont.resume(null); return@suspendCancellableCoroutine
            }
            val picker = UIImagePickerController().apply {
                sourceType = UIImagePickerControllerSourceTypeCamera
                allowsEditing = false
            }

            class Delegate : NSObject(), UIImagePickerControllerDelegateProtocol,
                UINavigationControllerDelegateProtocol {
                override fun imagePickerController(
                    picker: UIImagePickerController,
                    didFinishPickingMediaWithInfo: Map<Any?, *>
                ) {
                    val image =
                        didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
                    picker.dismissViewControllerAnimated(true, completion = null)
                    if (image == null) {
                        cont.resume(null); return
                    }
                    val data = image.jpegData(0.9)
                    cont.resume(
                        if (data != null) PickResult((data as NSData).toByteArray(), "image/jpeg")
                        else null
                    )
                }

                override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                    picker.dismissViewControllerAnimated(true, completion = null)
                    cont.resume(null)
                }
            }

            val delegate = Delegate()
            picker.delegate = delegate
            objc_setAssociatedObject(picker, "delegate_key", delegate, OBJC_ASSOCIATION_RETAIN)
            topController().presentViewController(picker, animated = true, completion = null)
        }
}

private fun NSData.toByteArray(): ByteArray {
    val buffer = ByteArray(this.length.toInt())
    memScoped {
        val bytes = buffer.refTo(0)
        this@toByteArray.getBytes(bytes, this@toByteArray.length)
    }
    return buffer
}

@Composable
actual fun rememberImageGateway(): ImageGateway = remember { IosImageGateway() }

actual fun decodeImage(bytes: ByteArray): ImageBitmap =
    org.jetbrains.skia.Image.makeFromEncoded(bytes).asImageBitmap()


private fun ByteArray.toNSData(): NSData = this.usePinned {
    NSData.create(bytes = it.addressOf(0), length = this.size.toULong())
}

private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    val bytes = ByteArray(length)
    bytes.usePinned {
        memScoped {
            memcpy(it.addressOf(0), this@toByteArray.bytes, length.convert())
        }
    }
    return bytes
}

actual fun compressImage(
    bytes: ByteArray,
    maxWidth: Int?,
    maxHeight: Int?,
    quality: Int
): ByteArray {
    val data = bytes.toNSData()
    val uiImage = UIImage(data = data) ?: return bytes

    val srcW = uiImage.size.useContents { width }
    val srcH = uiImage.size.useContents { height }

    val (targetW, targetH) = if (maxWidth != null && maxHeight != null && srcW > 0 && srcH > 0) {
        val scale = min(
            maxWidth.toDouble() / srcW,
            maxHeight.toDouble() / srcH
        ).coerceAtMost(1.0)
        Pair((srcW * scale), (srcH * scale))
    } else Pair(srcW, srcH)

    UIGraphicsBeginImageContextWithOptions(CGSizeMake(targetW, targetH), false, 1.0)
    uiImage.drawInRect(CGRectMake(0.0, 0.0, targetW, targetH))
    val resized = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()

    val outImage = resized ?: uiImage

    val q = (quality.coerceIn(0, 100).toDouble() / 100.0)
    val jpegData = outImage.JPEGRepresentation(q)

    return (jpegData ?: data).toByteArray()
}

private fun UIImage.JPEGRepresentation(quality: Double): NSData? {
    return UIImageJPEGRepresentation(this, quality)
}

