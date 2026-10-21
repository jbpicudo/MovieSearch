package com.velychko.movies.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.velychko.movies.domain.model.Movie
import com.velychko.movies.presentation.theme.MoviesTheme
import com.velychko.movies.presentation.theme.ThemePreviews

@Composable
fun MovieRow(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PosterImage(
                url = movie.posterUrl,
                contentDescription = movie.title,
                modifier = Modifier
                    .width(64.dp)
                    .height(96.dp),
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle =
                    listOfNotNull(movie.year, movie.type?.capitalized()).joinToString(" | ")
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            trailing?.invoke()
        }
    }
}

@ThemePreviews
@Composable
fun MovieRowPreview() {
    MoviesTheme {
        MovieRow(
            movie = Movie(
                imdbId = "tt1",
                title = "Love",
                year = "2000",
                type = "movie",
                posterUrl = null,
            ),
            onClick = {},
        )
    }
}

fun String.capitalized(): String = replaceFirstChar { it.uppercase() }
