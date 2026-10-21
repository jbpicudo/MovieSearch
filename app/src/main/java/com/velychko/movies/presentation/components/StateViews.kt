package com.velychko.movies.presentation.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.velychko.movies.R
import com.velychko.movies.presentation.ErrorReason
import com.velychko.movies.presentation.theme.MoviesTheme
import com.velychko.movies.presentation.theme.ThemePreviews

@Composable
fun CenteredMessage(@StringRes message: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@ThemePreviews
@Composable
fun CenteredMessagePreview() {
    MoviesTheme {
        CenteredMessage(R.string.search_no_results)
    }
}

@ThemePreviews
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    MoviesTheme {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun ErrorMessage(reason: ErrorReason, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val message = when (reason) {
        ErrorReason.NETWORK -> R.string.error_network
        ErrorReason.UNKNOWN -> R.string.error_unknown
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(message),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@ThemePreviews
@Composable
fun ErrorMessagePreview() {
    MoviesTheme {
        ErrorMessage(
            reason = ErrorReason.NETWORK,
            onRetry = {},
        )
    }
}
