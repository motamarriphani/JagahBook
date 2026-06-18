package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.data.LocationEntry
import com.example.ui.PinBookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    viewModel: PinBookViewModel,
    incomingUri: String?,
    locationId: Int?,
    onNavigateBack: () -> Unit
) {
    var label by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Other") }
    var uri by remember { mutableStateOf(incomingUri ?: "") }
    var notes by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf<Double?>(null) }
    var longitude by remember { mutableStateOf<Double?>(null) }

    var isParsing by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var existingLocation by remember { mutableStateOf<LocationEntry?>(null) }

    LaunchedEffect(locationId, incomingUri) {
        if (locationId != null) {
            existingLocation = viewModel.getLocationById(locationId)
            existingLocation?.let { loc ->
                label = loc.label
                category = loc.category
                uri = loc.uri
                notes = loc.notes
                address = loc.address
                city = loc.city
                latitude = loc.latitude
                longitude = loc.longitude
            }
        } else if (!incomingUri.isNullOrBlank()) {
            isParsing = true
            val parsed = viewModel.parseUri(incomingUri, context)
            if (label.isBlank() && parsed.title.isNotBlank()) label = parsed.title
            address = parsed.address
            city = parsed.city
            latitude = parsed.latitude
            longitude = parsed.longitude
            isParsing = false
            if (parsed.latitude == null && parsed.address.isBlank() && parsed.title.isBlank()) {
                android.widget.Toast.makeText(context, "Could not extract location details. Please enter manually.", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (locationId == null) "Save Location" else "Edit Location", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Map Mock Component
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE8F0FE)),
                contentAlignment = Alignment.Center
            ) {
                if (isParsing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Extracting location details...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    }
                } else {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Label (e.g., Rohan's Home)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    CategoryChip("Home", Icons.Filled.Home, category == "Home") { category = "Home" }
                    CategoryChip("Office", Icons.Filled.Business, category == "Office") { category = "Office" }
                    CategoryChip("Other", Icons.Filled.Place, category == "Other") { category = "Other" }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Notes (optional)", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    placeholder = { Text("Park facing building...") }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (label.isNotBlank() && uri.isNotBlank()) {
                        if (existingLocation != null) {
                            existingLocation?.copy(
                                label = label,
                                category = category,
                                uri = uri,
                                notes = notes,
                                address = address,
                                city = city,
                                latitude = latitude,
                                longitude = longitude
                            )?.let { viewModel.updateLocation(it) }
                        } else {
                            viewModel.addLocation(
                                label = label,
                                category = category,
                                uri = uri,
                                notes = notes,
                                address = address,
                                city = city,
                                latitude = latitude,
                                longitude = longitude
                            )
                        }
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp).padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = label.isNotBlank() && uri.isNotBlank()
            ) {
                Text(if (locationId == null) "Save" else "Update", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun RowScope.CategoryChip(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.weight(1f).height(48.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = if (selected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) 
                 else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text, style = MaterialTheme.typography.labelLarge, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
