package com.aeliavision.novagrab.feature.downloader.domain.usecase

import com.aeliavision.novagrab.feature.downloader.engine.HlsManifestParser
import com.aeliavision.novagrab.feature.downloader.engine.HlsVariant
import javax.inject.Inject

class GetHlsVariantsUseCase @Inject constructor(
    private val hlsManifestParser: HlsManifestParser,
) {
    suspend operator fun invoke(url: String): Result<List<HlsVariant>> {
        return hlsManifestParser.parseMaster(url).map { it.variants }
    }
}
