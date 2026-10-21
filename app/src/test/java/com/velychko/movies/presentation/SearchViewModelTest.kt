package com.velychko.movies.presentation

import app.cash.turbine.test
import com.velychko.movies.domain.model.Movie
import com.velychko.movies.presentation.search.SearchUiState
import com.velychko.movies.presentation.search.SearchViewModel
import com.velychko.movies.presentation.search.withAds
import com.velychko.movies.testutil.FakeMovieRepository
import com.velychko.movies.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.io.IOException

class SearchViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeMovieRepository()
    private val viewModel by lazy { SearchViewModel(repository) }
    private val movies = (1..4).map { Movie("tt$it", "Movie $it", "2000", "movie", null) }

    @Test
    fun `starts Idle`() = runTest {
        viewModel.uiState.test {
            assertEquals(SearchUiState.Idle, awaitItem())
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `blank query stays Idle and never hits the api`() = runTest {
        viewModel.uiState.test {
            assertEquals(SearchUiState.Idle, awaitItem())

            viewModel.onQueryChange("   ")
            advanceUntilIdle()

            expectNoEvents()
            assertTrue(repository.searchQueries.isEmpty())
        }
    }

    @Test
    fun `query goes Loading then Success with ads inserted`() = runTest {
        repository.searchResult = Result.success(movies)

        viewModel.uiState.test {
            assertEquals(SearchUiState.Idle, awaitItem())

            viewModel.onQueryChange("love")

            assertEquals(SearchUiState.Loading, awaitItem())
            assertEquals(SearchUiState.Success(movies.withAds()), awaitItem())
        }
    }

    @Test
    fun `rapid typing is debounced into a single request`() = runTest {
        repository.searchResult = Result.success(movies)

        viewModel.uiState.test {
            awaitItem() // Idle

            listOf("l", "lo", "lov", "love").forEach(viewModel::onQueryChange)

            assertEquals(SearchUiState.Loading, awaitItem())
            awaitItem() // Success
            assertEquals(listOf("love"), repository.searchQueries)
        }
    }

    @Test
    fun `query is trimmed before searching`() = runTest {
        viewModel.uiState.test {
            awaitItem() // Idle

            viewModel.onQueryChange("  love ")

            awaitItem() // Loading
            awaitItem() // Empty
            assertEquals(listOf("love"), repository.searchQueries)
        }
    }

    @Test
    fun `empty result becomes Empty`() = runTest {
        repository.searchResult = Result.success(emptyList())

        viewModel.uiState.test {
            awaitItem() // Idle
            viewModel.onQueryChange("aaaa")

            assertEquals(SearchUiState.Loading, awaitItem())
            assertEquals(SearchUiState.Empty, awaitItem())
        }
    }

    @Test
    fun `IOException becomes a network error and retry recovers`() = runTest {
        repository.searchResult = Result.failure(IOException("offline"))

        viewModel.uiState.test {
            awaitItem() // Idle
            viewModel.onQueryChange("love")

            assertEquals(SearchUiState.Loading, awaitItem())
            assertEquals(SearchUiState.Error(ErrorReason.NETWORK), awaitItem())

            repository.searchResult = Result.success(movies)
            viewModel.onRetry()

            assertEquals(SearchUiState.Loading, awaitItem())
            assertEquals(SearchUiState.Success(movies.withAds()), awaitItem())
        }
    }

    @Test
    fun `any other failure becomes an unknown error`() = runTest {
        repository.searchResult = Result.failure(IllegalStateException("boom"))

        viewModel.uiState.test {
            awaitItem() // Idle
            viewModel.onQueryChange("love")

            awaitItem() // Loading
            assertEquals(SearchUiState.Error(ErrorReason.UNKNOWN), awaitItem())
        }
    }
}
