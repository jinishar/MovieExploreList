package com.example.movieexplorelist

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.movieexplorelist.ui.navigation.NavGraph
import com.example.movieexplorelist.ui.theme.MovieExploreListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MovieExploreListTheme {
                NavGraph()
            }
        }
    }
}