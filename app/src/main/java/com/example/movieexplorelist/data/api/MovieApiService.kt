package com.example.movieexplorelist.data.api

import com.example.movieexplorelist.data.model.MovieResponse
import retrofit2.http.GET

interface MovieApiService {
    @GET("movies")
    suspend fun getMovies(): MovieResponse
}
