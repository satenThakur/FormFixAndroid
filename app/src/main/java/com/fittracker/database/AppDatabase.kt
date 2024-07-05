package com.fittracker.database


import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MediaData::class], version = 1)
abstract class AppDatabase : RoomDatabase(){
    abstract fun userDao(): MediaDao
}