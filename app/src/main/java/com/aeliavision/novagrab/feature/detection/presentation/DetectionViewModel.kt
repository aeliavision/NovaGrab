package com.aeliavision.novagrab.feature.detection.presentation

import androidx.lifecycle.viewModelScope
import com.aeliavision.novagrab.core.common.MviViewModel
import com.aeliavision.novagrab.feature.browser.domain.tabs.BrowserTabStore
import com.aeliavision.novagrab.feature.detection.engine.DetectionEngine
import com.aeliavision.novagrab.feature.downloader.domain.usecase.EnqueueDownloadUseCase
import com.aeliavision.novagrab.feature.downloader.domain.usecase.GetHlsVariantsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import com.aeliavision.novagrab.feature.downloader.domain.model.FileNameGenerator
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

@HiltViewModel
class DetectionViewModel @Inject constructor(
    private val detectionEngine: DetectionEngine,
    private val enqueueDownloadUseCase: EnqueueDownloadUseCase,
    private val getHlsVariantsUseCase: GetHlsVariantsUseCase,
    private val browserTabStore: BrowserTabStore,
) : MviViewModel<DetectionState, DetectionIntent, DetectionEffect>(
    initialState = DetectionState(),
) {

    init {
        browserTabStore.state
            .map { it.activeTabId }
            .distinctUntilChanged()
            .flatMapLatest { tabId ->
                updateState { copy(tabId = tabId) }
                detectionEngine.detectionsForTab(tabId)
            }
            .onEach { list ->
                updateState { copy(videos = list) }
            }
            .launchIn(viewModelScope)

        detectionEngine.longPressDownloadEvents
            .onEach { event ->
                if (event.tabId == state.value.tabId) {
                    updateState { copy(showSheet = true) }
                }
            }
            .launchIn(viewModelScope)
    }

    override fun handleIntent(intent: DetectionIntent) {
        when (intent) {
            DetectionIntent.ToggleSheet -> updateState { copy(showSheet = !showSheet) }

            is DetectionIntent.ToggleSelection -> {
                updateState {
                    val newSet = if (intent.videoId in selectedIds) {
                        selectedIds - intent.videoId
                    } else {
                        selectedIds + intent.videoId
                    }
                    copy(selectedIds = newSet, isMultiSelectMode = newSet.isNotEmpty())
                }
            }

            DetectionIntent.DownloadSelected -> {
                viewModelScope.launch {
                    val selected = state.value.videos.filter { it.id in state.value.selectedIds }
                    selected.forEach { video ->
                        if (!video.videoUrl.startsWith("blob:")) {
                            val name = FileNameGenerator.generate(
                                title = video.title,
                                extension = video.format.extension,
                                videoId = video.id,
                            )
                            enqueueDownloadUseCase(
                                url = video.videoUrl,
                                fileName = name,
                                mimeType = video.mimeType,
                                format = video.format,
                                headers = video.requestHeaders,
                                sourcePageUrl = video.pageUrl,
                                totalSizeBytes = video.estimatedSizeBytes,
                            )
                        }
                    }
                    updateState { copy(selectedIds = emptySet(), isMultiSelectMode = false) }
                }
            }

            DetectionIntent.ClearSelection -> {
                updateState { copy(selectedIds = emptySet(), isMultiSelectMode = false) }
            }

            DetectionIntent.DismissQualityPicker -> updateState {
                copy(
                    showQualityPicker = false,
                    qualityMasterUrl = null,
                    qualityVariants = emptyList(),
                )
            }

            is DetectionIntent.ShowRenameDialog -> updateState {
                copy(showRenameDialog = true, renameVideoUrl = intent.videoUrl)
            }

            DetectionIntent.DismissRenameDialog -> updateState {
                copy(showRenameDialog = false, renameVideoUrl = null)
            }

            is DetectionIntent.DownloadVideoWithName -> {
                viewModelScope.launch {
                    val video = state.value.videos.firstOrNull { it.videoUrl == intent.videoUrl } ?: return@launch
                    updateState { copy(showRenameDialog = false, renameVideoUrl = null) }
                    enqueueDownloadUseCase(
                        url = video.videoUrl,
                        fileName = intent.fileName,
                        mimeType = video.mimeType,
                        format = video.format,
                        headers = video.requestHeaders,
                        sourcePageUrl = video.pageUrl,
                        totalSizeBytes = video.estimatedSizeBytes,
                    )
                }
            }

            is DetectionIntent.DownloadVideo -> {
                if (intent.videoUrl.startsWith("blob:")) return
                viewModelScope.launch {
                    val video = state.value.videos.firstOrNull { it.videoUrl == intent.videoUrl } ?: return@launch

                    if (video.format == com.aeliavision.novagrab.feature.detection.domain.model.VideoFormat.HLS) {
                        val variants = getHlsVariantsUseCase(video.videoUrl).getOrElse { emptyList() }
                        if (variants.size <= 1) {
                            val name = FileNameGenerator.generate(
                                title = video.title,
                                extension = video.format.extension,
                                videoId = video.id,
                            )
                            updateState {
                                copy(
                                    showRenameDialog = true,
                                    renameVideoUrl = video.videoUrl,
                                    renameInitialName = name,
                                )
                            }
                        } else {
                            updateState {
                                copy(
                                    showQualityPicker = true,
                                    qualityMasterUrl = video.videoUrl,
                                    qualityVariants = variants,
                                )
                            }
                        }
                        return@launch
                    }

                    val name = FileNameGenerator.generate(
                        title = video.title,
                        extension = video.format.extension,
                        videoId = video.id,
                    )
                    updateState {
                        copy(
                            showRenameDialog = true,
                            renameVideoUrl = video.videoUrl,
                            renameInitialName = name,
                        )
                    }
                }
            }

            is DetectionIntent.SelectHlsVariant -> {
                viewModelScope.launch {
                    val video = state.value.videos.firstOrNull { it.videoUrl == intent.masterUrl } ?: return@launch
                    val name = FileNameGenerator.generate(
                        title = video.title,
                        extension = video.format.extension,
                        videoId = video.id,
                    )
                    updateState {
                        copy(
                            showQualityPicker = false,
                            qualityMasterUrl = null,
                            qualityVariants = emptyList(),
                            showRenameDialog = true,
                            renameVideoUrl = intent.variantUrl,
                            renameInitialName = name,
                        )
                    }
                }
            }
        }
    }
}
