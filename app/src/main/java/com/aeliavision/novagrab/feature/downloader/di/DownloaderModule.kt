package com.aeliavision.novagrab.feature.downloader.di

import com.aeliavision.novagrab.feature.downloader.repository.DownloadRepository
import com.aeliavision.novagrab.feature.downloader.repository.DownloadRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DownloaderModule {

    @Binds
    abstract fun bindDownloadRepository(impl: DownloadRepositoryImpl): DownloadRepository
}
