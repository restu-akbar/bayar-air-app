package org.com.bayarair.platform

// --- iosMain/desktopMain ---

// iosMain/your/package/SystemBackHandler.ios.kt
import androidx.compose.runtime.Composable

@Composable
actual fun SystemBackHandler(onBack: () -> Unit) { /* no-op */
}

@Composable
actual fun rememberPlatformHandle(): Any? = null

actual fun minimizeApp(handle: Any?) { /* no-op */
}

