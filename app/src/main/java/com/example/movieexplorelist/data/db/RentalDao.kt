package com.example.movieexplorelist.data.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RentalDao {

    // INSERT
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRental(rental: RentalEntity)

    // RETRIEVE ALL
    @Query("SELECT * FROM rentals")
    fun getAllRentals(): Flow<List<RentalEntity>>

    // RETRIEVE SINGLE by movieId
    @Query("SELECT * FROM rentals WHERE movieId = :movieId LIMIT 1")
    suspend fun getRentalByMovieId(movieId: Int): RentalEntity?

    // UPDATE rental days
    @Query("UPDATE rentals SET days = :days WHERE movieId = :movieId")
    suspend fun updateRentalDays(movieId: Int, days: Int)

    // DELETE by movieId
    @Query("DELETE FROM rentals WHERE movieId = :movieId")
    suspend fun deleteRentalByMovieId(movieId: Int)

    // DELETE ALL
    @Query("DELETE FROM rentals")
    suspend fun deleteAllRentals()

    // COUNT
    @Query("SELECT COUNT(*) FROM rentals")
    suspend fun getRentalCount(): Int
}