package com.tools.warrantybox.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.tools.warrantybox.data.WarrantyItem
import com.tools.warrantybox.ui.WarrantyViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditScreen(
    viewModel: WarrantyViewModel,
    itemId: Long,
    onNavigateBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val isEdit = itemId != -1L

    var name by remember { mutableStateOf("") }
    var purchaseDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var warrantyMonths by remember { mutableStateOf("") }
    var storeName by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var receiptPhotoUri by remember { mutableStateOf<String?>(null) }
    var isDatePickerVisible by remember { mutableStateOf(false) }
    var isLoaded by remember { mutableStateOf(!isEdit) }

    LaunchedEffect(itemId) {
        if (isEdit) {
            val item = viewModel.getWarrantyById(itemId)
            if (item != null) {
                name = item.name
                purchaseDateMillis = item.purchaseDateMillis
                warrantyMonths = item.warrantyMonths.toString()
                storeName = item.storeName ?: ""
                price = item.price?.toString() ?: ""
                category = item.category
                notes = item.notes ?: ""
                receiptPhotoUri = item.receiptPhotoUri
            }
            isLoaded = true
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                coroutineScope.launch {
                    val path = viewModel.copyImageToInternalStorage(uri)
                    if (path != null) {
                        receiptPhotoUri = path
                    }
                }
            }
        }
    )

    if (!isLoaded) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Warranty" else "Add Warranty") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            val months = warrantyMonths.toIntOrNull() ?: 0
                            val p = price.toDoubleOrNull()
                            val item = WarrantyItem(
                                id = if (isEdit) itemId else 0,
                                name = name.trim(),
                                purchaseDateMillis = purchaseDateMillis,
                                warrantyMonths = months,
                                storeName = storeName.trim().ifBlank { null },
                                price = p,
                                category = category.trim().ifBlank { "Uncategorized" },
                                notes = notes.trim().ifBlank { null },
                                receiptPhotoUri = receiptPhotoUri
                            )
                            if (isEdit) viewModel.update(item) else viewModel.insert(item)
                            onNavigateBack()
                        },
                        enabled = name.isNotBlank() && (warrantyMonths.toIntOrNull() ?: 0) > 0
                    ) {
                        Text("Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Item Name *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            OutlinedTextField(
                value = dateFormat.format(Date(purchaseDateMillis)),
                onValueChange = {},
                label = { Text("Purchase Date *") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isDatePickerVisible = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            if (isDatePickerVisible) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = purchaseDateMillis
                )
                DatePickerDialog(
                    onDismissRequest = { isDatePickerVisible = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { purchaseDateMillis = it }
                            isDatePickerVisible = false
                        }) { Text("OK") }
                    },
                    dismissButton = {
                        TextButton(onClick = { isDatePickerVisible = false }) { Text("Cancel") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            OutlinedTextField(
                value = warrantyMonths,
                onValueChange = { warrantyMonths = it.filter { c -> c.isDigit() } },
                label = { Text("Warranty Length (Months) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Category") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("e.g. Electronics, Appliances, Furniture") }
            )

            OutlinedTextField(
                value = storeName,
                onValueChange = { storeName = it },
                label = { Text("Store (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text("Price (Optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (receiptPhotoUri == null) "📷 Add Receipt Photo" else "📷 Change Receipt Photo")
            }

            if (receiptPhotoUri != null) {
                Text(
                    "✅ Receipt photo attached",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
