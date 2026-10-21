package com.velychko.movies.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velychko.movies.domain.model.Movie
import com.velychko.movies.domain.repository.MovieRepository
import com.velychko.movies.presentation.navigation.DETAILS_ARG_IMDB_ID
import com.velychko.movies.presentation.toErrorReason
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STOP_TIMEOUT_MS = 5_000L

@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository,
) : ViewModel() {

    private val imdbId: String =
        checkNotNull(savedStateHandle.get<String>(DETAILS_ARG_IMDB_ID)) { "imdbId argument is missing" }

    /** null = still loading. */
    private val movieResult = MutableStateFlow<Result<Movie>?>(null)

    val uiState: StateFlow<DetailsUiState> = combine(
        movieResult,
        repository.observeIsFavorite(imdbId), // the heart reacts to the database, not to local flags
    ) { result, isFavorite ->
        when {
            result == null -> DetailsUiState.Loading
            result.isSuccess -> DetailsUiState.Success(result.getOrThrow(), isFavorite)
            else -> DetailsUiState.Error(result.exceptionOrNull().toErrorReason())
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        DetailsUiState.Loading
    )

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            movieResult.value = null
            movieResult.value = repository.getMovie(imdbId)
        }
    }

    fun onToggleFavorite() {
        val movie = (uiState.value as? DetailsUiState.Success)?.movie ?: return
        viewModelScope.launch { repository.toggleFavorite(movie) }
    }
}
