package com.ludito.task.data.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ludito.task.data.bookmark.db.dao.BookmarkDao
import com.ludito.task.data.bookmark.db.model.LocationEntity

@Database(
    entities = [LocationEntity::class],
    version = 1
)
@TypeConverters(StringListConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookmarkDao(): BookmarkDao
}