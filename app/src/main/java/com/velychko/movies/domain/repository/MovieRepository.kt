package com.velychko.movies.domain.repository

import com.velychko.movies.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    /** An empty list means "nothing found"; failures (network, API errors) are returned as [Result.failure]. */
    suspend fun searchMovies(query: String): Result<List<Movie>>

    /** Looks in favorites first (works offline), then falls back to the API. */
    suspend fun getMovie(imdbId: String): Result<Movie>

    fun observeFavorites(): Flow<List<Movie>>
    fun observeIsFavorite(imdbId: String): Flow<Boolean>

    /** Adds the movie to favorites, or removes it if it is already there. */
    suspend fun toggleFavorite(movie: Movie)
}
