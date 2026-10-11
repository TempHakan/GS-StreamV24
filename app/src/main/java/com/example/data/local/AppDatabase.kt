package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.Channel
import com.example.data.model.LockedCategory
import com.example.data.model.ParentalSettings
import com.example.data.model.Playlist
import com.example.data.model.VodWatchHistory

@Database(
    entities = [Channel::class, Playlist::class, LockedCategory::class, ParentalSettings::class, VodWatchHistory::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun channelDao(): ChannelDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun parentalControlDao(): ParentalControlDao
    abstract fun vodWatchHistoryDao(): VodWatchHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "streamflow_iptv.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
