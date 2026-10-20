package com.velychko.movies.data

import com.velychko.movies.data.remote.dto.MovieDetailsDto
import com.velychko.movies.data.remote.dto.MovieDto
import com.velychko.movies.data.remote.dto.SearchResponseDto
import com.velychko.movies.domain.model.Movie
import com.velychko.movies.domain.model.MovieApiException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class MovieRepositoryImplTest {

    private val api = FakeOmdbApi()
    private val dao = FakeFavoriteDao()
    private val repository = MovieRepositoryImpl(api, dao)

    private val movie = Movie("tt1", "Love", "2000", "movie", "https://img/1.jpg")

    @Test
    fun `search maps dto to domain, turns N-A into null and drops duplicates`() = runTest {
        api.searchResponse = SearchResponseDto(
            response = "True",
            search = listOf(
                MovieDto("tt1", "Love", "2000", "movie", "N/A"),
                MovieDto("tt1", "Love", "2000", "movie", "N/A"),
                MovieDto("tt2", "Love 2", "2001", "series", "https://img/2.jpg"),
            ),
        )

        val result = repository.searchMovies("love").getOrThrow()

        assertEquals(
            listOf(
                Movie("tt1", "Love", "2000", "movie", null),
                Movie("tt2", "Love 2", "2001", "series", "https://img/2.jpg"),
            ),
            result,
        )
    }

    @Test
    fun `movie not found is an empty result, not an error`() = runTest {
        api.searchResponse = SearchResponseDto(response = "False", error = "Movie not found!")

        assertEquals(emptyList<Movie>(), repository.searchMovies("aaaa").getOrThrow())
    }

    @Test
    fun `other api errors become failures`() = runTest {
        api.searchResponse = SearchResponseDto(response = "False", error = "Too many results.")

        val error = repository.searchMovies("a").exceptionOrNull()

        assertTrue(error is MovieApiException)
        assertEquals("Too many results.", error?.message)
    }

    @Test
    fun `network errors become failures`() = runTest {
        api.failure = IOException("offline")

        assertTrue(repository.searchMovies("love").exceptionOrNull() is IOException)
    }

    @Test
    fun `getMovie prefers the local favorite and skips the network`() = runTest {
        repository.toggleFavorite(movie)

        assertEquals(movie, repository.getMovie("tt1").getOrThrow())
        assertEquals(0, api.detailsCalls)
    }

    @Test
    fun `getMovie falls back to the api`() = runTest {
        api.detailsResponse = MovieDetailsDto(
            response = "True",
            imdbId = "tt9",
            title = "Remote",
            year = "1999",
            type = "movie",
            poster = "N/A",
        )

        assertEquals(
            Movie("tt9", "Remote", "1999", "movie", null),
            repository.getMovie("tt9").getOrThrow()
        )
    }

    @Test
    fun `getMovie fails when the api reports an error`() = runTest {
        assertTrue(repository.getMovie("bad").exceptionOrNull() is MovieApiException)
    }

    @Test
    fun `toggling adds a favorite and toggling again removes it`() = runTest {
        repository.toggleFavorite(movie)
        assertEquals(listOf(movie), repository.observeFavorites().first())
        assertTrue(repository.observeIsFavorite("tt1").first())

        repository.toggleFavorite(movie)
        assertEquals(emptyList<Movie>(), repository.observeFavorites().first())
        assertFalse(repository.observeIsFavorite("tt1").first())
    }
}
