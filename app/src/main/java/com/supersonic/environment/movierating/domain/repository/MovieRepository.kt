package com.supersonic.environment.movierating.domain.repository

import androidx.paging.PagingData
import com.supersonic.environment.movierating.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MovieRepository {
    fun getPopularMoviesPaged(): Flow<PagingData<Movie>>
    fun getPopularMovies(page: Int): Flow<List<Movie>>
    fun searchMovies(query: String, page: Int): Flow<List<Movie>>
    fun getMovieDetails(movieId: Int): Flow<Movie>
    
    fun getFavoriteMovies(): Flow<List<Movie>>
    fun getRecentlyViewedMovies(): Flow<List<Movie>>
    suspend fun toggleFavorite(movieId: Int)
    suspend fun isFavorite(movieId: Int): Boolean
    suspend fun markAsViewed(movieId: Int)
}
