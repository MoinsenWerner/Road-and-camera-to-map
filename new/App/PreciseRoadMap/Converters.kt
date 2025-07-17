package com.example.preciseroadmap.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object Converters {
    @TypeConverter
    fun fromList(list: List<List<Double>>): String = Json.encodeToString(list)

    @TypeConverter
    fun toList(value: String): List<List<Double>> = Json.decodeFromString(value)
}