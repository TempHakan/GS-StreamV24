package com.example.data.local

import androidx.room.*
import com.example.data.model.LockedCategory
import com.example.data.model.ParentalSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface ParentalControlDao {
    @Query("SELECT * FROM parental_settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<ParentalSettings?>

    @Query("SELECT * FROM parental_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsDirect(): ParentalSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(settings: ParentalSettings)

    @Query("SELECT categoryName FROM locked_categories ORDER BY categoryName ASC")
    fun getLockedCategories(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun lockCategory(category: LockedCategory)

    @Query("DELETE FROM locked_categories WHERE categoryName = :categoryName")
    suspend fun unlockCategory(categoryName: String)

    @Query("SELECT COUNT(*) FROM locked_categories WHERE categoryName = :categoryName")
    suspend fun isCategoryLocked(categoryName: String): Int

    @Query("DELETE FROM locked_categories")
    suspend fun clearLockedCategories()
}
