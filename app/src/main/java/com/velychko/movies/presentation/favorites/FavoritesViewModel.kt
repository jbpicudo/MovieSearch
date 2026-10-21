package com.velychko.movies.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velychko.movies.domain.model.Movie
import com.velychko.movies.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val STOP_TIMEOUT_MS = 5_000L

data class FavoritesUiState(
    val movies: List<Movie> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: MovieRepository,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = repository.observeFavorites()
        .map { FavoritesUiState(movies = it, isLoading = false) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            FavoritesUiState()
        )

    /** Items on this screen are favorites by definition, so toggling removes them. */
    fun onRemove(movie: Movie) {
        viewModelScope.launch { repository.toggleFavorite(movie) }
    }
}
