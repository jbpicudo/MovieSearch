package com.velychko.movies.testutil

import com.velychko.movies.domain.model.Movie
import com.velychko.movies.domain.repository.MovieRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Duration.Companion.milliseconds

class FakeMovieRepository : MovieRepository {

    var searchResult: Result<List<Movie>> = Result.success(emptyList())
    var movieResult: Result<Movie> = Result.failure(IllegalStateException("movieResult not set"))
    val searchQueries = mutableListOf<String>()

    private val favorites = MutableStateFlow<List<Movie>>(emptyList())

    override suspend fun searchMovies(query: String): Result<List<Movie>> {
        searchQueries += query
        delay(SEARCH_LATENCY_MS.milliseconds) // lets tests observe the Loading state
        return searchResult
    }

    override suspend fun getMovie(imdbId: String): Result<Movie> {
        delay(SEARCH_LATENCY_MS.milliseconds) // lets tests observe the Loading state
        return movieResult
    }

    override fun observeFavorites(): Flow<List<Movie>> = favorites

    override fun observeIsFavorite(imdbId: String): Flow<Boolean> =
        favorites.map { list -> list.any { it.imdbId == imdbId } }

    override suspend fun toggleFavorite(movie: Movie) {
        favorites.update { list ->
            if (list.any { it.imdbId == movie.imdbId }) list.filterNot { it.imdbId == movie.imdbId } else list + movie
        }
    }

    private companion object {
        const val SEARCH_LATENCY_MS = 100L
    }
}
