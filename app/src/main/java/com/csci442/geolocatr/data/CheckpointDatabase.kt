package com.csci442.geolocatr.data

import android.content.Context
import androidx.room.*

@Database(entities = [Checkpoint::class], version = 1)
@TypeConverters(CheckpointTypeConverters::class)
abstract class CheckpointDatabase : RoomDatabase() {
    companion object {
        private var instance: CheckpointDatabase? = null
        private const val DATABASE_NAME = "checkpoint-database"
        fun getInstance(context: Context): CheckpointDatabase {
            return instance ?: let {
                instance ?: Room.databaseBuilder(
                    context,
                    CheckpointDatabase::class.java,
                    DATABASE_NAME
                ).build()
            }
        }
    }

    abstract fun checkpointDao(): CheckpointDao
}