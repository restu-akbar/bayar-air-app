package org.com.bayarair.platform


import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.ImageBitmap
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSData
import platform.MobileCoreServices.UTTypeImage
import platform.PhotosUI.*
import platform.UIKit.*
import kotlin.coroutines.resume

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
        // iOS akan handle sendiri saat first access; di sini kembalikan true saja.
        // (Kalau mau lebih ketat, cek AVAuthorizationStatus.video)
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
            // keep delegate alive until resume
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
                    val data = image.jpegData(0.9) // NSData?
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

// util kecil
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

// Render ke Compose ImageBitmap
actual fun decodeImage(bytes: ByteArray): ImageBitmap =
    org.jetbrains.skia.Image.makeFromEncoded(bytes).asImageBitmap()
