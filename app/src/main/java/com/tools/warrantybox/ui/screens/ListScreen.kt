package com.tools.warrantybox.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tools.warrantybox.data.WarrantyItem
import com.tools.warrantybox.data.getDaysLeft
import com.tools.warrantybox.data.getExpiryDateMillis
import com.tools.warrantybox.ui.WarrantyViewModel
import kotlinx.coroutines.launch
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    viewModel: WarrantyViewModel,
    onNavigateToAdd: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val items by viewModel.warranties.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val currentTime = System.currentTimeMillis()

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var isSearchVisible by remember { mutableStateOf(false) }

    val allCategories = items.map { it.category }.distinct().sorted()

    val activeItems = items.filter { it.getDaysLeft(currentTime) > 0 }
        .sortedBy { it.getDaysLeft(currentTime) }
    val expiredItems = items.filter { it.getDaysLeft(currentTime) <= 0 }
        .sortedByDescending { it.getExpiryDateMillis() }

    val displayItems = if (selectedTabIndex == 0) activeItems else expiredItems

    val expiringSoonCount = activeItems.count { it.getDaysLeft(currentTime) <= 60 }
    val nextToExpire = activeItems.firstOrNull()

    // Backup launcher
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val outputStream = context.contentResolver.openOutputStream(it)
                    val writer = OutputStreamWriter(outputStream)
                    writer.write(Gson().toJson(items))
                    writer.close()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Restore launcher
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(it)
                    val reader = InputStreamReader(inputStream)
                    val itemType = object : TypeToken<List<WarrantyItem>>() {}.type
                    val importedItems: List<WarrantyItem> = Gson().fromJson(reader, itemType)
                    reader.close()
                    viewModel.replaceAll(importedItems)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (isSearchVisible) {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { viewModel.updateSearchQuery(it) },
                    onClose = {
                        isSearchVisible = false
                        viewModel.updateSearchQuery("")
                    }
                )
            } else {
                TopAppBar(
                    title = { Text("WarrantyBox") },
                    actions = {
                        IconButton(onClick = { isSearchVisible = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        var expanded by remember { mutableStateOf(false) }
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Backup Data") },
                                onClick = {
                                    expanded = false
                                    exportLauncher.launch("warranty_backup.json")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Restore Data") },
                                onClick = {
                                    expanded = false
                                    importLauncher.launch(arrayOf("application/json"))
                                }
                            )
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add Warranty")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {

            // Summary Card
            if (items.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            if (expiringSoonCount > 0)
                                "$expiringSoonCount warrant${if (expiringSoonCount == 1) "y" else "ies"} expiring within 60 days"
                            else
                                "All warranties looking good!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (nextToExpire != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            val days = nextToExpire.getDaysLeft(currentTime)
                            Text(
                                "Next: ${nextToExpire.name} (in $days day${if (days != 1) "s" else ""})",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Category filter chips
            if (allCategories.size > 1) {
                LazyRow(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.updateSelectedCategory(null) },
                            label = { Text("All") }
                        )
                    }
                    items(allCategories) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = {
                                viewModel.updateSelectedCategory(
                                    if (selectedCategory == cat) null else cat
                                )
                            },
                            label = { Text(cat) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Active (${activeItems.size})") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Expired (${expiredItems.size})") }
                )
            }

            if (displayItems.isEmpty()) {
                EmptyState(isExpired = selectedTabIndex == 1)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayItems, key = { it.id }) { item ->
                        WarrantyCard(item = item, currentTime = currentTime) {
                            onNavigateToDetail(item.id)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, onClose: () -> Unit) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Search warranties...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close Search")
            }
        }
    )
}

@Composable
private fun EmptyState(isExpired: Boolean) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isExpired) "🎉" else "📦",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isExpired)
                    "No expired warranties"
                else
                    "No active warranties yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (!isExpired) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Tap + to add your first one",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun WarrantyCard(item: WarrantyItem, currentTime: Long, onClick: () -> Unit) {
    val daysLeft = item.getDaysLeft(currentTime)

    val badgeColor = when {
        daysLeft <= 0 -> Color.Gray
        daysLeft <= 30 -> Color(0xFFD32F2F) // Red
        daysLeft <= 90 -> Color(0xFFF57C00) // Amber
        else -> Color(0xFF388E3C)           // Green
    }

    val daysLeftText = when {
        daysLeft < 0 -> "Expired ${-daysLeft}d ago"
        daysLeft == 0 -> "Expires today"
        else -> "in $daysLeft days"
    }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val expiryDateString = dateFormat.format(Date(item.getExpiryDateMillis()))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon placeholder
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    getCategoryEmoji(item.category),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Expires: $expiryDateString",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                color = badgeColor,
                shape = RoundedCornerShape(16.dp),
            ) {
                Text(
                    text = daysLeftText,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private fun getCategoryEmoji(category: String): String {
    return when (category.lowercase()) {
        "electronics" -> "💻"
        "appliances" -> "🏠"
        "furniture" -> "🪑"
        "vehicle" -> "🚗"
        "clothing" -> "👕"
        "jewelry" -> "💍"
        "tools" -> "🔧"
        "sports" -> "⚽"
        "toys" -> "🧸"
        "health" -> "🏥"
        else -> "📦"
    }
}
