package com.example.data.local

import androidx.room.*
import com.example.data.model.Channel
import com.example.data.model.StreamType
import kotlinx.coroutines.flow.Flow

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY name ASC")
    fun getAllChannels(): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE streamType = :type ORDER BY name ASC")
    fun getChannelsByType(type: StreamType): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE isFavorite = 1 ORDER BY name ASC")
    fun getFavoriteChannels(): Flow<List<Channel>>

    @Query("SELECT * FROM channels WHERE lastWatchedTime > 0 ORDER BY lastWatchedTime DESC LIMIT 20")
    fun getRecentChannels(): Flow<List<Channel>>

    @Query("SELECT DISTINCT groupTitle FROM channels WHERE streamType = :type ORDER BY groupTitle ASC")
    fun getCategories(type: StreamType): Flow<List<String>>

    @Query("SELECT * FROM channels WHERE id = :id LIMIT 1")
    suspend fun getChannelById(id: String): Channel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<Channel>)

    @Update
    suspend fun updateChannel(channel: Channel)

    @Query("UPDATE channels SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: String, isFav: Boolean)

    @Query("UPDATE channels SET lastWatchedTime = :time WHERE id = :id")
    suspend fun updateLastWatched(id: String, time: Long)

    @Query("DELETE FROM channels WHERE playlistId = :playlistId")
    suspend fun deleteByPlaylistId(playlistId: String)

    @Query("DELETE FROM channels")
    suspend fun clearAll()
}
