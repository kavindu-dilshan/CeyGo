package com.example.ceygo.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.ceygo.data.db.user.UserDao
import com.example.ceygo.data.db.user.UserEntity

@Database(
    entities = [
        LocationEntity::class,
        LocationImageEntity::class,
        ReviewEntity::class,
        // Auth
        UserEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun locationDao(): LocationDao
    abstract fun userDao(): UserDao
}
