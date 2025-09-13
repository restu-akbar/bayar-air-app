package org.com.bayarair.di

import io.ktor.client.HttpClient
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.data.token.TokenHandler
import org.com.bayarair.platform.createHttpClient
import org.com.bayarair.presentation.viewmodel.HomeViewModel
import org.com.bayarair.presentation.viewmodel.SplashViewModel
import org.com.bayarair.presentation.viewmodel.AuthViewModel
import org.koin.dsl.module

val commonModule =
    module {
        single<HttpClient> { createHttpClient() }

        single<AuthRepository> { AuthRepository(get<HttpClient>()) }

        factory<AuthViewModel> { AuthViewModel(get<TokenHandler>(), get<AuthRepository>()) }
        factory<SplashViewModel> { SplashViewModel(get<TokenHandler>()) }
        factory<HomeViewModel> { HomeViewModel(get<TokenHandler>(), get<AuthRepository>()) }
    }
