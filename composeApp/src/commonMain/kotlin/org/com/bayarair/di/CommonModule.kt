package org.com.bayarair.di

import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.platform.createHttpClient
import org.koin.dsl.module

val commonModule =
    module {
        single {
            org.com.bayarair.platform
                .createHttpClient()
        }
        single { AuthRepository(get()) }
    }
