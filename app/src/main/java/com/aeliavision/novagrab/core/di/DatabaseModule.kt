package com.aeliavision.novagrab.core.di

import android.content.Context
import androidx.room.Room
import com.aeliavision.novagrab.core.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext  context: Context,
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideBrowserHistoryDao(db: AppDatabase) = db.browserHistoryDao()

    @Provides
    fun provideBookmarkDao(db: AppDatabase) = db.bookmarkDao()

    @Provides
    fun provideDetectedVideoDao(db: AppDatabase) = db.detectedVideoDao()

    @Provides
    fun provideDownloadTaskDao(db: AppDatabase) = db.downloadTaskDao()
}
