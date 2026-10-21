package com.velychko.movies.presentation.details

import com.velychko.movies.domain.model.Movie
import com.velychko.movies.presentation.ErrorReason

sealed interface DetailsUiState {
    data object Loading : DetailsUiState
    data class Success(val movie: Movie, val isFavorite: Boolean) : DetailsUiState
    data class Error(val reason: ErrorReason) : DetailsUiState
}
