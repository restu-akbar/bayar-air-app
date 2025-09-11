package org.com.bayarair.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.com.bayarair.data.repository.GreetingRepo
import org.com.bayarair.data.repository.GreetingRepoImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideRepo(): GreetingRepo = GreetingRepoImpl()
}
