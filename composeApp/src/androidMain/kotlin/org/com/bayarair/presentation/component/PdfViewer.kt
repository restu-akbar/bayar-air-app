package org.com.bayarair.presentation.component

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView
import coil3.Bitmap

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
actual fun PdfViewer(url: String, modifier: Modifier) {
    var isRefreshing by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    Box(modifier = modifier) {
        AndroidView(
            factory = { context ->
                val swipe = androidx.swiperefreshlayout.widget.SwipeRefreshLayout(context)
                val web = WebView(context)

                swipe.addView(
                    web,
                    ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                )

                web.setOnScrollChangeListener { _, _, _, _, _ ->
                    swipe.isEnabled = !web.canScrollVertically(-1)
                }

                swipe.setOnRefreshListener {
                    isRefreshing = true
                    isLoading =
                        true
                    web.reload()
                }

                web.settings.javaScriptEnabled = true
                web.settings.mixedContentMode =
                    android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                web.webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                        isLoading = true
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        isLoading = false
                        if (isRefreshing) {
                            isRefreshing = false
                            swipe.isRefreshing = false
                        }
                    }

                    @Suppress("DEPRECATION")
                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        isLoading = false
                        if (isRefreshing) {
                            isRefreshing = false
                            swipe.isRefreshing = false
                        }
                    }
                }

                web.loadUrl("https://docs.google.com/viewer?embedded=true&url=$url")
                swipe
            },
            update = { swipe ->
                swipe.isRefreshing = isRefreshing
            },
            modifier = Modifier.matchParentSize()
        )

        if (isLoading && !isRefreshing) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}
