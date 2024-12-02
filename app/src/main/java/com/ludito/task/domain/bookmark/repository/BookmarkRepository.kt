package com.ludito.task.domain.bookmark.repository

import com.ludito.task.data.bookmark.dto.LocationDto

interface BookmarkRepository {
    fun insertLocation(location: LocationDto)

    fun getLocationsList(): List<LocationDto>
}