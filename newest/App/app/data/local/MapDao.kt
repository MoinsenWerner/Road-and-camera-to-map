package com.example.preciseroadmap.data.local

import androidx.room.*

@Dao
interface MapDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSegment(segment: SegmentEntity)

    @Query("SELECT * FROM segments")
    suspend fun getAllSegments(): List<SegmentEntity>
}