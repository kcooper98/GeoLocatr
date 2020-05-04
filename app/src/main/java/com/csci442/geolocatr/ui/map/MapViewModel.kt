package com.csci442.geolocatr.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.csci442.geolocatr.data.Checkpoint
import com.csci442.geolocatr.data.CheckpointRepository
import java.util.*

class MapViewModel(private val checkpointRepository: CheckpointRepository) : ViewModel() {
    val checkpointLiveData = checkpointRepository.getCheckpoints()

    fun getCheckpoint(id: UUID): LiveData<Checkpoint?> {
        return checkpointRepository.getCheckpoint(id)
    }

    fun addCheckpoint(checkpoint: Checkpoint) {
        checkpointRepository.addCheckpoint(checkpoint)
    }

    fun deleteCheckpoint(checkpoint: Checkpoint) {
        checkpointRepository.deleteCheckpoint(checkpoint)
    }
}