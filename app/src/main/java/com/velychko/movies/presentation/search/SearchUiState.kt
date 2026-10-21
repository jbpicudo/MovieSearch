package com.velychko.movies.presentation.search

import com.velychko.movies.presentation.ErrorReason

sealed interface SearchUiState {
    data object Idle : SearchUiState
    data object Loading : SearchUiState
    data object Empty : SearchUiState
    data class Success(val items: List<ListItem>) : SearchUiState
    data class Error(val reason: ErrorReason) : SearchUiState
}
