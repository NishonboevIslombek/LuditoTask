package com.ludito.task.presentation.map.model

sealed interface TypeSpecificState {
    data class Toponym(val address: String) : TypeSpecificState

    data class Business(
        val name: String,
        val workingHours: String?,
        val categories: String?,
        val phones: String?,
        val link: String?,
    ) : TypeSpecificState

    object Undefined : TypeSpecificState
}