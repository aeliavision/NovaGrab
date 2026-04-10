package com.aeliavision.novagrab.feature.browser.data.repository

import com.aeliavision.novagrab.feature.browser.data.dao.BookmarkDao
import com.aeliavision.novagrab.feature.browser.data.dao.BrowserHistoryDao
import com.aeliavision.novagrab.feature.browser.data.entity.BookmarkEntity
import com.aeliavision.novagrab.feature.browser.data.entity.BrowserHistoryEntity
import com.aeliavision.novagrab.feature.browser.domain.model.BrowserBookmark
import com.aeliavision.novagrab.feature.browser.domain.model.BrowserHistoryItem
import com.aeliavision.novagrab.feature.browser.domain.repository.BrowserRepository
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Singleton
class BrowserRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao,
    private val historyDao: BrowserHistoryDao,
) : BrowserRepository {

    override fun getBookmarks(): Flow<List<BrowserBookmark>> {
        return bookmarkDao.getBookmarksAsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun addBookmark(url: String, title: String?) {
        bookmarkDao.insert(
            BookmarkEntity(
                id = UUID.randomUUID().toString(),
                url = url,
                title = title,
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun removeBookmark(id: String) {
        bookmarkDao.delete(id)
    }

    override fun getHistory(): Flow<List<BrowserHistoryItem>> {
        return historyDao.getHistoryAsFlow().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun recordVisit(url: String, title: String?) {
        historyDao.insert(
            BrowserHistoryEntity(
                id = UUID.randomUUID().toString(),
                url = url,
                title = title,
                visitedAt = System.currentTimeMillis(),
            )
        )
    }

    override suspend fun clearHistory() {
        historyDao.clear()
    }

    private fun BookmarkEntity.toDomain(): BrowserBookmark {
        return BrowserBookmark(
            id = id,
            url = url,
            title = title,
            createdAt = createdAt,
        )
    }

    private fun BrowserHistoryEntity.toDomain(): BrowserHistoryItem {
        return BrowserHistoryItem(
            id = id,
            url = url,
            title = title,
            visitedAt = visitedAt,
        )
    }
}
