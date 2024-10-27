package com.fittracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "transcription")
data class TransCriptionData(@PrimaryKey(autoGenerate = true) val id: Long = 0,
                             val filename:String,
                             val state: String,
                             val kneeAngle:String,
                             val hipAngle: String)
