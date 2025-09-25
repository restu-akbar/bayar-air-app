package org.com.bayarair

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import org.com.bayarair.core.AppEvents
import org.com.bayarair.print.UnifiedReceiptPrinter
import org.com.bayarair.utils.LocalReceiptPrinter
import org.com.bayarair.utils.ReceiptPrinter
import org.koin.java.KoinJavaComponent.get

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val appEvents: AppEvents = get(AppEvents::class.java)

        setContent {
            val printer: ReceiptPrinter = UnifiedReceiptPrinter(this)
            CompositionLocalProvider(LocalReceiptPrinter provides printer) {
                App(appEvents)
            }
        }
    }
}
