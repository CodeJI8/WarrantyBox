package com.tools.warrantybox.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface WarrantyDao {
    @Query("SELECT * FROM warranties")
    fun getAllWarranties(): Flow<List<WarrantyItem>>

    @Query("SELECT * FROM warranties WHERE id = :id")
    suspend fun getWarrantyById(id: Long): WarrantyItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarranty(item: WarrantyItem): Long

    @Update
    suspend fun updateWarranty(item: WarrantyItem)

    @Delete
    suspend fun deleteWarranty(item: WarrantyItem)
    
    @Query("DELETE FROM warranties")
    suspend fun deleteAll()
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WarrantyItem>)
}
