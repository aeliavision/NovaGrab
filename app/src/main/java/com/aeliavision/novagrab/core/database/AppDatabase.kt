package com.aeliavision.novagrab.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.aeliavision.novagrab.core.database.converters.DateConverter
import com.aeliavision.novagrab.core.database.converters.DownloadStatusConverter
import com.aeliavision.novagrab.core.database.converters.ListConverter
import com.aeliavision.novagrab.core.database.converters.MapConverter
import com.aeliavision.novagrab.core.database.converters.VideoFormatConverter
import com.aeliavision.novagrab.feature.browser.data.dao.BookmarkDao
import com.aeliavision.novagrab.feature.browser.data.dao.BrowserHistoryDao
import com.aeliavision.novagrab.feature.browser.data.entity.BookmarkEntity
import com.aeliavision.novagrab.feature.browser.data.entity.BrowserHistoryEntity
import com.aeliavision.novagrab.feature.detection.data.dao.DetectedVideoDao
import com.aeliavision.novagrab.feature.detection.data.entity.DetectedVideoEntity
import com.aeliavision.novagrab.feature.downloader.data.dao.DownloadTaskDao
import com.aeliavision.novagrab.feature.downloader.data.entity.DownloadTaskEntity

@Database(
    entities = [
        BrowserHistoryEntity::class,
        BookmarkEntity::class,
        DetectedVideoEntity::class,
        DownloadTaskEntity::class,
    ],
    version = 2,
    exportSchema = true,
    autoMigrations = []
)
@TypeConverters(
    DateConverter::class,
    ListConverter::class,
    VideoFormatConverter::class,
    DownloadStatusConverter::class,
    MapConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun browserHistoryDao(): BrowserHistoryDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun detectedVideoDao(): DetectedVideoDao
    abstract fun downloadTaskDao(): DownloadTaskDao

    companion object {
        const val DATABASE_NAME = "smart_video_loader.db"
    }
}
