package org.com.bayarair.platform

import androidx.compose.runtime.Composable

@Composable
expect fun SystemBackHandler(onBack: () -> Unit)

@Composable
expect fun rememberPlatformHandle(): Any?

expect fun minimizeApp(handle: Any?)
