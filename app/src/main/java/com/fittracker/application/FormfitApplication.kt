package com.fittracker.application

import android.app.Application
import androidx.room.Room
import com.fittracker.database.AppDatabase

class FormfitApplication : Application() {
    companion object {
        lateinit var database: AppDatabase
    }
    override fun onCreate() {
        super.onCreate()
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "formfit_database"
        ).build()
    }
}