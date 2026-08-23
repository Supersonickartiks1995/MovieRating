package com.supersonic.environment.movierating.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.supersonic.environment.movierating.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun searchMovie_and_navigateToDetail() {
        // Wait for Home Screen to load by checking for the App Bar title
        composeTestRule.onNodeWithText("Movie Rating").assertIsDisplayed()

        // 1. Locate and click the Search Bar
        // The SearchBar usually has a placeholder "Search movies..."
        composeTestRule.onNodeWithText("Search movies...").performClick()

        // 2. Type movie name
        val searchQuery = "Inception"
        composeTestRule.onNodeWithText("Search movies...").performTextInput(searchQuery)

        // 3. Wait for search results to appear
        // The search logic has a 500ms delay, so we wait until at least 2 nodes with the text exist:
        // One in the SearchBar input field, and one or more in the results list.
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodes(hasText(searchQuery)).fetchSemanticsNodes().size > 1
        }
        
        // Click the search result (index 1 is usually the first item in the result list)
        composeTestRule.onAllNodes(hasText(searchQuery))[1].performClick()

        // 4. Verify Detail Screen is shown
        composeTestRule.onNodeWithText("Overview").assertIsDisplayed()
        
        // Use index 0 here because on the Detail Screen, the search bar is gone, 
        // so there should be only one node with the movie title.
        composeTestRule.onAllNodes(hasText(searchQuery))[0].assertIsDisplayed()
        
        // 5. Navigate back using the Back button in TopAppBar
        composeTestRule.onNodeWithContentDescription("Back").performClick()
        
        // 6. Verify we are back on the Home Screen
        composeTestRule.onNodeWithText("Movie Rating").assertIsDisplayed()
    }
}
