package com.supersonic.environment.movierating.ui.presentation.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.supersonic.environment.movierating.MainDispatcherRule
import com.supersonic.environment.movierating.domain.model.Movie
import com.supersonic.environment.movierating.domain.repository.MovieRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: DetailViewModel
    private val repository: MovieRepository = mockk(relaxed = true)
    private val savedStateHandle: SavedStateHandle = SavedStateHandle(mapOf("movieId" to 1))

    private val testMovie = Movie(
        id = 1,
        title = "Test Movie",
        overview = "Test Overview",
        posterUrl = "url",
        backdropUrl = "url",
        releaseDate = "2024-01-01",
        rating = 8.5,
        voteCount = 100
    )

    @Before
    fun setup() {
        every { repository.getMovieDetails(1) } returns flowOf(testMovie)
        coEvery { repository.isFavorite(1) } returns false
    }

    @Test
    fun `init should fetch movie details, check favorite and mark as viewed`() = runTest {
        viewModel = DetailViewModel(repository, savedStateHandle)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.movie).isEqualTo(testMovie)
            assertThat(state.isLoading).isFalse()
            assertThat(state.isFavorite).isFalse()
        }

        verify { repository.getMovieDetails(1) }
        coVerify { repository.isFavorite(1) }
        coVerify { repository.markAsViewed(1) }
    }

    @Test
    fun `onEvent Refresh should fetch movie details`() = runTest {
        viewModel = DetailViewModel(repository, savedStateHandle)
        
        viewModel.onEvent(DetailEvent.Refresh)

        verify(exactly = 2) { repository.getMovieDetails(1) }
    }

    @Test
    fun `onEvent ToggleFavorite should call repository and update status`() = runTest {
        coEvery { repository.isFavorite(1) } returnsMany listOf(false, true)
        
        viewModel = DetailViewModel(repository, savedStateHandle)
        
        viewModel.onEvent(DetailEvent.ToggleFavorite)

        viewModel.uiState.test {
            // First item is from init, next should be true
            assertThat(awaitItem().isFavorite).isTrue()
        }

        coVerify { repository.toggleFavorite(1) }
    }

    @Test
    fun `getMovieDetails error should update state with error message`() = runTest {
        every { repository.getMovieDetails(1) } returns flow<Movie> { throw Exception("Network Error") }
        
        viewModel = DetailViewModel(repository, savedStateHandle)

        viewModel.uiState.test {
            val state = awaitItem()
            assertThat(state.error).isEqualTo("Network Error")
            assertThat(state.isLoading).isFalse()
        }
    }
}
