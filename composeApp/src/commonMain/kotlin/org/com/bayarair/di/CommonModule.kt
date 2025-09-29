package org.com.bayarair.di

import io.ktor.client.HttpClient
import org.com.bayarair.core.AppEvents
import org.com.bayarair.data.repository.AuthRepository
import org.com.bayarair.data.repository.CustomerRepository
import org.com.bayarair.data.repository.ProfileRepository
import org.com.bayarair.data.repository.RecordRepository
import org.com.bayarair.data.token.TokenHandler
import org.com.bayarair.platform.createHttpClient
import org.com.bayarair.presentation.viewmodel.AuthViewModel
import org.com.bayarair.presentation.viewmodel.HomeViewModel
import org.com.bayarair.presentation.viewmodel.ProfileViewModel
import org.com.bayarair.presentation.viewmodel.RecordDetailViewModel
import org.com.bayarair.presentation.viewmodel.RecordHistoryShared
import org.com.bayarair.presentation.viewmodel.RecordViewModel
import org.com.bayarair.presentation.viewmodel.SplashViewModel
import org.com.bayarair.presentation.viewmodel.StatsShared
import org.com.bayarair.presentation.viewmodel.UserShared
import org.koin.dsl.module

val commonModule =
    module {
        single { AppEvents() }
        single<HttpClient> { createHttpClient() }

        single<AuthRepository> { AuthRepository(get<HttpClient>()) }
        single<RecordRepository> { RecordRepository(get<HttpClient>()) }
        single<ProfileRepository> { ProfileRepository(get<HttpClient>()) }
        single<CustomerRepository> { CustomerRepository(get<HttpClient>()) }

        factory<AuthViewModel> {
            AuthViewModel(
                get<TokenHandler>(),
                get<AuthRepository>(),
                get<AppEvents>(),
                get<RecordHistoryShared>(),
                get<UserShared>(),
            )
        }
        factory<SplashViewModel> { SplashViewModel(get<TokenHandler>()) }
        factory<HomeViewModel> {
            HomeViewModel(
                get<RecordRepository>(),
                get<CustomerRepository>(),
                get<AppEvents>(),
                get<RecordHistoryShared>(),
                get<StatsShared>(),
            )
        }
        factory<RecordViewModel> {
            RecordViewModel(
                get<RecordRepository>(),
                get<CustomerRepository>(),
                get<RecordHistoryShared>(),
                get<StatsShared>(),
            )
        }
        factory<RecordDetailViewModel> { RecordDetailViewModel(get<RecordRepository>()) }
        factory<ProfileViewModel> {
            ProfileViewModel(
                get<ProfileRepository>(),
                get<AppEvents>(),
                get<UserShared>(),
            )
        }

        single { RecordHistoryShared() }
        single { UserShared() }
        single { StatsShared() }
    }
