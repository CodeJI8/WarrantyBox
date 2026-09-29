package com.tools.warrantybox.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Calendar

@Entity(tableName = "warranties")
data class WarrantyItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val purchaseDateMillis: Long,
    val warrantyMonths: Int,
    val storeName: String?,
    val price: Double?,
    val category: String,
    val notes: String?,
    val receiptPhotoUri: String?
)

/**
 * Pure utility functions for warranty date calculations.
 * Separated from the entity for testability.
 */
object WarrantyDateUtil {

    /**
     * Calculates the exact expiry date in milliseconds.
     * Uses java.util.Calendar which handles leap years and month-end edge cases.
     * e.g. Jan 31 + 1 month = Feb 28 (or 29 in a leap year).
     */
    fun getExpiryDateMillis(purchaseDateMillis: Long, warrantyMonths: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = purchaseDateMillis
        // Zero out time components for consistent day-level calculation
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        calendar.add(Calendar.MONTH, warrantyMonths)
        return calendar.timeInMillis
    }

    /**
     * Calculates days left from a given current time in millis.
     * Returns positive for days remaining, 0 for "today", negative for days past.
     */
    fun getDaysLeft(purchaseDateMillis: Long, warrantyMonths: Int, currentTimeMillis: Long): Int {
        val expiry = getExpiryDateMillis(purchaseDateMillis, warrantyMonths)

        val currentCal = Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val expiryCal = Calendar.getInstance().apply {
            timeInMillis = expiry
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val diffMs = expiryCal.timeInMillis - currentCal.timeInMillis
        return (diffMs / (1000L * 60 * 60 * 24)).toInt()
    }
}

/** Extension functions for convenience */
fun WarrantyItem.getExpiryDateMillis(): Long =
    WarrantyDateUtil.getExpiryDateMillis(purchaseDateMillis, warrantyMonths)

fun WarrantyItem.getDaysLeft(currentTimeMillis: Long): Int =
    WarrantyDateUtil.getDaysLeft(purchaseDateMillis, warrantyMonths, currentTimeMillis)
