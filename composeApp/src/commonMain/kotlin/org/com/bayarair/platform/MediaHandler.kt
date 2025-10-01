package org.com.bayarair.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.ImageBitmap

data class PickResult(
    val bytes: ByteArray,
    val mimeType: String? = "image/jpeg",
)

interface ImageGateway {
    suspend fun ensureCameraPermission(): Boolean

    suspend fun captureImage(): PickResult?

    suspend fun pickImage(): PickResult?
}

@Composable
expect fun rememberImageGateway(): ImageGateway

expect fun decodeImage(bytes: ByteArray): ImageBitmap
