package com.velychko.movies.presentation.search

import com.velychko.movies.domain.model.Movie

/** What the results list actually renders: movies interleaved with ad slots. */
sealed interface ListItem {
    val key: String
    val contentType: String

    data class MovieItem(val movie: Movie) : ListItem {
        override val key: String get() = "movie_${movie.imdbId}"
        override val contentType: String get() = "movie"
    }

    data class AdItem(val slotId: String) : ListItem {
        override val key: String get() = "ad_$slotId"
        override val contentType: String get() = "native_ad"
    }
}

private const val MIN_RESULTS_FOR_ADS = 3
private val AD_POSITIONS = listOf(1, 3)

/** Native ads go to list indexes 1 and 3, but only if there are at least 3 results. */
fun List<Movie>.withAds(): List<ListItem> {
    val items = map<Movie, ListItem> { ListItem.MovieItem(it) }.toMutableList()
    if (size < MIN_RESULTS_FOR_ADS) return items
    AD_POSITIONS.forEach { position ->
        items.add(position, ListItem.AdItem(slotId = "search_ad_$position"))
    }
    return items
}
