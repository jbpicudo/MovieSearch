package com.velychko.movies.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val imdbId: String,
    val title: String,
    val year: String?,
    val type: String?,
    val posterUrl: String?,
    val addedAt: Long,
)
