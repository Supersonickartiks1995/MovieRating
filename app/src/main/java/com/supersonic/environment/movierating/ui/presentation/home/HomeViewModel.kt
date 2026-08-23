package com.supersonic.environment.movierating.ui.presentation.home

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.supersonic.environment.movierating.domain.model.Movie
import com.supersonic.environment.movierating.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val favoriteMovies: List<Movie> = emptyList(),
    val recentlyViewedMovies: List<Movie> = emptyList(),
    val searchResults: List<Movie> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val error: String? = null
)

sealed interface HomeEvent {
    data object Refresh : HomeEvent
    data class OnMovieClick(val movie: Movie) : HomeEvent
    data class OnSearchQueryChange(val query: String) : HomeEvent
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val popularMoviesPaged: Flow<PagingData<Movie>> = movieRepository.getPopularMoviesPaged()
        .cachedIn(viewModelScope)

    private var searchJob: Job? = null

    init {
        getFavoriteMovies()
        getRecentlyViewedMovies()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> {
                getFavoriteMovies()
                getRecentlyViewedMovies()
            }
            is HomeEvent.OnMovieClick -> {
                // Handle navigation or other actions
            }
            is HomeEvent.OnSearchQueryChange -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(500)
                    searchMovies(event.query)
                }
            }
        }
    }

    private suspend fun searchMovies(query: String) {
        if (query.isBlank()) {
            _uiState.update { it.copy(searchResults = emptyList(), isSearching = false) }
            return
        }

        movieRepository.searchMovies(query, 1)
            .onStart { _uiState.update { it.copy(isSearching = true, error = null) } }
            .catch { e ->
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        error = e.message ?: "An unknown error occurred"
                    )
                }
            }
            .collect { movies ->
                _uiState.update {
                    it.copy(
                        isSearching = false,
                        searchResults = movies
                    )
                }
            }
    }

    private fun getFavoriteMovies() {
        viewModelScope.launch {
            movieRepository.getFavoriteMovies()
                .collect { movies ->
                    _uiState.update {
                        it.copy(favoriteMovies = movies)
                    }
                }
        }
    }

    private fun getRecentlyViewedMovies() {
        viewModelScope.launch {
            movieRepository.getRecentlyViewedMovies()
                .collect { movies ->
                    _uiState.update {
                        it.copy(recentlyViewedMovies = movies)
                    }
                }
        }
    }
}
