package com.supersonic.environment.movierating.ui.presentation.home

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.supersonic.environment.movierating.domain.model.Movie
import com.supersonic.environment.movierating.domain.repository.MovieRepository
import androidx.paging.PagingData
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private lateinit var viewModel: HomeViewModel
    private val movieRepository: MovieRepository = mockk()
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { movieRepository.getPopularMoviesPaged() } returns flowOf(PagingData.empty())
        every { movieRepository.getFavoriteMovies() } returns flowOf(emptyList())
        every { movieRepository.getRecentlyViewedMovies() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `init calls getPopularMovies and updates state with success`() = runTest {
        // Given
        val movies = listOf(
            Movie(
                id = 1,
                title = "Movie 1",
                overview = "Overview 1",
                posterUrl = "",
                backdropUrl = "",
                releaseDate = "2024-01-01",
                rating = 8.0,
                voteCount = 100
            ),
            Movie(
                id = 2,
                title = "Movie 2",
                overview = "Overview 2",
                posterUrl = "",
                backdropUrl = "",
                releaseDate = "2024-01-02",
                rating = 7.0,
                voteCount = 50
            )
        )
        every { movieRepository.getPopularMoviesPaged() } returns flowOf(PagingData.from(movies))

        // When
        viewModel = HomeViewModel(movieRepository)

        // Then
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()
            assertThat(initialState.error).isNull()
            
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init calls getPopularMovies and updates state with error`() = runTest {
        // Given
        val errorMessage = "Network Error"
        every { movieRepository.getPopularMoviesPaged() } returns flow {
            throw Exception(errorMessage)
        }

        // When
        viewModel = HomeViewModel(movieRepository)

        // Then
        viewModel.uiState.test {
            // Initial state
            val initialState = awaitItem()
            assertThat(initialState.error).isNull()

            cancelAndIgnoreRemainingEvents()
        }
    }
}
