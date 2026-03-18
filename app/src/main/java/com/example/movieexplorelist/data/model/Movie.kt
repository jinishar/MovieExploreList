package com.example.movieexplorelist.data.model

import com.google.gson.annotations.SerializedName

data class Movie(
    @SerializedName("id")
    val id: Int = 0,
    
    @SerializedName("title")
    val title: String = "Unknown Title",
    
    @SerializedName("overview")
    val overview: String = "No description available",
    
    @SerializedName("poster_url")
    val posterUrl: String = "https://via.placeholder.com/500x750?text=No+Image",
    
    @SerializedName("rating")
    val rating: Float = 0f,
    
    @SerializedName("rental_price_per_day")
    val rentalPricePerDay: Double = 2.99
)

data class MovieResponse(
    @SerializedName("movies")
    val movies: List<Movie>? = emptyList()
)
