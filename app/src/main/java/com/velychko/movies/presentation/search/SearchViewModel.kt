package com.velychko.movies.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.velychko.movies.domain.repository.MovieRepository
import com.velychko.movies.presentation.toErrorReason
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

private const val DEBOUNCE_MS = 400L
private const val STOP_TIMEOUT_MS = 5_000L

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: MovieRepository,
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val retryTrigger = MutableStateFlow(0)

    /**
     * typing -> debounce -> trim -> distinct -> cancel the previous request -> load.
     * Blank input resets to Idle without touching the network.
     */
    val uiState: StateFlow<SearchUiState> = combine(
        _query.debounce(DEBOUNCE_MS.milliseconds).map { it.trim() }.distinctUntilChanged(),
        retryTrigger,
    ) { query, _ -> query }
        .flatMapLatest { query -> search(query) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
            SearchUiState.Idle
        )

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun onRetry() {
        retryTrigger.update { it + 1 }
    }

    private fun search(query: String): Flow<SearchUiState> {
        if (query.isEmpty()) return flowOf(SearchUiState.Idle)
        return flow {
            emit(SearchUiState.Loading)
            emit(
                repository.searchMovies(query).fold(
                    onSuccess = { movies ->
                        if (movies.isEmpty()) SearchUiState.Empty else SearchUiState.Success(movies.withAds())
                    },
                    onFailure = { error -> SearchUiState.Error(error.toErrorReason()) },
                ),
            )
        }
    }
}
