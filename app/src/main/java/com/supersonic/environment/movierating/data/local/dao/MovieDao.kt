package com.supersonic.environment.movierating.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.supersonic.environment.movierating.data.local.entity.MovieEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies WHERE isCached = 1")
    fun getCachedMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE isFavorite = 1")
    fun getFavoriteMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE lastViewedAt IS NOT NULL ORDER BY lastViewedAt DESC LIMIT 10")
    fun getRecentlyViewedMovies(): Flow<List<MovieEntity>>

    @Query("SELECT * FROM movies WHERE id = :movieId")
    suspend fun getMovieById(movieId: Int): MovieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: MovieEntity)

    @Query("UPDATE movies SET isFavorite = :isFavorite WHERE id = :movieId")
    suspend fun updateFavoriteStatus(movieId: Int, isFavorite: Boolean)

    @Query("UPDATE movies SET lastViewedAt = :timestamp WHERE id = :movieId")
    suspend fun updateLastViewedTimestamp(movieId: Int, timestamp: Long)
    
    @Query("DELETE FROM movies WHERE isCached = 1 AND isFavorite = 0 AND lastViewedAt IS NULL")
    suspend fun clearCachedMovies()
}
