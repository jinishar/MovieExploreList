package com.example.movieexplorelist.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.movieexplorelist.data.db.AppDatabase
import com.example.movieexplorelist.data.repository.MovieRepository
import com.example.movieexplorelist.ui.screens.HomeScreen
import com.example.movieexplorelist.ui.screens.MovieDetailScreen
import com.example.movieexplorelist.ui.screens.CheckoutScreen
import com.example.movieexplorelist.ui.screens.RentalScreen
import com.example.movieexplorelist.viewmodel.MovieViewModel
import com.example.movieexplorelist.viewmodel.MovieViewModelFactory

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Rental : Screen("rental")
    object MovieDetail : Screen("movieDetail/{movieId}") {
        fun createRoute(movieId: Int) = "movieDetail/$movieId"
    }
    object Checkout : Screen("checkout")
}

@Composable
fun NavGraph(navController: NavHostController = rememberNavController()) {
    val context = LocalContext.current
    
    // Remember database and repository to avoid recreation
    val db = remember { AppDatabase.getDatabase(context) }
    val repository = remember { MovieRepository(db) }
    
    val viewModel: MovieViewModel = viewModel(
        factory = MovieViewModelFactory(repository)
    )

    NavHost(navController = navController, startDestination = Screen.Home.route) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToRentals = { navController.navigate(Screen.Rental.route) },
                onMovieClick = { id: Int ->
                    navController.navigate(Screen.MovieDetail.createRoute(id))
                }
            )
        }
        composable(
            route = Screen.MovieDetail.route,
            arguments = listOf(
                navArgument("movieId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val movieId = backStackEntry.arguments?.getInt("movieId") ?: return@composable
            val movies = (viewModel.movieState.value as? com.example.movieexplorelist.viewmodel.MovieUiState.Success)?.movies ?: emptyList()
            val movie = movies.find { it.id == movieId } ?: return@composable
            val rentals = viewModel.rentals.value
            val isRented = rentals.any { it.movieId == movieId }
            
            MovieDetailScreen(
                movie = movie,
                viewModel = viewModel,
                isRented = isRented,
                onNavigateBack = { navController.popBackStack() },
                onRentalSuccess = { navController.navigate(Screen.Rental.route) }
            )
        }
        composable(Screen.Rental.route) {
            RentalScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onCheckout = { navController.navigate(Screen.Checkout.route) }
            )
        }
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                rentals = viewModel.rentals.value,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onCheckoutSuccess = { navController.popBackStack() }
            )
        }
    }
}
