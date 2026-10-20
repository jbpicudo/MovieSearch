package com.velychko.movies.data.remote

import com.velychko.movies.data.remote.dto.MovieDetailsDto
import com.velychko.movies.data.remote.dto.SearchResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

/** The `apikey` query parameter is added by [ApiKeyInterceptor]. */
interface OmdbApi {

    /** `GET /?s=<keyword>` - search titles by keyword. */
    @GET("/")
    suspend fun search(@Query("s") query: String): SearchResponseDto

    /** `GET /?i=<imdbId>` - full record of a single title. */
    @GET("/")
    suspend fun getById(@Query("i") imdbId: String): MovieDetailsDto
}
