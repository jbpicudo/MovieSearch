package com.velychko.movies.data.remote

import kotlinx.serialization.json.Json

/** Single Json configuration shared by Retrofit and tests. */
val OmdbJson = Json { ignoreUnknownKeys = true }
