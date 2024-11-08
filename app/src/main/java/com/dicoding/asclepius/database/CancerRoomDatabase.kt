package com.dicoding.asclepius.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Cancer::class], version = 1)
abstract class CancerRoomDatabase : RoomDatabase() {
    abstract fun noteDao(): CancerDao
    companion object {
        @Volatile
        private var INSTANCE: CancerRoomDatabase? = null
        @JvmStatic
        fun getDatabase(context: Context): CancerRoomDatabase {
            if (INSTANCE == null) {
                synchronized(CancerRoomDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                        CancerRoomDatabase::class.java, "cancer_database")
                        .build()
                }
            }
            return INSTANCE as CancerRoomDatabase
        }
    }
}