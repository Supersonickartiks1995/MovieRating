package com.supersonic.environment.movierating.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.supersonic.environment.movierating.data.local.MovieDatabase
import com.supersonic.environment.movierating.data.local.entity.MovieEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MovieDaoTest {

    private lateinit var database: MovieDatabase
    private lateinit var dao: MovieDao

    private val movie1 = MovieEntity(
        id = 1,
        title = "Movie 1",
        overview = "Overview 1",
        posterUrl = null,
        backdropUrl = null,
        releaseDate = null,
        rating = 8.0,
        voteCount = 100,
        isCached = true
    )

    private val movie2 = MovieEntity(
        id = 2,
        title = "Movie 2",
        overview = "Overview 2",
        posterUrl = null,
        backdropUrl = null,
        releaseDate = null,
        rating = 7.0,
        voteCount = 50,
        isFavorite = true
    )

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            MovieDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = database.movieDao
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun insertAndGetMovieById() = runTest {
        dao.insertMovie(movie1)
        val result = dao.getMovieById(1)
        assertThat(result).isEqualTo(movie1)
    }

    @Test
    fun getCachedMoviesFlow() = runTest {
        dao.insertMovies(listOf(movie1, movie2))
        val cached = dao.getCachedMovies().first()
        assertThat(cached).hasSize(1)
        assertThat(cached[0].id).isEqualTo(1)
    }

    @Test
    fun getFavoriteMoviesFlow() = runTest {
        dao.insertMovies(listOf(movie1, movie2))
        val favorites = dao.getFavoriteMovies().first()
        assertThat(favorites).hasSize(1)
        assertThat(favorites[0].id).isEqualTo(2)
    }

    @Test
    fun updateFavoriteStatus() = runTest {
        dao.insertMovie(movie1)
        dao.updateFavoriteStatus(1, true)
        val result = dao.getMovieById(1)
        assertThat(result?.isFavorite).isTrue()
    }

    @Test
    fun updateLastViewedTimestampAndGetRecentlyViewed() = runTest {
        dao.insertMovie(movie1)
        val timestamp = 123456789L
        dao.updateLastViewedTimestamp(1, timestamp)
        
        val recentlyViewed = dao.getRecentlyViewedMovies().first()
        assertThat(recentlyViewed).hasSize(1)
        assertThat(recentlyViewed[0].lastViewedAt).isEqualTo(timestamp)
    }

    @Test
    fun clearCachedMovies() = runTest {
        dao.insertMovies(listOf(movie1, movie2))
        dao.clearCachedMovies()
        
        val cached = dao.getCachedMovies().first()
        assertThat(cached).isEmpty()
        
        // Favorite should remain
        val result2 = dao.getMovieById(2)
        assertThat(result2).isNotNull()
    }
}
