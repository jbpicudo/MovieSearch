package com.velychko.movies.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE imdbId = :imdbId)")
    fun observeIsFavorite(imdbId: String): Flow<Boolean>

    @Query("SELECT * FROM favorites WHERE imdbId = :imdbId")
    suspend fun getById(imdbId: String): FavoriteEntity?

    @Upsert
    suspend fun upsert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE imdbId = :imdbId")
    suspend fun delete(imdbId: String)
}
