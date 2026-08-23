package com.supersonic.environment.movierating.ui.presentation.detail

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.supersonic.environment.movierating.domain.model.Movie
import com.supersonic.environment.movierating.domain.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class DetailUiState(
    val isLoading: Boolean = false,
    val movie: Movie? = null,
    val isFavorite: Boolean = false,
    val error: String? = null
)

sealed interface DetailEvent {
    data object Refresh : DetailEvent
    data object ToggleFavorite : DetailEvent
}

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val movieRepository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId: Int = checkNotNull(savedStateHandle["movieId"])
    
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    init {
        getMovieDetails()
        checkFavoriteStatus()
        markAsViewed()
    }

    fun onEvent(event: DetailEvent) {
        when (event) {
            DetailEvent.Refresh -> getMovieDetails()
            DetailEvent.ToggleFavorite -> toggleFavorite()
        }
    }

    private fun markAsViewed() {
        viewModelScope.launch {
            movieRepository.markAsViewed(movieId)
        }
    }

    private fun checkFavoriteStatus() {
        viewModelScope.launch {
            val isFavorite = movieRepository.isFavorite(movieId)
            _uiState.update { it.copy(isFavorite = isFavorite) }
        }
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            movieRepository.toggleFavorite(movieId)
            checkFavoriteStatus()
        }
    }

    private fun getMovieDetails() {
        viewModelScope.launch {
            movieRepository.getMovieDetails(movieId)
                .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "An unknown error occurred"
                        )
                    }
                }
                .collect { movie ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            movie = movie
                        )
                    }
                }
        }
    }
}
