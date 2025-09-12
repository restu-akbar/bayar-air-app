package org.com.bayarair.di

import org.com.bayarair.data.token.AndroidTokenStore
import org.com.bayarair.data.token.TokenHandler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
// import RoomModule jika pakai Room

val androidModule =
    module {
        single<TokenHandler> { AndroidTokenStore(androidContext()) }
    }
