package com.example.movieexplorelist.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rentals")
data class RentalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val movieId: Int,
    val title: String,
    val posterUrl: String,
    val rating: Float,
    val days: Int = 1,
    val rentalPricePerDay: Double = 2.99
)
