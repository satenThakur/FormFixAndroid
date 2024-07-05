package com.fittracker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaData(mediaData: MediaData)

    @Query("DELETE FROM mediadata WHERE id = :id")
    fun deletebyId(id: Long)

    @Query("SELECT * FROM mediadata")
     fun getAllMediaData(): List<MediaData>
}