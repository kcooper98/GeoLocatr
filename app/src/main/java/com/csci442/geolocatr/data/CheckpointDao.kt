package com.csci442.geolocatr.data

import androidx.lifecycle.LiveData
import androidx.room.*
import java.util.*

@Dao
interface CheckpointDao {
    @Query("SELECT * FROM checkpoint")
    fun getCheckpoints(): LiveData<List<Checkpoint>>

    @Query("SELECT * FROM checkpoint WHERE id=(:id)")
    fun getCheckpoint(id: UUID): LiveData<Checkpoint?>

    @Update
    fun updateCheckpoint(checkpoint: Checkpoint)

    @Insert
    fun addCheckpoint(checkpoint: Checkpoint)

    @Delete
    fun deleteCheckpoint(checkpoint: Checkpoint)
}