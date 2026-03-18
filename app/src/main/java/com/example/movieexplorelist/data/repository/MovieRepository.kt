package com.example.movieexplorelist.data.repository

import android.util.Log
import com.example.movieexplorelist.data.api.RetrofitInstance
import com.example.movieexplorelist.data.db.AppDatabase
import com.example.movieexplorelist.data.db.RentalEntity
import com.example.movieexplorelist.data.model.Movie
import kotlinx.coroutines.flow.Flow

class MovieRepository(db: AppDatabase) {

    private val rentalDao = db.rentalDao()
    private val TAG = "MovieRepository"
    private companion object {
        const val MIN_RENTAL_DAYS = 1
        const val MAX_RENTAL_DAYS = 30
    }

    // ---------- API ----------
    suspend fun fetchMovies(): List<Movie> {
        return try {
            Log.d(TAG, "=== FETCHING FROM API: https://fooapi.com/api/movies ===")
            val response = RetrofitInstance.api.getMovies()
            Log.d(TAG, "✓ API Response Status: SUCCESS")
            Log.d(TAG, "✓ Response Object: $response")
            Log.d(TAG, "✓ Movies Count: ${response.movies?.size ?: 0}")
            
            val movies = response.movies.orEmpty()
            if (movies.isNotEmpty()) {
                Log.d(TAG, "✓ USING API DATA - Returning ${movies.size} movies from https://fooapi.com/api/movies")
                movies.forEach { movie ->
                    Log.d(TAG, "  - ${movie.title} (ID: ${movie.id}, Rating: ${movie.rating})")
                }
                movies
            } else {
                Log.w(TAG, "⚠ API returned empty movies list")
                Log.d(TAG, "⚠ Falling back to dummy data (${getDummyMovies().size} movies)")
                getDummyMovies()
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ API CALL FAILED: ${e.javaClass.simpleName}")
            Log.e(TAG, "✗ Error Message: ${e.message}")
            Log.e(TAG, "✗ Stack Trace: ${e.stackTraceToString()}")
            Log.w(TAG, "⚠ Falling back to dummy data (${getDummyMovies().size} movies)")
            getDummyMovies()
        }
    }

    // ---------- Room CRUD ----------
    fun getAllRentals(): Flow<List<RentalEntity>> = rentalDao.getAllRentals()

    suspend fun rentMovie(rental: RentalEntity) {
        val existing = rentalDao.getRentalByMovieId(rental.movieId)
        if (existing == null) {
            rentalDao.insertRental(rental)
        }
    }

    suspend fun increaseRentalDays(movieId: Int) {
        val rental = rentalDao.getRentalByMovieId(movieId) ?: return
        if (rental.days < MAX_RENTAL_DAYS) {
            rentalDao.updateRentalDays(movieId, rental.days + 1)
        }
    }

    suspend fun decreaseRentalDays(movieId: Int) {
        val rental = rentalDao.getRentalByMovieId(movieId) ?: return
        if (rental.days > MIN_RENTAL_DAYS) {
            rentalDao.updateRentalDays(movieId, rental.days - 1)
        }
    }

    suspend fun removeRental(movieId: Int) {
        rentalDao.deleteRentalByMovieId(movieId)
    }

    suspend fun getRentalCount(): Int = rentalDao.getRentalCount()

    // ---------- Dummy fallback data ----------
    private fun getDummyMovies(): List<Movie> = listOf(
        Movie(1, "Inception", "A thief enters dreams to steal secrets.", "https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg", 8.8f, 2.99),
        Movie(2, "Interstellar", "Explorers travel through a wormhole in space.", "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg", 8.6f, 3.49),
        Movie(3, "The Dark Knight", "Batman faces the Joker in Gotham City.", "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg", 9.0f, 3.99),
        Movie(4, "Avengers: Endgame", "The Avengers fight to undo Thanos' snap.", "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg", 8.4f, 2.99),
        Movie(5, "Dune", "A noble family becomes embroiled in Arrakis.", "https://image.tmdb.org/t/p/w500/d5NXSklpcvwro5LhA9jSC5Ejq7L.jpg", 8.0f, 3.49),
        Movie(6, "The Matrix", "A hacker discovers reality is a simulation.", "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg", 8.7f, 2.49),
        Movie(7, "Pulp Fiction", "A hit man, a boxer, and a gangster's wife face off.", "https://image.tmdb.org/t/p/w500/d5iIlW_sziJT1JulFZdwJcH94S8.jpg", 8.9f, 3.99),
        Movie(8, "Forrest Gump", "A man with low IQ witnesses historical events.", "https://image.tmdb.org/t/p/w500/arw2vcBaO1iB1h3JU6TeV43tHf5.jpg", 8.8f, 2.49),
        Movie(9, "The Shawshank Redemption", "Two imprisoned men bond over a long period of time.", "https://image.tmdb.org/t/p/w500/q6y0aKMLrFrWh6zKChF00z8d04t.jpg", 9.3f, 2.99),
        Movie(10, "The Godfather", "The aging patriarch of an organized crime dynasty transfers control.", "https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsU5A1wYcW0sI.jpg", 9.2f, 3.99),
        Movie(11, "Fight Club", "An insomniac office worker forms an underground fight club.", "https://image.tmdb.org/t/p/w500/pB8BM8dQwgNmRZQEIkhqYe5Jlho.jpg", 8.8f, 3.49),
        Movie(12, "Joker", "A struggling comedian's descent into madness and violence.", "https://image.tmdb.org/t/p/w500/udDclMgo1lA7IlEqv7MfJ6zuSsA.jpg", 8.4f, 3.99),
        Movie(13, "The Lion King", "A young lion prince is tricked into exile.", "https://image.tmdb.org/t/p/w500/sHRN5rJd3EsNGagaKa7RC0LGglJ.jpg", 8.5f, 2.99),
        Movie(14, "Titanic", "A romance blooms aboard the ill-fated maiden voyage.", "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg", 7.8f, 2.49),
        Movie(15, "Avatar", "A paraplegic Marine dispatched to the alien world of Pandora.", "https://image.tmdb.org/t/p/w500/kmnjYSZbdEHQe1sUJRjCKw2RtLX.jpg", 7.9f, 3.99),
        Movie(16, "Gladiator", "A former Roman General sets out to exact vengeance.", "https://image.tmdb.org/t/p/w500/O5mHd8gS4l0iMl8Y0fhYA2fzD8V.jpg", 8.5f, 3.49),
        Movie(17, "The Silence of the Lambs", "A young FBI cadet seeks help from an imprisoned cannibalistic killer.", "https://image.tmdb.org/t/p/w500/rplLJ2hJtvQwLaka8h3jtfdMCOb.jpg", 8.6f, 2.99),
        Movie(18, "Parasite", "A poor family schemes their way into employment with a wealthy household.", "https://image.tmdb.org/t/p/w500/sy6DvAu_bnwFMSogSZe8PdIvTUQ.jpg", 8.5f, 3.99),
        Movie(19, "Oppenheimer", "The story of American scientist J. Robert Oppenheimer.", "https://image.tmdb.org/t/p/w500/8Gxv8kScoBNw8InIWWQI4appearing.jpg", 8.1f, 3.99),
        Movie(20, "Barbie", "Barbie and Ken venture into the real world.", "https://image.tmdb.org/t/p/w500/NNZ8in7ePKDck21LzfZdnRjQD1V.jpg", 7.0f, 2.99)
    )
}