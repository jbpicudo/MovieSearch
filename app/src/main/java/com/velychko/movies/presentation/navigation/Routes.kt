package com.velychko.movies.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object SearchRoute

@Serializable
data object FavoritesRoute

@Serializable
data class DetailsRoute(val imdbId: String)

/**
 * Key under which Navigation stores [DetailsRoute.imdbId] in the destination's SavedStateHandle.
 * Type-safe navigation uses the property name, so keep both in sync.
 */
const val DETAILS_ARG_IMDB_ID = "imdbId"
