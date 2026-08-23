package com.supersonic.environment.movierating.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.supersonic.environment.movierating.data.local.dao.MovieDao
import com.supersonic.environment.movierating.data.local.entity.toDomain
import com.supersonic.environment.movierating.data.local.entity.toEntity
import com.supersonic.environment.movierating.data.remote.TmdbApi
import com.supersonic.environment.movierating.data.remote.model.MovieDto
import com.supersonic.environment.movierating.data.remote.paging.MoviePagingSource
import com.supersonic.environment.movierating.domain.model.Movie
import com.supersonic.environment.movierating.domain.repository.MovieRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MovieRepositoryImpl @Inject constructor(
    private val api: TmdbApi,
    private val movieDao: MovieDao
) : MovieRepository {

    private val apiKey = "3543275f1c505df946c3f2bd43dff997"

    override fun getPopularMoviesPaged(): Flow<PagingData<Movie>> {
        return Pager(
            config = PagingConfig(pageSize = 20),
            pagingSourceFactory = { MoviePagingSource(api, apiKey) }
        ).flow
    }

    override fun getPopularMovies(page: Int): Flow<List<Movie>> = flow {
        // Emit cached movies first
        val cachedMovies = movieDao.getCachedMovies().map { entities -> 
            entities.map { it.toDomain() } 
        }
        
        try {
            val response = api.getPopularMovies(apiKey, page)
            val movies = response.results.map { it.toDomain() }
            
            // Update cache
            movieDao.clearCachedMovies()
            movieDao.insertMovies(movies.map { it.toEntity(isCached = true) })
            
            emit(movies)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            emitAll(cachedMovies)
        }
    }

    override fun searchMovies(query: String, page: Int): Flow<List<Movie>> = flow {
        val response = api.searchMovies(apiKey, query, page)
        emit(response.results.map { it.toDomain() })
    }

    override fun getMovieDetails(movieId: Int): Flow<Movie> = flow {
        try {
            val response = api.getMovieDetails(movieId, apiKey)
            val movie = response.toDomain()
            movieDao.insertMovie(movie.toEntity(isCached = false))
            emit(movie)
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            val cached = movieDao.getMovieById(movieId)
            if (cached != null) {
                emit(cached.toDomain())
            } else {
                throw e
            }
        }
    }

    override fun getFavoriteMovies(): Flow<List<Movie>> {
        return movieDao.getFavoriteMovies().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getRecentlyViewedMovies(): Flow<List<Movie>> {
        return movieDao.getRecentlyViewedMovies().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun toggleFavorite(movieId: Int) {
        val movie = movieDao.getMovieById(movieId)
        if (movie != null) {
            movieDao.updateFavoriteStatus(movieId, !movie.isFavorite)
        }
    }

    override suspend fun isFavorite(movieId: Int): Boolean {
        return movieDao.getMovieById(movieId)?.isFavorite ?: false
    }

    override suspend fun markAsViewed(movieId: Int) {
        movieDao.updateLastViewedTimestamp(movieId, System.currentTimeMillis())
    }

    private fun MovieDto.toDomain(): Movie {
        return Movie(
            id = id,
            title = title,
            overview = overview,
            posterUrl = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" },
            backdropUrl = backdropPath?.let { "https://image.tmdb.org/t/p/w1280$it" },
            releaseDate = releaseDate,
            rating = voteAverage,
            voteCount = voteCount
        )
    }
}
