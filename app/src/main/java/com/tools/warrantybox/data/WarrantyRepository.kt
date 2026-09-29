package com.tools.warrantybox.data

import kotlinx.coroutines.flow.Flow

class WarrantyRepository(private val warrantyDao: WarrantyDao) {
    val allWarranties: Flow<List<WarrantyItem>> = warrantyDao.getAllWarranties()

    suspend fun getWarrantyById(id: Long): WarrantyItem? {
        return warrantyDao.getWarrantyById(id)
    }

    suspend fun insertWarranty(item: WarrantyItem): Long {
        return warrantyDao.insertWarranty(item)
    }

    suspend fun updateWarranty(item: WarrantyItem) {
        warrantyDao.updateWarranty(item)
    }

    suspend fun deleteWarranty(item: WarrantyItem) {
        warrantyDao.deleteWarranty(item)
    }

    suspend fun replaceAll(items: List<WarrantyItem>) {
        warrantyDao.deleteAll()
        warrantyDao.insertAll(items)
    }
}
