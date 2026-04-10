package com.aeliavision.novagrab.core.database.converters

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class MapConverter {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringMap(value: Map<String, String>?): String =
        json.encodeToString(value ?: emptyMap())

    @TypeConverter
    fun toStringMap(value: String?): Map<String, String> =
        runCatching { json.decodeFromString<Map<String, String>>(value ?: "{}") }
            .getOrDefault(emptyMap())
}
