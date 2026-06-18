package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.LocationEntry
import com.example.ui.PinBookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PinBookViewModel,
    onAddLocation: () -> Unit,
    onLocationClick: (Int) -> Unit
) {
    val locations by viewModel.locations.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    val favorites = locations.filter { it.isFavorite }
    val others = locations.filter { !it.isFavorite }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Place, contentDescription = "Places") },
                    label = { Text("Places") },
                    selected = true,
                    onClick = { },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Group, contentDescription = "Groups") },
                    label = { Text("Groups") },
                    selected = false,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { }
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddLocation,
                icon = { Icon(Icons.Filled.Add, "Add Location") },
                text = { Text("Add Location") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(24.dp)
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.Place,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PinBook",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row {
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Search, "Search")
                        }
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.MoreVert, "More")
                        }
                    }
                }
            }

            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.onSearchQueryChange(it) },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search places, people, or notes") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChange("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = CircleShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            if (favorites.isNotEmpty()) {
                item {
                    Text(
                        text = "FAVORITES",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                }
                items(favorites, key = { it.id }) { loc ->
                    SimpleLocationItem(
                        location = loc,
                        onClick = { onLocationClick(loc.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(loc) },
                        isFavorite = true
                    )
                }
            }

            if (others.isNotEmpty() || favorites.isEmpty()) {
                item {
                    Text(
                        text = if(favorites.isEmpty()) "PLACES" else "ALL PLACES",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                    )
                }
                items(others, key = { it.id }) { loc ->
                    SimpleLocationItem(
                        location = loc,
                        onClick = { onLocationClick(loc.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(loc) },
                        isFavorite = false
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(80.dp)) // space for FAB
            }
        }
    }
}

@Composable
fun SimpleLocationItem(
    location: LocationEntry,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    isFavorite: Boolean
) {
    val (iconBgColor, iconColor, iconVector) = when (location.category) {
        "Home" -> Triple(Color(0xFF6750A4), Color.White, Icons.Filled.Home) // Purple
        "Office" -> Triple(Color(0xFF0288D1), Color.White, Icons.Filled.Business) // Blue
        else -> Triple(Color(0xFF4CAF50), Color.White, Icons.Filled.Place) // Green
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color = iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(iconVector, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = location.label,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = location.category,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (location.city.isNotBlank()) {
                        Text(
                            text = " • ${location.city}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            IconButton(onClick = onToggleFavorite) {
                if (isFavorite) {
                    Icon(Icons.Filled.Star, contentDescription = "Favorite", tint = Color(0xFFFFC107))
                } else {
                    Icon(Icons.Filled.StarOutline, contentDescription = "Favorite", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
