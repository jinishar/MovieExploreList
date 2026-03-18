package com.example.movieexplorelist.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.movieexplorelist.data.db.RentalEntity
import com.example.movieexplorelist.data.model.Movie
import com.example.movieexplorelist.data.repository.MovieRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class MovieUiState {
    object Loading : MovieUiState()
    data class Success(val movies: List<Movie>) : MovieUiState()
    data class Error(val message: String) : MovieUiState()
}

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {

    private val TAG = "MovieViewModel"
    private val _movieState = MutableStateFlow<MovieUiState>(MovieUiState.Loading)
    val movieState: StateFlow<MovieUiState> = _movieState.asStateFlow()

    val rentals: StateFlow<List<RentalEntity>> = repository.getAllRentals()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _snackbarMessage = MutableSharedFlow<String>()
    val snackbarMessage: SharedFlow<String> = _snackbarMessage.asSharedFlow()

    init {
        loadMovies()
    }

    fun loadMovies() {
        viewModelScope.launch {
            _movieState.value = MovieUiState.Loading
            Log.d(TAG, "=== LOADING MOVIES ===")
            try {
                val movies = repository.fetchMovies()
                Log.d(TAG, "✓ Movies loaded: ${movies.size} total")
                if (movies.isNotEmpty()) {
                    Log.d(TAG, "✓ Setting UI state to SUCCESS with ${movies.size} movies")
                    _movieState.value = MovieUiState.Success(movies)
                } else {
                    Log.w(TAG, "✗ No movies available - empty list received")
                    _movieState.value = MovieUiState.Error("No movies available from API")
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error loading movies: ${e.message}", e)
                _movieState.value = MovieUiState.Error(e.message ?: "Unknown Error")
            }
        }
    }

    fun rentMovie(movie: Movie) {
        viewModelScope.launch {
            val rental = RentalEntity(
                movieId = movie.id,
                title = movie.title,
                posterUrl = movie.posterUrl,
                rating = movie.rating,
                rentalPricePerDay = movie.rentalPricePerDay
            )
            repository.rentMovie(rental)
            _snackbarMessage.emit("${movie.title} added to rentals")
        }
    }

    fun increaseRentalDays(movieId: Int) {
        viewModelScope.launch {
            repository.increaseRentalDays(movieId)
        }
    }

    fun decreaseRentalDays(movieId: Int) {
        viewModelScope.launch {
            repository.decreaseRentalDays(movieId)
        }
    }

    fun removeRental(movieId: Int) {
        viewModelScope.launch {
            repository.removeRental(movieId)
            _snackbarMessage.emit("Movie removed from rentals")
        }
    }

    fun getTotalPrice(rentals: List<RentalEntity>): Double {
        return rentals.sumOf { it.rentalPricePerDay * it.days }
    }
}
