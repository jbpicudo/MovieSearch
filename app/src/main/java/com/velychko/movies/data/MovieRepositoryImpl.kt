package com.velychko.movies.data

import com.velychko.movies.data.local.FavoriteDao
import com.velychko.movies.data.remote.OmdbApi
import com.velychko.movies.domain.model.Movie
import com.velychko.movies.domain.model.MovieApiException
import com.velychko.movies.domain.repository.MovieRepository
import com.velychko.movies.util.suspendCatching
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private const val RESPONSE_TRUE = "True"
private const val ERROR_NOT_FOUND = "Movie not found!"

class MovieRepositoryImpl @Inject constructor(
    private val api: OmdbApi,
    private val dao: FavoriteDao,
) : MovieRepository {

    override suspend fun searchMovies(query: String): Result<List<Movie>> = suspendCatching {
        val response = api.search(query)
        when {
            response.response.equals(RESPONSE_TRUE, ignoreCase = true) ->
                response.search.orEmpty().map { it.toDomain() }.distinctBy { it.imdbId }

            response.error == ERROR_NOT_FOUND -> emptyList()
            else -> throw MovieApiException(response.error ?: "Unknown API error")
        }
    }

    override suspend fun getMovie(imdbId: String): Result<Movie> = suspendCatching {
        dao.getById(imdbId)?.toDomain() ?: run {
            val dto = api.getById(imdbId)
            dto.toDomainOrNull() ?: throw MovieApiException(dto.error ?: "Movie not found")
        }
    }

    override fun observeFavorites(): Flow<List<Movie>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeIsFavorite(imdbId: String): Flow<Boolean> = dao.observeIsFavorite(imdbId)

    override suspend fun toggleFavorite(movie: Movie) {
        if (dao.getById(movie.imdbId) != null) {
            dao.delete(movie.imdbId)
        } else {
            dao.upsert(movie.toEntity())
        }
    }
}
