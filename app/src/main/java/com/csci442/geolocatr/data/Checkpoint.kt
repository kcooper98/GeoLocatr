package com.csci442.geolocatr.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Checkpoint(
    @PrimaryKey val id: UUID = UUID.randomUUID(),
    var lat: Double,
    var lon: Double,
    var address: String,
    var datetime: String,
    var temp: Double?,
    var description: String?
)