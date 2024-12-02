package com.ludito.task.data.bookmark.repository

import com.ludito.task.data.bookmark.db.dao.BookmarkDao
import com.ludito.task.data.bookmark.db.model.LocationEntity
import com.ludito.task.data.bookmark.dto.LocationDto
import com.ludito.task.domain.bookmark.repository.BookmarkRepository
import javax.inject.Inject

class BookmarkRepositoryImpl @Inject constructor(
    private val bookmarkDao: BookmarkDao
) : BookmarkRepository {

    override fun insertLocation(location: LocationDto) {
        bookmarkDao.insertLocation(
            LocationEntity(
                latitude = location.latitude,
                longitude = location.longitude,
                name = location.name,
                description = location.description
            )
        )
    }

    override fun getLocationsList(): List<LocationDto> {
        return bookmarkDao.getLocations().map {
            LocationDto(it.latitude, it.longitude, it.name, it.description)
        }
    }
}