package org.com.bayarair

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.com.bayarair.core.AppEvents
import org.koin.core.context.GlobalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val appEvents: AppEvents = GlobalContext.get().get()
        setContent {
            App(appEvents)
        }
    }
}
