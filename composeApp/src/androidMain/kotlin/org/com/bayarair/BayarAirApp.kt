package org.com.bayarair

import android.app.Application
import org.com.bayarair.data.local.AndroidTokenStorage
import org.com.bayarair.data.token.DefaultTokenHandler
import org.com.bayarair.data.token.TokenHandler
import org.com.bayarair.data.token.TokenStorage
import org.com.bayarair.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

class BayarAirApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin(
            appDeclaration = {
                printLogger()
                androidContext(this@BayarAirApp)
            },
            platformModules = arrayOf(androidModule),
        )
    }
}

val androidModule = module {
    single<TokenStorage> { AndroidTokenStorage(androidContext()) }
    single<TokenHandler> { DefaultTokenHandler(get()) }
}
