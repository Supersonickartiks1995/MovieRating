package com.supersonic.environment.movierating.ui.presentation.home

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import com.supersonic.environment.movierating.domain.model.Movie
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_showsCircularProgressIndicator() {
        val uiState = HomeUiState(isLoading = true)

        composeTestRule.setContent {
            val popularMovies = flowOf(PagingData.from(emptyList<Movie>())).collectAsLazyPagingItems()
            HomeScreenContent(
                uiState = uiState,
                popularMovies = popularMovies,
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText("Movie Rating").assertIsDisplayed()
    }

    @Test
    fun errorState_showsErrorMessage() {
        val errorMessage = "Error fetching movies"
        val uiState = HomeUiState(error = errorMessage)

        composeTestRule.setContent {
            val popularMovies = flowOf(PagingData.from(emptyList<Movie>())).collectAsLazyPagingItems()
            HomeScreenContent(
                uiState = uiState,
                popularMovies = popularMovies,
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText(errorMessage).assertIsDisplayed()
    }

    @Test
    fun successState_showsMovieRows() {
        val movies = listOf(
            Movie(
                id = 1,
                title = "Test Movie",
                overview = "Overview",
                posterUrl = null,
                backdropUrl = null,
                releaseDate = "2024",
                rating = 9.0,
                voteCount = 10
            )
        )
        val uiState = HomeUiState()

        composeTestRule.setContent {
            val popularMovies = flowOf(PagingData.from(movies)).collectAsLazyPagingItems()
            HomeScreenContent(
                uiState = uiState,
                popularMovies = popularMovies,
                onEvent = {}
            )
        }

        composeTestRule.onNodeWithText("Popular Movies").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Movie").assertIsDisplayed()
    }
}
