package com.ludito.task.presentation.map.model

import com.yandex.mapkit.geometry.Point

data class MapUiState(
    var isLoading: Boolean = false,
    var title: String? = null,
    var descriptionText: String? = "",
    var location: Point? = null,
    var lastLocation: Point? = null,
    var geoObjectType: TypeSpecificState = TypeSpecificState.Undefined,
    var listLocations: List<PlaceItem> = emptyList()
)