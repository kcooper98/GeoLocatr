package com.csci442.geolocatr.data

import android.content.Context
import androidx.lifecycle.LiveData
import java.util.*
import java.util.concurrent.Executors

class CheckpointRepository(private val checkpointDao: CheckpointDao) {
    private val executor = Executors.newSingleThreadExecutor()

    fun getCheckpoints(): LiveData<List<Checkpoint>> = checkpointDao.getCheckpoints()

    fun getCheckpoint(id: UUID): LiveData<Checkpoint?> = checkpointDao.getCheckpoint(id)

    fun updateCheckpoint(checkpoint: Checkpoint) {
        executor.execute {
            checkpointDao.updateCheckpoint(checkpoint)
        }
    }

    fun addCheckpoint(checkpoint: Checkpoint) {
        executor.execute {
            checkpointDao.addCheckpoint(checkpoint)
        }
    }

    fun deleteCheckpoint(checkpoint: Checkpoint) {
        executor.execute {
            checkpointDao.deleteCheckpoint(checkpoint)
        }
    }

    companion object {
        private var instance: CheckpointRepository? = null
        fun getInstance(context: Context): CheckpointRepository? {
            return instance ?: let {
                if (instance == null) {
                    val database = CheckpointDatabase.getInstance(context)
                    instance = CheckpointRepository(database.checkpointDao())
                }
                return instance
            }
        }
    }
}