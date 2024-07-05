package com.fittracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "mediadata")
data class MediaData(@PrimaryKey(autoGenerate = true) val id: Long = 0,
                     val exerciseType:String,
                     val uri: String,
                     val filename: String,
                     val date: String,
                     val time: String)
