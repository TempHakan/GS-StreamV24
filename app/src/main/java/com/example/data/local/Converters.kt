package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.StreamType

class Converters {
    @TypeConverter
    fun fromStreamType(value: StreamType): String = value.name

    @TypeConverter
    fun toStreamType(value: String): StreamType = try {
        StreamType.valueOf(value)
    } catch (e: Exception) {
        StreamType.LIVE
    }
}
