package com.tools.warrantybox.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tools.warrantybox.data.WarrantyDatabase
import com.tools.warrantybox.data.WarrantyItem
import com.tools.warrantybox.data.WarrantyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class WarrantyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: WarrantyRepository
    
    init {
        val database = WarrantyDatabase.getDatabase(application)
        repository = WarrantyRepository(database.warrantyDao())
    }

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory

    val allWarranties = repository.allWarranties

    val warranties: StateFlow<List<WarrantyItem>> = repository.allWarranties
        .combine(_searchQuery) { items, query ->
            if (query.isBlank()) items else items.filter { it.name.contains(query, ignoreCase = true) }
        }
        .combine(_selectedCategory) { items, category ->
            if (category == null) items else items.filter { it.category == category }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun insert(item: WarrantyItem) = viewModelScope.launch {
        repository.insertWarranty(item)
    }
    
    suspend fun getWarrantyById(id: Long): WarrantyItem? {
        return repository.getWarrantyById(id)
    }

    fun update(item: WarrantyItem) = viewModelScope.launch {
        repository.updateWarranty(item)
    }

    fun delete(item: WarrantyItem) = viewModelScope.launch {
        repository.deleteWarranty(item)
        item.receiptPhotoUri?.let { uriStr ->
            val file = File(uriStr)
            if (file.exists()) {
                file.delete()
            }
        }
    }
    
    fun replaceAll(items: List<WarrantyItem>) = viewModelScope.launch {
        repository.replaceAll(items)
    }

    fun copyImageToInternalStorage(uri: Uri): String? {
        try {
            val inputStream = getApplication<Application>().contentResolver.openInputStream(uri) ?: return null
            val fileName = "receipt_${System.currentTimeMillis()}.jpg"
            val file = File(getApplication<Application>().filesDir, fileName)
            val outputStream = FileOutputStream(file)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
