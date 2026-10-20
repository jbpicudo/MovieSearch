package com.velychko.movies.data

import com.velychko.movies.data.remote.OmdbApi
import com.velychko.movies.data.remote.dto.MovieDetailsDto
import com.velychko.movies.data.remote.dto.SearchResponseDto

class FakeOmdbApi : OmdbApi {
    var searchResponse = SearchResponseDto(response = "True", search = emptyList())
    var detailsResponse = MovieDetailsDto(response = "False", error = "Incorrect IMDb ID.")
    var failure: Throwable? = null
    var detailsCalls = 0

    override suspend fun search(query: String): SearchResponseDto {
        failure?.let { throw it }
        return searchResponse
    }

    override suspend fun getById(imdbId: String): MovieDetailsDto {
        detailsCalls++
        failure?.let { throw it }
        return detailsResponse
    }
}
