package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "locations")
data class LocationEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String,
    val category: String, // e.g., "Home", "Office", "Other"
    val notes: String = "",
    val uri: String, // the google maps link or shared content
    val timestamp: Long = System.currentTimeMillis(),
    val isFavorite: Boolean = false,
    val address: String = "",
    val city: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)
