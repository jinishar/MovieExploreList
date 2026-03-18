package com.example.movieexplorelist.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.movieexplorelist.data.model.Movie
import com.example.movieexplorelist.ui.theme.CinemaRed
import com.example.movieexplorelist.ui.theme.GoldStar
import com.example.movieexplorelist.ui.utils.CurrencyFormatter
import com.example.movieexplorelist.viewmodel.MovieUiState
import com.example.movieexplorelist.viewmodel.MovieViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MovieViewModel,
    onNavigateToRentals: () -> Unit,
    onMovieClick: (Int) -> Unit
) {
    val movieState by viewModel.movieState.collectAsState()
    val rentals by viewModel.rentals.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    // Search and filter states
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var minRating by rememberSaveable { mutableStateOf(0f) }
    var maxPrice by rememberSaveable { mutableStateOf(500.0) }
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }

    // Collect snackbar messages
    LaunchedEffect(Unit) {
        viewModel.snackbarMessage.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    val rentedMovieIds = rentals.map { it.movieId }.toSet()
    
    // Filter and search movies
    val filteredMovies = if (movieState is MovieUiState.Success) {
        val allMovies = (movieState as MovieUiState.Success).movies
        allMovies.filter { movie ->
            val matchesSearch = searchQuery.isEmpty() || 
                movie.title.contains(searchQuery, ignoreCase = true)
            val matchesRating = movie.rating >= minRating
            val matchesPrice = (movie.rentalPricePerDay * 83.0) <= maxPrice
            matchesSearch && matchesRating && matchesPrice
        }
    } else {
        emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "🎬 Movie Explorer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (rentals.isNotEmpty()) {
                                Badge { Text(rentals.size.toString()) }
                            }
                        }
                    ) {
                        IconButton(onClick = onNavigateToRentals) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Rentals",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { showFilterDialog = true }) {
                        Icon(Icons.Default.Tune, contentDescription = "Filter")
                    }
                    IconButton(onClick = { viewModel.loadMovies() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search bar (enhanced look)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .shadow(6.dp, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search movies, e.g. Interstellar") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Search",
                            tint = CinemaRed
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            // Quick filters (horizontally scrollable)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = minRating >= 8f,
                    onClick = { minRating = if (minRating >= 8f) 0f else 8f },
                    label = { Text("Top Rated") },
                    leadingIcon = {
                        Icon(Icons.Default.Star, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                FilterChip(
                    selected = maxPrice <= 250.0,
                    onClick = { maxPrice = if (maxPrice <= 250.0) 500.0 else 250.0 },
                    label = { Text("Under ₹250") },
                    leadingIcon = {
                        Icon(Icons.Default.LocalOffer, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                if (searchQuery.isNotEmpty() || minRating > 0f || maxPrice < 500.0) {
                    AssistChip(
                        onClick = {
                            searchQuery = ""
                            minRating = 0f
                            maxPrice = 500.0
                        },
                        label = { Text("Reset") },
                        leadingIcon = {
                            Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                        }
                    )
                }
            }
            
            // Filter info
            if (searchQuery.isNotEmpty() || minRating > 0f || maxPrice < 500.0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Found: ${filteredMovies.size} movies",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (minRating > 0f || maxPrice < 500.0) {
                        TextButton(
                            onClick = {
                                minRating = 0f
                                maxPrice = 500.0
                            },
                            modifier = Modifier.height(24.dp)
                        ) {
                            Text("Reset filters", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Movies grid
            when (val state = movieState) {
                is MovieUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = CinemaRed)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Fetching movies...", color = MaterialTheme.colorScheme.onBackground)
                        }
                    }
                }

                is MovieUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CinemaRed, modifier = Modifier.size(48.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Error: ${state.message}", color = CinemaRed)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.loadMovies() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is MovieUiState.Success -> {
                    if (filteredMovies.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.MovieFilter, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("No movies found", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                                Text("Try adjusting your search or filters", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            items(filteredMovies) { movie ->
                                MovieCard(
                                    movie = movie,
                                    isRented = movie.id in rentedMovieIds,
                                    onMovieClick = { onMovieClick(movie.id) },
                                    onRent = {
                                        scope.launch { viewModel.rentMovie(movie) }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Filter dialog
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Filter Movies") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Rating filter
                    Text("Minimum Rating: ${String.format("%.1f", minRating)}", fontSize = 12.sp)
                    Slider(
                        value = minRating,
                        onValueChange = { minRating = it },
                        valueRange = 0f..10f,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Price filter
                    Text("Maximum Price: ${CurrencyFormatter.formatPriceINR(maxPrice / 83.0)}", fontSize = 12.sp)
                    Slider(
                        value = maxPrice.toFloat(),
                        onValueChange = { maxPrice = it.toDouble() },
                        valueRange = 0f..500f,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showFilterDialog = false }) {
                    Text("Apply")
                }
            }
        )
    }
}

@Composable
fun MovieCard(
    movie: Movie,
    isRented: Boolean,
    onMovieClick: () -> Unit,
    onRent: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clickable { onMovieClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Poster Image
            AsyncImage(
                model = movie.posterUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                            startY = 100f
                        )
                    )
            )

            // Rating badge
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp),
                shape = RoundedCornerShape(8.dp),
                color = Color.Black.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = GoldStar,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = String.format("%.1f", movie.rating),
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom info
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            ) {
                Text(
                    text = movie.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = CurrencyFormatter.formatPriceINR(movie.rentalPricePerDay) + "/day",
                    color = GoldStar,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Button(
                    onClick = onRent,
                    enabled = !isRented,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRented) Color.Gray else CinemaRed,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = if (isRented) Icons.Default.Check else Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isRented) "Rented" else "Rent",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
