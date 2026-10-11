package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.VodWatchHistory
import kotlinx.coroutines.flow.Flow

/**
 * VOD İzleme geçmişi (Continue Watching) için Room DAO arayüzü.
 */
@Dao
interface VodWatchHistoryDao {

    /**
     * Kullanıcının en son izlediği VOD içeriklerini kronolojik olarak getirir.
     */
    @Query("SELECT * FROM vod_watch_history ORDER BY lastWatchedTimestamp DESC LIMIT :limit")
    fun getContinueWatching(limit: Int = 20): Flow<List<VodWatchHistory>>

    /**
     * Belirli bir VOD için kayıtlı izleme pozisyonunu getirir.
     */
    @Query("SELECT * FROM vod_watch_history WHERE vodId = :vodId LIMIT 1")
    suspend fun getWatchHistoryById(vodId: String): VodWatchHistory?

    /**
     * VOD izleme pozisyonunu kaydeder veya günceller.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWatchProgress(history: VodWatchHistory)

    /**
     * Belirli bir VOD geçmiş kaydını siler.
     */
    @Query("DELETE FROM vod_watch_history WHERE vodId = :vodId")
    suspend fun deleteWatchHistoryById(vodId: String)

    /**
     * Tüm izleme geçmişini temizler.
     */
    @Query("DELETE FROM vod_watch_history")
    suspend fun clearAllHistory()
}
