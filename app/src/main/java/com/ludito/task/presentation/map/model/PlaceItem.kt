package com.ludito.task.presentation.map.model

import com.yandex.mapkit.geometry.Point

data class PlaceItem(
    val name: String,
    val description: String,
    val distance: Int = 0,
    var location: Point? = null,
    var geoObjectType: TypeSpecificState = TypeSpecificState.Undefined
)
