package com.example.ui

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.LocationEntry
import com.example.data.LocationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLDecoder
import java.util.Locale

data class ParsedLocation(
    val title: String,
    val address: String,
    val city: String,
    val latitude: Double?,
    val longitude: Double?
)

class PinBookViewModel(private val repository: LocationRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val locations: StateFlow<List<LocationEntry>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allLocations
            } else {
                repository.searchLocations(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun addLocation(
        label: String,
        category: String,
        uri: String,
        notes: String = "",
        address: String = "",
        city: String = "",
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        viewModelScope.launch {
            repository.insert(
                LocationEntry(
                    label = label,
                    category = category,
                    uri = uri,
                    notes = notes,
                    address = address,
                    city = city,
                    latitude = latitude,
                    longitude = longitude
                )
            )
        }
    }

    suspend fun parseUri(rawText: String, context: Context): ParsedLocation = withContext(Dispatchers.IO) {
        android.util.Log.d("PinBook", "Raw shared text: $rawText")
        var lat: Double? = null
        var lng: Double? = null
        var title: String = ""
        var addressLine: String = ""

        // Extract title/address lines from the raw text for fallback
        val lines = rawText.split('\n').map { it.trim() }.filter { it.isNotBlank() }
        val nonUrlLines = lines.filter { !it.contains("http://") && !it.contains("https://") }
        if (nonUrlLines.isNotEmpty()) {
            title = nonUrlLines.first()
            if (nonUrlLines.size > 1) {
                addressLine = nonUrlLines.drop(1).joinToString(", ")
            }
        }

        // Try to parse geo: URI
        val geoMatch = Regex("geo:(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)").find(rawText)
        val geoQueryMatch = Regex("[?&]q=(-?\\d+\\.\\d+),(-?\\d+\\.\\d+)").find(rawText)
        val geoLabelMatch = Regex("\\(([^)]+)\\)").find(rawText)

        if (rawText.startsWith("geo:")) {
            if (geoQueryMatch != null) {
                lat = geoQueryMatch.groupValues[1].toDoubleOrNull()
                lng = geoQueryMatch.groupValues[2].toDoubleOrNull()
            } else if (geoMatch != null) {
                lat = geoMatch.groupValues[1].toDoubleOrNull()
                lng = geoMatch.groupValues[2].toDoubleOrNull()
            }
            if (geoLabelMatch != null) {
                try {
                    title = URLDecoder.decode(geoLabelMatch.groupValues[1].replace("+", " "), "UTF-8")
                } catch (e: Exception) {}
            }
        } else {
            val urlMatch = Regex("https?://[^\\s]+").find(rawText)
            val urlToParse = urlMatch?.value

            if (urlToParse != null) {
                var expandedUrl = urlToParse
                try {
                    val connection = URL(urlToParse).openConnection() as HttpURLConnection
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    // Read the response up to 5 redirects
                    var redirectCount = 0
                    var currentConnection = connection
                    var currentUrl = urlToParse

                    while (redirectCount < 10) {
                        currentConnection.instanceFollowRedirects = false
                        currentConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36")
                        currentConnection.connect()
                        val responseCode = currentConnection.responseCode
                        if (responseCode in 300..399) {
                            val newUrlStr = currentConnection.getHeaderField("Location")
                            currentConnection.disconnect()
                            if (newUrlStr == null) break
                            
                            currentUrl = java.net.URI(currentUrl).resolve(newUrlStr).toString()
                            currentConnection = URL(currentUrl).openConnection() as HttpURLConnection
                            currentConnection.connectTimeout = 5000
                            currentConnection.readTimeout = 5000
                            redirectCount++
                        } else {
                            currentConnection.disconnect()
                            break
                        }
                    }
                    expandedUrl = currentUrl
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                android.util.Log.d("PinBook", "Expanded URL: $expandedUrl")

                val placeMatch = Regex("/place/([^/]+)/").find(expandedUrl)
                if (placeMatch != null && (title.isBlank() || title.startsWith("https://"))) {
                    try {
                        val parsedTitle = URLDecoder.decode(placeMatch.groupValues[1].replace("+", " "), "UTF-8")
                        if (parsedTitle.isNotBlank()) title = parsedTitle
                    } catch (e: Exception) {}
                }

                val queryMatch = Regex("[?&](?:query|q)=(-?\\d+\\.\\d+)(?:,|%2C)(-?\\d+\\.\\d+)").find(expandedUrl)
                val atMatch = Regex("@(-?\\d+\\.\\d+)(?:,|%2C)(-?\\d+\\.\\d+)").find(expandedUrl)
                val d3Match = Regex("!3d(-?\\d+\\.\\d+)").find(expandedUrl)
                val d4Match = Regex("!4d(-?\\d+\\.\\d+)").find(expandedUrl)

                if (queryMatch != null) {
                    lat = queryMatch.groupValues[1].toDoubleOrNull()
                    lng = queryMatch.groupValues[2].toDoubleOrNull()
                } else if (atMatch != null) {
                    lat = atMatch.groupValues[1].toDoubleOrNull()
                    lng = atMatch.groupValues[2].toDoubleOrNull()
                } else if (d3Match != null && d4Match != null) {
                    lat = d3Match.groupValues[1].toDoubleOrNull()
                    lng = d4Match.groupValues[1].toDoubleOrNull()
                }
            }
        }

        android.util.Log.d("PinBook", "Parsed lat=$lat lng=$lng")

        var city: String = ""

        if (lat != null && lng != null) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val extractedAddressLine = addr.getAddressLine(0)
                    if (!extractedAddressLine.isNullOrBlank()) {
                        addressLine = extractedAddressLine
                    }
                    city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: ""
                    if (title.isBlank() && addr.featureName != null) {
                        title = addr.featureName
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Return original raw text if no title found and no mapping matched
        ParsedLocation(title, addressLine, city, lat, lng)
    }

    fun updateLocation(location: LocationEntry) {
        viewModelScope.launch {
            repository.update(location)
        }
    }

    fun toggleFavorite(location: LocationEntry) {
        viewModelScope.launch {
            repository.update(location.copy(isFavorite = !location.isFavorite))
        }
    }

    fun deleteLocation(location: LocationEntry) {
        viewModelScope.launch {
            repository.delete(location)
        }
    }

    suspend fun getLocationById(id: Int): LocationEntry? {
        return repository.getLocationById(id)
    }
}

class PinBookViewModelFactory(private val repository: LocationRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PinBookViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PinBookViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
