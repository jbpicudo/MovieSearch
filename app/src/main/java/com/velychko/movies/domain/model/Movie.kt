package com.velychko.movies.domain.model

data class Movie(
    val imdbId: String,
    val title: String,
    val year: String?,
    val type: String?,
    val posterUrl: String?,
)
