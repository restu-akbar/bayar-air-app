package org.com.bayarair.platform

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.coroutines.resume
import kotlin.math.max
import kotlin.math.min

private class AndroidImageGateway(
    private val activity: ComponentActivity,
    private val context: Context,
    private val launcherTakePicture: (Uri, (Boolean) -> Unit) -> Unit,
    private val launcherGetContent: ((Uri?) -> Unit) -> Unit,
    private val launcherRequestPermission: ((Boolean) -> Unit) -> Unit,
) : ImageGateway {
    private fun newTempUri(): Uri {
        val imageFile =
            File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                "meter_${System.currentTimeMillis()}.jpg",
            )
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile,
        )
    }

    override suspend fun ensureCameraPermission(): Boolean =
        suspendCancellableCoroutine { cont ->
            launcherRequestPermission { granted -> cont.resume(granted) }
        }

    override suspend fun captureImage(): PickResult? {
        val uri = newTempUri()
        val ok =
            suspendCancellableCoroutine<Boolean> { cont ->
                launcherTakePicture(uri) { success -> cont.resume(success) }
            }
        if (!ok) return null
        val bytes =
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        return PickResult(bytes, "image/jpeg")
    }

    override suspend fun pickImage(): PickResult? {
        val uri =
            suspendCancellableCoroutine<Uri?> { cont ->
                launcherGetContent { result -> cont.resume(result) }
            } ?: return null
        val bytes =
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        return PickResult(bytes, "image/*")
    }
}

@Composable
actual fun rememberImageGateway(): ImageGateway {
    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = remember(context) { context as ComponentActivity }

    var takePictureCallback by remember { mutableStateOf<(Boolean) -> Unit>({}) }
    val takePictureLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture(),
        ) { success -> takePictureCallback(success) }

    var getContentCallback by remember { mutableStateOf<(Uri?) -> Unit>({}) }
    val getContentLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent(),
        ) { uri -> getContentCallback(uri) }

    var permissionCallback by remember { mutableStateOf<(Boolean) -> Unit>({}) }
    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { granted -> permissionCallback(granted) }

    return remember {
        AndroidImageGateway(
            activity = activity,
            context = context,
            launcherTakePicture = { uri, cb ->
                takePictureCallback = cb
                takePictureLauncher.launch(uri)
            },
            launcherGetContent = { cb ->
                getContentCallback = cb
                getContentLauncher.launch("image/*")
            },
            launcherRequestPermission = { cb ->
                permissionCallback = cb
                permissionLauncher.launch(Manifest.permission.CAMERA)
            },
        )
    }
}

actual fun decodeImage(bytes: ByteArray): ImageBitmap {
    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return bmp.asImageBitmap()
}

actual fun compressImage(
    bytes: ByteArray,
    maxWidth: Int?,
    maxHeight: Int?,
    quality: Int,
): ByteArray {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(bytes, 0, bytes.size, bounds)

    val srcW = bounds.outWidth
    val srcH = bounds.outHeight

    val sampleOpts = BitmapFactory.Options()
    sampleOpts.inSampleSize =
        run {
            if (maxWidth == null || maxHeight == null || srcW <= 0 || srcH <= 0) {
                1
            } else {
                val ratioW = srcW.toFloat() / maxWidth
                val ratioH = srcH.toFloat() / maxHeight
                val ratio = max(1f, max(ratioW, ratioH))
                ratio.toInt().coerceAtLeast(1)
            }
        }

    val decoded =
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, sampleOpts)
            ?: return bytes

    val (targetW, targetH) =
        if (maxWidth != null && maxHeight != null) {
            val scale =
                min(
                    maxWidth.toFloat() / decoded.width,
                    maxHeight.toFloat() / decoded.height,
                ).coerceAtMost(1f)
            Pair(
                (decoded.width * scale).toInt().coerceAtLeast(1),
                (decoded.height * scale).toInt().coerceAtLeast(1),
            )
        } else {
            Pair(decoded.width, decoded.height)
        }

    val resized =
        if (decoded.width != targetW || decoded.height != targetH) {
            Bitmap.createScaledBitmap(decoded, targetW, targetH, true)
        } else {
            decoded
        }

    val baos = ByteArrayOutputStream()
    resized.compress(Bitmap.CompressFormat.JPEG, quality.coerceIn(0, 100), baos)

    if (resized !== decoded) decoded.recycle()
    resized.recycle()

    return baos.toByteArray()
}
