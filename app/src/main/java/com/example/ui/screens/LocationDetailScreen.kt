package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.LocationEntry
import com.example.ui.PinBookViewModel

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationDetailScreen(
    viewModel: PinBookViewModel,
    locationId: Int,
    onNavigateBack: () -> Unit,
    onEdit: (Int) -> Unit
) {
    var location by remember { mutableStateOf<LocationEntry?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(locationId) {
        location = viewModel.getLocationById(locationId)
    }

    val loc = location ?: return

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp, top = 8.dp)
            ) {
                Text(
                    "Choose an action",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 16.dp)
                )
                
                BottomSheetItem(
                    icon = Icons.Filled.OpenInNew,
                    title = "Open With",
                    subtitle = "Open in Maps or other apps",
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = { 
                        showBottomSheet = false
                        openWith(context, loc) 
                    }
                )
                BottomSheetItem(
                    icon = Icons.Filled.Share,
                    title = "Share Location",
                    subtitle = "Share this location",
                    iconTint = MaterialTheme.colorScheme.primary,
                    onClick = { 
                        showBottomSheet = false
                        shareLocation(context, loc) 
                    }
                )
                BottomSheetItem(
                    icon = Icons.Filled.Edit,
                    title = "Edit Place",
                    subtitle = "Edit name, notes, category",
                    iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = { 
                        showBottomSheet = false
                        onEdit(loc.id)
                    }
                )
                BottomSheetItem(
                    icon = Icons.Filled.Delete,
                    title = "Delete Place",
                    subtitle = "Remove from PinBook",
                    iconTint = MaterialTheme.colorScheme.error,
                    titleColor = MaterialTheme.colorScheme.error,
                    onClick = { 
                        showBottomSheet = false
                        showDeleteConfirm = true
                    }
                )
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Place") },
            text = { Text("Are you sure you want to delete this place? This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { 
                        showDeleteConfirm = false
                        viewModel.deleteLocation(loc)
                        onNavigateBack()
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(loc.id) }) {
                        Icon(Icons.Filled.Edit, "Edit")
                    }
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(Icons.Filled.MoreVert, "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingVals ->
        Column(
            modifier = Modifier
                .padding(paddingVals)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Map Mock Component
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFFE8F0FE))
                    .clickable { openWith(context, loc) },
                contentAlignment = Alignment.Center
            ) {
                // Map background pattern placeholder
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = loc.label,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )
                IconButton(onClick = { viewModel.toggleFavorite(loc); location = loc.copy(isFavorite = !loc.isFavorite) }) {
                    Icon(
                        imageVector = if (loc.isFavorite) Icons.Filled.Star else Icons.Filled.StarOutline,
                        contentDescription = "Favorite",
                        tint = if (loc.isFavorite) Color(0xFFFFC107) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp)).padding(horizontal = 8.dp, vertical = 4.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Business, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(loc.category, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (loc.city.isNotBlank()) "• ${loc.city}" else "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionItem(Icons.Filled.OpenInNew, "Open With", MaterialTheme.colorScheme.primary) { openWith(context, loc) }
                ActionItem(Icons.Filled.Share, "Share", MaterialTheme.colorScheme.primary) { shareLocation(context, loc) }
                ActionItem(Icons.Filled.MoreHoriz, "More", MaterialTheme.colorScheme.onSurfaceVariant) { showBottomSheet = true }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text("DETAILS", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DetailRow(Icons.Filled.LocationOn, loc.address.ifBlank { "No address provided" })
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), modifier = Modifier.padding(start=56.dp))
                    
                    val latStr = if (loc.latitude != null) { if (loc.latitude >= 0) "${loc.latitude}° N" else "${-loc.latitude}° S" } else ""
                    val lngStr = if (loc.longitude != null) { if (loc.longitude >= 0) "${loc.longitude}° E" else "${-loc.longitude}° W" } else ""
                    val exploreText = if (loc.latitude != null && loc.longitude != null) "$latStr, $lngStr" else "Coordinates unavailable"
                    
                    DetailRow(Icons.Filled.Explore, exploreText)
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), modifier = Modifier.padding(start=56.dp))
                    
                    val dateStr = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(loc.timestamp))
                    DetailRow(Icons.Filled.CalendarToday, "Saved on $dateStr")
                    
                    if (loc.notes.isNotBlank()) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f), modifier = Modifier.padding(start=56.dp))
                        DetailRow(Icons.Filled.Notes, loc.notes)
                    }
                }
            }
        }
    }
}

@Composable
fun ActionItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, tint: Color, onClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable(onClick = onClick).padding(8.dp)) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surface, CircleShape)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = tint)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(20.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun BottomSheetItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconTint: Color,
    titleColor: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(24.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyLarge, color = titleColor, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

fun openWith(context: Context, location: LocationEntry) {
    val uri = if (location.latitude != null && location.longitude != null) {
        Uri.parse("geo:${location.latitude},${location.longitude}?q=${location.latitude},${location.longitude}(${Uri.encode(location.label)})")
    } else {
        Uri.parse(location.uri.ifBlank { "https://maps.google.com" })
    }
    
    try {
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        val chooser = Intent.createChooser(intent, "Open with...")
        context.startActivity(chooser)
    } catch (e: Exception) {
        val fallbackUri = Uri.parse(location.uri.ifBlank { "https://maps.google.com/?q=${location.latitude},${location.longitude}" })
        val fallbackIntent = Intent(Intent.ACTION_VIEW, fallbackUri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(fallbackIntent)
        } catch (e2: Exception) {}
    }
}

fun shareLocation(context: Context, location: LocationEntry) {
    val shareText = "Location: ${location.label}\n${location.uri}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, shareText)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, "Share via..."))
}
