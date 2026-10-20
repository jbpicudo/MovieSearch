package com.velychko.movies.domain.model

/** The API answered, but reported an error (e.g. "Too many results."). */
class MovieApiException(message: String) : Exception(message)
