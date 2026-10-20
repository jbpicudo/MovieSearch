package com.velychko.movies.data

import com.velychko.movies.data.local.FavoriteDao
import com.velychko.movies.data.local.FavoriteEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeFavoriteDao : FavoriteDao {
    private val items = MutableStateFlow<List<FavoriteEntity>>(emptyList())

    override fun observeAll(): Flow<List<FavoriteEntity>> =
        items.map { list -> list.sortedByDescending { it.addedAt } }

    override fun observeIsFavorite(imdbId: String): Flow<Boolean> =
        items.map { list -> list.any { it.imdbId == imdbId } }

    override suspend fun getById(imdbId: String): FavoriteEntity? =
        items.value.firstOrNull { it.imdbId == imdbId }

    override suspend fun upsert(entity: FavoriteEntity) {
        items.update { list -> list.filterNot { it.imdbId == entity.imdbId } + entity }
    }

    override suspend fun delete(imdbId: String) {
        items.update { list -> list.filterNot { it.imdbId == imdbId } }
    }
}
