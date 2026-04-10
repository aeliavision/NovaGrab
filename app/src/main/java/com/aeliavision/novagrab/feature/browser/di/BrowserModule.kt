package com.aeliavision.novagrab.feature.browser.di

import com.aeliavision.novagrab.feature.browser.data.repository.BrowserRepositoryImpl
import com.aeliavision.novagrab.feature.browser.domain.repository.BrowserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class BrowserModule {

    @Binds
    abstract fun bindBrowserRepository(impl: BrowserRepositoryImpl): BrowserRepository
}
