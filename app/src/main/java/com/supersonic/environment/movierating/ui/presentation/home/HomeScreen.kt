package com.supersonic.environment.movierating.ui.presentation.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil3.compose.AsyncImage
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.ui.graphics.RectangleShape
import com.supersonic.environment.movierating.ui.presentation.home.components.MovieRow
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.IconButton
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToDetail: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val popularMovies = viewModel.popularMoviesPaged.collectAsLazyPagingItems()

    val onEvent = remember(viewModel, onNavigateToDetail) {
        { event: HomeEvent ->
            if (event is HomeEvent.OnMovieClick) {
                onNavigateToDetail(event.movie.id)
            } else {
                viewModel.onEvent(event)
            }
        }
    }

    HomeScreenContent(
        uiState = uiState,
        popularMovies = popularMovies,
        onEvent = onEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    popularMovies: androidx.paging.compose.LazyPagingItems<com.supersonic.environment.movierating.domain.model.Movie>,
    onEvent: (HomeEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    var active by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(text = "Movie Rating") }
                )
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { onEvent(HomeEvent.OnSearchQueryChange(it)) },
                    onSearch = { active = false },
                    active = active,
                    onActiveChange = { active = it },
                    placeholder = { Text("Search movies...") },
                    leadingIcon = { 
                        if (active) {
                            IconButton(onClick = { active = false }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        } else {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onEvent(HomeEvent.OnSearchQueryChange("")) }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    colors = SearchBarDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = if (active) 0.dp else 16.dp)
                        .clip(if (active) RectangleShape else MaterialTheme.shapes.extraLarge)
                ) {
                    if (uiState.isSearching) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(uiState.searchResults) { movie ->
                                val onSearchResultClick = remember(onEvent, movie) {
                                    {
                                        active = false
                                        onEvent(HomeEvent.OnMovieClick(movie))
                                    }
                                }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = onSearchResultClick)
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = movie.posterUrl,
                                        contentDescription = movie.title,
                                        modifier = Modifier
                                            .size(50.dp)
                                            .padding(end = 16.dp),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                    Text(
                                        text = movie.title,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp)
                ) {
                    val onMovieClick = remember(onEvent) {
                        { movie: com.supersonic.environment.movierating.domain.model.Movie -> 
                            onEvent(HomeEvent.OnMovieClick(movie)) 
                        }
                    }

                    MovieRow(
                        title = "Popular Movies",
                        movies = popularMovies,
                        onMovieClick = onMovieClick
                    )
                    
                    if (uiState.favoriteMovies.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        MovieRow(
                            title = "Favorite Movies",
                            movies = uiState.favoriteMovies,
                            onMovieClick = onMovieClick
                        )
                    }

                    if (uiState.recentlyViewedMovies.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(24.dp))
                        MovieRow(
                            title = "Recently Viewed",
                            movies = uiState.recentlyViewedMovies,
                            onMovieClick = onMovieClick
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
