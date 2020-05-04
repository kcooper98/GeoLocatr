package com.csci442.geolocatr.ui.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.csci442.geolocatr.data.CheckpointRepository

class MapViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T: ViewModel?> create(modelClass: Class<T>): T {
        return modelClass.getConstructor(CheckpointRepository::class.java)
            .newInstance(CheckpointRepository.getInstance(context))
    }
}