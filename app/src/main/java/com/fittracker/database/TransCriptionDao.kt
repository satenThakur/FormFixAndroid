package com.fittracker.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TransCriptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscriptionData(transCriptionData: TransCriptionData)

    @Query("DELETE FROM transcription WHERE id = :id")
    fun deletebyId(id: Long)

    @Query("SELECT * FROM transcription")
     fun getAllTranscriptionData(): List<TransCriptionData>

}