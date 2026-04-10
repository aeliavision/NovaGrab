package com.aeliavision.novagrab.feature.browser.domain.repository

import com.aeliavision.novagrab.feature.browser.domain.model.BrowserBookmark
import com.aeliavision.novagrab.feature.browser.domain.model.BrowserHistoryItem
import kotlinx.coroutines.flow.Flow

interface BrowserRepository {
    fun getBookmarks(): Flow<List<BrowserBookmark>>
    suspend fun addBookmark(url: String, title: String?)
    suspend fun removeBookmark(id: String)

    fun getHistory(): Flow<List<BrowserHistoryItem>>
    suspend fun recordVisit(url: String, title: String?)
    suspend fun clearHistory()
}
