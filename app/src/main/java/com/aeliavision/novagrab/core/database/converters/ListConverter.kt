package com.aeliavision.novagrab.core.database.converters

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class ListConverter {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromStringList(value: List<String>?): String =
        json.encodeToString(value ?: emptyList())

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        runCatching { json.decodeFromString<List<String>>(value ?: "[]") }
            .getOrDefault(emptyList())
}
