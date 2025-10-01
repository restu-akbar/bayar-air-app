package org.com.bayarair

import org.com.bayarair.data.token.DefaultTokenHandler
import org.com.bayarair.data.token.IosTokenStorage
import org.com.bayarair.data.token.TokenHandler
import org.com.bayarair.data.token.TokenStorage
import org.com.bayarair.di.initKoin
import org.koin.dsl.module

val iosModule =
    module {
        single<TokenStorage> { IosTokenStorage() }
        single<TokenHandler> { DefaultTokenHandler(get()) }
    }

fun initKoinIos() = initKoin(platformModules = arrayOf(iosModule))

