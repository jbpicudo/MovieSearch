package com.velychko.movies.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.velychko.movies.presentation.details.DetailsScreen
import com.velychko.movies.presentation.favorites.FavoritesScreen
import com.velychko.movies.presentation.search.SearchScreen

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = SearchRoute) {

        composable<SearchRoute> {
            SearchScreen(
                onMovieClick = { movie -> navController.navigate(DetailsRoute(movie.imdbId)) },
                onFavoritesClick = { navController.navigate(FavoritesRoute) },
            )
        }

        composable<DetailsRoute> {
            DetailsScreen(onBack = { navController.popBackStack() })
        }

        composable<FavoritesRoute> {
            FavoritesScreen(
                onBack = { navController.popBackStack() },
                onMovieClick = { movie -> navController.navigate(DetailsRoute(movie.imdbId)) },
            )
        }
    }
}
