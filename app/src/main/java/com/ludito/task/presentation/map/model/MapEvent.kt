package com.ludito.task.presentation.map.model

sealed interface MapEvent {
    data class Error(val error: String) : MapEvent
}