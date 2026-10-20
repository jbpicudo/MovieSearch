package com.velychko.movies.data

import com.velychko.movies.data.local.FavoriteEntity
import com.velychko.movies.data.remote.dto.MovieDetailsDto
import com.velychko.movies.data.remote.dto.MovieDto
import com.velychko.movies.domain.model.Movie

private const val NOT_AVAILABLE = "N/A"

/** OMDb uses the literal string "N/A" for missing values. */
private fun String?.orNull(): String? = this?.takeIf { it.isNotBlank() && it != NOT_AVAILABLE }

fun MovieDto.toDomain() = Movie(
    imdbId = imdbId,
    title = title,
    year = year.orNull(),
    type = type.orNull(),
    posterUrl = poster.orNull(),
)

fun MovieDetailsDto.toDomainOrNull(): Movie? {
    val id = imdbId ?: return null
    val name = title ?: return null
    return Movie(id, name, year.orNull(), type.orNull(), poster.orNull())
}

fun FavoriteEntity.toDomain() = Movie(imdbId, title, year, type, posterUrl)

fun Movie.toEntity(addedAt: Long = System.currentTimeMillis()) =
    FavoriteEntity(imdbId, title, year, type, posterUrl, addedAt)
