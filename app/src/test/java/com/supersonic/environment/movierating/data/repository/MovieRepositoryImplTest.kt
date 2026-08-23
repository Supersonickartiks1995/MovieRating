package com.supersonic.environment.movierating.data.repository

import com.google.common.truth.Truth.assertThat
import com.supersonic.environment.movierating.data.local.dao.MovieDao
import com.supersonic.environment.movierating.data.local.entity.toEntity
import com.supersonic.environment.movierating.data.remote.TmdbApi
import com.supersonic.environment.movierating.data.remote.model.MovieDto
import com.supersonic.environment.movierating.data.remote.model.MovieResponse
import com.supersonic.environment.movierating.domain.model.Movie
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class MovieRepositoryImplTest {

    private lateinit var repository: MovieRepositoryImpl
    private val api: TmdbApi = mockk()
    private val dao: MovieDao = mockk(relaxed = true)

    private val testMovieDto = MovieDto(
        id = 1,
        title = "Test Movie",
        overview = "Test Overview",
        posterPath = "/path.jpg",
        backdropPath = "/back.jpg",
        releaseDate = "2024-01-01",
        voteAverage = 8.5,
        voteCount = 100
    )

    private val testMovieResponse = MovieResponse(
        page = 1,
        results = listOf(testMovieDto),
        totalPages = 1,
        totalResults = 1
    )

    @Before
    fun setup() {
        repository = MovieRepositoryImpl(api, dao)
    }

    @Test
    fun `getPopularMovies should fetch from API and update cache on success`() = runTest {
        coEvery { api.getPopularMovies(any(), any()) } returns testMovieResponse
        
        val result = repository.getPopularMovies(1).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Test Movie")
        coVerify { dao.clearCachedMovies() }
        coVerify { dao.insertMovies(any()) }
    }

    @Test
    fun `getPopularMovies should return cache on API failure`() = runTest {
        val cachedMovie = testMovieDto.toDomain().toEntity(isCached = true)
        coEvery { api.getPopularMovies(any(), any()) } throws Exception("Network Error")
        every { dao.getCachedMovies() } returns flowOf(listOf(cachedMovie))

        val result = repository.getPopularMovies(1).first()

        assertThat(result).hasSize(1)
        assertThat(result[0].title).isEqualTo("Test Movie")
        coVerify(exactly = 0) { dao.clearCachedMovies() }
    }

    @Test
    fun `getMovieDetails should fetch from API and update cache on success`() = runTest {
        coEvery { api.getMovieDetails(1, any()) } returns testMovieDto
        
        val result = repository.getMovieDetails(1).first()

        assertThat(result.title).isEqualTo("Test Movie")
        coVerify { dao.insertMovie(any()) }
    }

    @Test
    fun `getMovieDetails should return cache on API failure if available`() = runTest {
        val cachedMovie = testMovieDto.toDomain().toEntity()
        coEvery { api.getMovieDetails(1, any()) } throws Exception("Network Error")
        coEvery { dao.getMovieById(1) } returns cachedMovie

        val result = repository.getMovieDetails(1).first()

        assertThat(result.title).isEqualTo("Test Movie")
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
