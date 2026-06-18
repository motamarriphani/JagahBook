package com.example.data

import kotlinx.coroutines.flow.Flow

class LocationRepository(private val locationDao: LocationDao) {
    val allLocations: Flow<List<LocationEntry>> = locationDao.getAllLocations()

    fun searchLocations(query: String): Flow<List<LocationEntry>> {
        return locationDao.searchLocations(query)
    }

    suspend fun getLocationById(id: Int): LocationEntry? {
        return locationDao.getLocationById(id)
    }

    suspend fun insert(location: LocationEntry) {
        locationDao.insertLocation(location)
    }

    suspend fun update(location: LocationEntry) {
        locationDao.updateLocation(location)
    }

    suspend fun delete(location: LocationEntry) {
        locationDao.deleteLocation(location)
    }
}
