package com.velychko.movies.presentation.favorites

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.velychko.movies.R
import com.velychko.movies.domain.model.Movie
import com.velychko.movies.presentation.components.CenteredMessage
import com.velychko.movies.presentation.components.LoadingIndicator
import com.velychko.movies.presentation.components.MovieRow
import com.velychko.movies.presentation.theme.MoviesTheme
import com.velychko.movies.presentation.theme.ThemePreviews

@Composable
fun FavoritesScreen(
    onBack: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    viewModel: FavoritesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FavoritesContent(state, onBack, onMovieClick, viewModel::onRemove)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesContent(
    state: FavoritesUiState,
    onBack: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    onRemove: (Movie) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.favorites)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when {
            state.isLoading -> LoadingIndicator(contentModifier)
            state.movies.isEmpty() -> CenteredMessage(R.string.favorites_empty, contentModifier)
            else -> LazyColumn(
                modifier = contentModifier,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items = state.movies, key = { it.imdbId }) { movie ->
                    MovieRow(
                        movie = movie,
                        onClick = { onMovieClick(movie) },
                        trailing = {
                            IconButton(onClick = { onRemove(movie) }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = stringResource(R.string.remove_favorite),
                                )
                            }
                        },
                    )
                }
            }
        }
    }
}

@ThemePreviews
@Composable
fun FavoritesContentPreview() {
    MoviesTheme {
        FavoritesContent(
            state = FavoritesUiState(
                movies = listOf(
                    Movie(
                        imdbId = "tt1",
                        title = "Love",
                        year = "2000",
                        type = "movie",
                        posterUrl = null,
                    ),
                    Movie(
                        imdbId = "tt2",
                        title = "Love 2",
                        year = "2001",
                        type = "movie",
                        posterUrl = null,
                    ),
                ),
                isLoading = false,
            ),
            onBack = {},
            onMovieClick = {},
            onRemove = {},
        )
    }
}
