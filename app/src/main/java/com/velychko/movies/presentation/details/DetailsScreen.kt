package com.velychko.movies.presentation.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.velychko.movies.R
import com.velychko.movies.domain.model.Movie
import com.velychko.movies.presentation.components.ErrorMessage
import com.velychko.movies.presentation.components.LoadingIndicator
import com.velychko.movies.presentation.components.PosterImage
import com.velychko.movies.presentation.components.capitalized
import com.velychko.movies.presentation.theme.MoviesTheme
import com.velychko.movies.presentation.theme.ThemePreviews

@Composable
fun DetailsScreen(
    onBack: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    DetailsContent(
        state = state,
        onBack = onBack,
        onToggleFavorite = viewModel::onToggleFavorite,
        onRetry = viewModel::load,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsContent(
    state: DetailsUiState,
    onBack: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (state is DetailsUiState.Success) {
                        FavoriteAction(state.isFavorite, onToggleFavorite)
                    }
                },
            )
        },
    ) { innerPadding ->
        val contentModifier = Modifier.padding(innerPadding)
        when (state) {
            DetailsUiState.Loading -> LoadingIndicator(contentModifier)
            is DetailsUiState.Error -> ErrorMessage(state.reason, onRetry, contentModifier)
            is DetailsUiState.Success -> MovieDetails(state.movie, contentModifier)
        }
    }
}

@ThemePreviews
@Composable
fun DetailsContentPreview() {
    MoviesTheme {
        DetailsContent(
            state = DetailsUiState.Success(
                Movie(
                    imdbId = "tt1",
                    title = "Love",
                    year = "2000",
                    type = "movie",
                    posterUrl = null,
                ),
                isFavorite = true
            ),
            onBack = {},
            onToggleFavorite = {},
            onRetry = {},
        )
    }
}

@Composable
private fun FavoriteAction(isFavorite: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = stringResource(
                if (isFavorite) R.string.remove_favorite else R.string.add_favorite,
            ),
            tint = if (isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun MovieDetails(movie: Movie, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PosterImage(
            url = movie.posterUrl,
            contentDescription = movie.title,
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .aspectRatio(2f / 3f),
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = movie.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        movie.type?.let {
            Text(
                stringResource(R.string.details_type, it.capitalized()),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        movie.year?.let {
            Text(
                text = stringResource(R.string.details_year, it),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
