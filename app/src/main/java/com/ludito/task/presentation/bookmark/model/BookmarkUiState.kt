package com.ludito.task.presentation.bookmark.model

import com.ludito.task.presentation.map.model.PlaceItem
import com.yandex.mapkit.geometry.Point

data class BookmarkUiState(
    val list: List<PlaceItem> = emptyList()
)
