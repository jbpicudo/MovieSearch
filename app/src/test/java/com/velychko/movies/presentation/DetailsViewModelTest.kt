package com.velychko.movies.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.velychko.movies.domain.model.Movie
import com.velychko.movies.presentation.details.DetailsUiState
import com.velychko.movies.presentation.details.DetailsViewModel
import com.velychko.movies.presentation.navigation.DETAILS_ARG_IMDB_ID
import com.velychko.movies.testutil.FakeMovieRepository
import com.velychko.movies.testutil.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class DetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMovieRepository()
    private val movie = Movie("tt1", "Love Actually", "2003", "movie", null)

    @Test
    fun `loads the movie and toggling the favorite flips the heart both ways`() = runTest {
        repository.movieResult = Result.success(movie)
        val viewModel = DetailsViewModel(
            savedStateHandle = SavedStateHandle(mapOf(DETAILS_ARG_IMDB_ID to "tt1")),
            repository = repository,
        )

        viewModel.uiState.test {
            assertEquals(DetailsUiState.Loading, awaitItem())
            assertEquals(DetailsUiState.Success(movie, isFavorite = false), awaitItem())

            viewModel.onToggleFavorite()
            assertEquals(DetailsUiState.Success(movie, isFavorite = true), awaitItem())

            viewModel.onToggleFavorite()
            assertEquals(DetailsUiState.Success(movie, isFavorite = false), awaitItem())
        }
    }
}
