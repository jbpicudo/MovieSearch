package com.velychko.movies.presentation

import com.velychko.movies.domain.model.Movie
import com.velychko.movies.presentation.search.ListItem
import com.velychko.movies.presentation.search.withAds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ListItemTest {

    private fun movies(count: Int) =
        (1..count).map { Movie("tt$it", "Movie $it", "2000", "movie", null) }

    @Test
    fun `no ads when there are fewer than 3 results`() {
        (0..2).forEach { count ->
            assertTrue("count=$count", movies(count).withAds().none { it is ListItem.AdItem })
        }
    }

    @Test
    fun `exactly 3 results get ads at indexes 1 and 3`() {
        val items = movies(3).withAds()

        assertEquals(5, items.size)
        assertTrue(items[1] is ListItem.AdItem)
        assertTrue(items[3] is ListItem.AdItem)
        assertEquals(listOf(0, 2, 4), items.indices.filter { items[it] is ListItem.MovieItem })
    }

    @Test
    fun `many results still get only two ads, at indexes 1 and 3`() {
        val items = movies(10).withAds()

        assertEquals(12, items.size)
        assertEquals(listOf(1, 3), items.indices.filter { items[it] is ListItem.AdItem })
    }

    @Test
    fun `ad slots have stable, distinct keys`() {
        val keys = movies(5).withAds().map { it.key }

        assertEquals(keys.size, keys.toSet().size)
    }

    @Test
    fun `original order of movies is preserved`() {
        val source = movies(6)

        val result = source.withAds().filterIsInstance<ListItem.MovieItem>().map { it.movie }

        assertEquals(source, result)
    }
}
