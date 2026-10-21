package com.velychko.movies.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.velychko.movies.R
import com.velychko.movies.domain.model.Movie
import com.velychko.movies.presentation.components.CenteredMessage
import com.velychko.movies.presentation.components.ErrorMessage
import com.velychko.movies.presentation.components.LoadingIndicator
import com.velychko.movies.presentation.components.MovieRow
import com.velychko.sdk.AdsSdk
import com.velychko.sdk.ui.NativeAdCard
import com.velychko.movies.presentation.theme.MoviesTheme
import com.velychko.movies.presentation.theme.ThemePreviews

/** Stateful entry point: wires the ViewModel and the ads SDK to the stateless [SearchContent]. */
@Composable
fun SearchScreen(
    onMovieClick: (Movie) -> Unit,
    onFavoritesClick: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose { AdsSdk.releaseNativeAds() }
    }

    SearchContent(
        query = query,
        state = state,
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::onRetry,
        onMovieClick = onMovieClick,
        onFavoritesClick = onFavoritesClick,
        nativeAd = { slotId -> NativeAdCard(slotId) },
    )
}

/** Stateless and free of ViewModel/SDK dependencies, so it is easy to test and preview. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    query: String,
    state: SearchUiState,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    onFavoritesClick: () -> Unit,
    nativeAd: @Composable (slotId: String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton(onClick = onFavoritesClick) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = stringResource(R.string.favorites)
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            SearchField(query, onQueryChange)
            when (state) {
                SearchUiState.Idle -> CenteredMessage(R.string.search_hint)
                SearchUiState.Loading -> LoadingIndicator()
                SearchUiState.Empty -> CenteredMessage(R.string.search_no_results)
                is SearchUiState.Error -> ErrorMessage(state.reason, onRetry)
                is SearchUiState.Success -> ResultsList(state.items, onMovieClick, nativeAd)
            }
        }
    }
}

@ThemePreviews
@Composable
fun SearchContentPreview() {
    MoviesTheme {
        SearchContent(
            query = "love",
            state = SearchUiState.Success(
                listOf(
                    ListItem.MovieItem(
                        Movie(
                            imdbId = "tt1",
                            title = "Love",
                            year = "2000",
                            type = "movie",
                            posterUrl = null,
                        ),
                    ),
                    ListItem.AdItem(slotId = "search_ad_1"),
                    ListItem.MovieItem(
                        Movie(
                            imdbId = "tt2",
                            title = "Love 2",
                            year = "2001",
                            type = "movie",
                            posterUrl = null,
                        )
                    ),
                )
            ),
            onQueryChange = {},
            onRetry = {},
            onMovieClick = {},
            onFavoritesClick = {},
            nativeAd = { slotId -> NativeAdCard(slotId) },
        )
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("search_field"),
        singleLine = true,
        placeholder = { Text(stringResource(R.string.search_placeholder)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.clear))
                }
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
    )
}

@Composable
private fun ResultsList(
    listItems: List<ListItem>,
    onMovieClick: (Movie) -> Unit,
    nativeAd: @Composable (slotId: String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.testTag("results_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Stable key + contentType: items are reused correctly and ads aren't recreated on scroll.
        items(items = listItems, key = { it.key }, contentType = { it.contentType }) { item ->
            when (item) {
                is ListItem.MovieItem -> MovieRow(
                    movie = item.movie,
                    onClick = { onMovieClick(item.movie) },
                )

                is ListItem.AdItem -> nativeAd(item.slotId)
            }
        }
    }
}
