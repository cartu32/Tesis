package com.example.abumonitor.utils

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): java.sql.Time? {
        return value?.let { java.sql.Time(it) }
    }

    @TypeConverter
    fun toTimestamp(time: java.sql.Time?): Long? {
        return time?.time
    }

    @TypeConverter
    fun fromDate(value: Long?): java.sql.Date? {
        return value?.let { java.sql.Date(it) }
    }

    @TypeConverter
    fun toDate(date: java.sql.Date?): Long? {
        return date?.time
    }
}

