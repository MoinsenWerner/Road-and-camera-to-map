package com.example.preciseroadmap.data.local

import android.content.Context
import androidx.room.*

@Database(entities = [SegmentEntity::class], version = 1)
@TypeConverters(Converters::class)
abstract class MapDatabase : RoomDatabase() {
    abstract fun dao(): MapDao

    companion object {
        @Volatile private var INSTANCE: MapDatabase? = null

        fun get(context: Context): MapDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    MapDatabase::class.java,
                    "map.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}