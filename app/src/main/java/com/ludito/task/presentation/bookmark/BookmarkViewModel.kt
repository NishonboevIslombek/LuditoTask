package com.ludito.task.presentation.bookmark

import androidx.lifecycle.ViewModel
import com.ludito.task.domain.bookmark.repository.BookmarkRepository
import com.ludito.task.presentation.bookmark.model.BookmarkUiState
import com.ludito.task.presentation.map.model.PlaceItem
import com.yandex.mapkit.geometry.Point
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class BookmarkViewModel @Inject constructor(
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookmarkUiState())
    val uiState: StateFlow<BookmarkUiState>
        get() = _uiState

    init {
        getLocations()
    }

    /**
     * Retrieves the list of saved locations from the repository and updates the UI state.
     */
    private fun getLocations() {
        val data = bookmarkRepository.getLocationsList()

        _uiState.update { currentUiState ->
            currentUiState.copy(list = data.map {
                PlaceItem(
                    name = it.name,
                    description = it.description,
                    location = Point(it.latitude, it.longitude)
                )
            })
        }
    }
}