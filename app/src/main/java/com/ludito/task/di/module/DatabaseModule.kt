package com.ludito.task.di.module

import android.content.Context
import androidx.room.Room
import com.ludito.task.data.bookmark.db.dao.BookmarkDao
import com.ludito.task.data.core.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context?): AppDatabase {
        return Room.databaseBuilder(context!!, AppDatabase::class.java, "ludito_app.db")
            .allowMainThreadQueries()
            .build()
    }

    @Provides
    @Singleton
    fun provideBookmarkDao(appDatabase: AppDatabase): BookmarkDao {
        return appDatabase.bookmarkDao()
    }
}