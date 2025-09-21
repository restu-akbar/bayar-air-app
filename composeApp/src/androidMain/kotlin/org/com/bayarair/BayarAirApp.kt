package org.com.bayarair

import android.app.Application
import org.com.bayarair.di.androidModule
import org.com.bayarair.di.commonModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BayarAirApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            printLogger()
            androidContext(this@BayarAirApp)
            modules(
                commonModule,
                androidModule,
            )
        }
    }
}
