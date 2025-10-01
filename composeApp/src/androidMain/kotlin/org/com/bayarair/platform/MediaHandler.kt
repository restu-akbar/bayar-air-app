package org.com.bayarair.platform


import android.Manifest
import android.content.Context
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
import java.io.File
import kotlin.coroutines.resume

private class AndroidImageGateway(
    private val activity: ComponentActivity,
    private val context: Context,
    private val launcherTakePicture: (Uri, (Boolean) -> Unit) -> Unit,
    private val launcherGetContent: ((Uri?) -> Unit) -> Unit,
    private val launcherRequestPermission: ((Boolean) -> Unit) -> Unit
) : ImageGateway {

    private fun newTempUri(): Uri {
        val imageFile = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
            "meter_${System.currentTimeMillis()}.jpg"
        )
        return FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", imageFile
        )
    }

    override suspend fun ensureCameraPermission(): Boolean =
        suspendCancellableCoroutine { cont ->
            launcherRequestPermission { granted -> cont.resume(granted) }
        }

    override suspend fun captureImage(): PickResult? {
        val uri = newTempUri()
        val ok = suspendCancellableCoroutine<Boolean> { cont ->
            launcherTakePicture(uri) { success -> cont.resume(success) }
        }
        if (!ok) return null
        val bytes =
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        return PickResult(bytes, "image/jpeg")
    }

    override suspend fun pickImage(): PickResult? {
        val uri = suspendCancellableCoroutine<Uri?> { cont ->
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
    val takePictureLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> takePictureCallback(success) }

    var getContentCallback by remember { mutableStateOf<(Uri?) -> Unit>({}) }
    val getContentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> getContentCallback(uri) }

    var permissionCallback by remember { mutableStateOf<(Boolean) -> Unit>({}) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
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
            }
        )
    }
}

actual fun decodeImage(bytes: ByteArray): ImageBitmap {
    val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    return bmp.asImageBitmap()
}
