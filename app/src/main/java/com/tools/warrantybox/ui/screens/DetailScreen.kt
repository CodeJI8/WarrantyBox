package com.tools.warrantybox.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.rememberAsyncImagePainter
import com.tools.warrantybox.data.WarrantyItem
import com.tools.warrantybox.data.getExpiryDateMillis
import com.tools.warrantybox.ui.WarrantyViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: WarrantyViewModel,
    itemId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: () -> Unit
) {
    var item by remember { mutableStateOf<WarrantyItem?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showFullScreenImage by remember { mutableStateOf(false) }

    LaunchedEffect(itemId) {
        item = viewModel.getWarrantyById(itemId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { showDeleteConfirm = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        }
    ) { padding ->
        val currentItem = item
        if (currentItem == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(currentItem.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        DetailRow("Category", currentItem.category)
                        
                        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                        DetailRow("Purchase Date", dateFormat.format(Date(currentItem.purchaseDateMillis)))
                        DetailRow("Expiry Date", dateFormat.format(Date(currentItem.getExpiryDateMillis())))
                        DetailRow("Warranty Length", "${currentItem.warrantyMonths} months")
                        
                        if (!currentItem.storeName.isNullOrBlank()) {
                            DetailRow("Store", currentItem.storeName)
                        }
                        if (currentItem.price != null) {
                            DetailRow("Price", "$${currentItem.price}")
                        }
                    }
                }

                if (!currentItem.notes.isNullOrBlank()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Notes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(currentItem.notes)
                        }
                    }
                }

                if (currentItem.receiptPhotoUri != null) {
                    Text("Receipt Photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable { showFullScreenImage = true }
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(File(currentItem.receiptPhotoUri)),
                            contentDescription = "Receipt Photo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    if (showDeleteConfirm && item != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Warranty") },
            text = { Text("Are you sure you want to delete ${item?.name}? This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(item!!)
                    showDeleteConfirm = false
                    onNavigateBack()
                }) {
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

    if (showFullScreenImage && item?.receiptPhotoUri != null) {
        FullScreenImageViewer(
            imagePath = item!!.receiptPhotoUri!!,
            onDismiss = { showFullScreenImage = false }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun FullScreenImageViewer(imagePath: String, onDismiss: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset += offsetChange
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .clickable { onDismiss() }
        ) {
            Image(
                painter = rememberAsyncImagePainter(File(imagePath)),
                contentDescription = "Full Screen Receipt",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
                    .transformable(state = transformableState),
                contentScale = ContentScale.Fit
            )
        }
    }
}
