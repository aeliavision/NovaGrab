package com.aeliavision.novagrab.core.database.converters

import androidx.room.TypeConverter
import com.aeliavision.novagrab.feature.detection.domain.model.VideoFormat

class VideoFormatConverter {
    @TypeConverter
    fun fromVideoFormat(format: VideoFormat?): String? = format?.extension

    @TypeConverter
    fun toVideoFormat(value: String?): VideoFormat =
        if (value.isNullOrBlank()) VideoFormat.Unknown else VideoFormat.fromExtension(value)
}
