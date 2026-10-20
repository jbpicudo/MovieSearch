package com.velychko.movies.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `?s=` response. On failure `Search` is absent and `Error` is set. */
@Serializable
data class SearchResponseDto(
    @SerialName("Response") val response: String,
    @SerialName("Search") val search: List<MovieDto>? = null,
    @SerialName("Error") val error: String? = null,
)

@Serializable
data class MovieDto(
    @SerialName("imdbID") val imdbId: String,
    @SerialName("Title") val title: String,
    @SerialName("Year") val year: String? = null,
    @SerialName("Type") val type: String? = null,
    @SerialName("Poster") val poster: String? = null,
)

/** `?i=` response. Lenient, because a failed lookup contains only `Response` and `Error`. */
@Serializable
data class MovieDetailsDto(
    @SerialName("Response") val response: String,
    @SerialName("Error") val error: String? = null,
    @SerialName("imdbID") val imdbId: String? = null,
    @SerialName("Title") val title: String? = null,
    @SerialName("Year") val year: String? = null,
    @SerialName("Type") val type: String? = null,
    @SerialName("Poster") val poster: String? = null,
)
