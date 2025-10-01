package org.com.bayarair.platform

import android.annotation.SuppressLint
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun SystemBackHandler(onBack: () -> Unit) {
    BackHandler(onBack = onBack)
}

@SuppressLint("ContextCastToActivity")
@Composable
actual fun rememberPlatformHandle(): Any? {
    return LocalContext.current as? Activity
}

actual fun minimizeApp(handle: Any?) {
    (handle as? Activity)?.moveTaskToBack(true)
}
