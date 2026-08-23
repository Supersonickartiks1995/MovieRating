package com.supersonic.environment.movierating.e2e

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.supersonic.environment.movierating.MainActivity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class MovieFlowTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun fullMovieFlow() {
        // 1. Start at Home, verify "Movie Rating" app bar
        composeTestRule.onNodeWithText("Movie Rating").assertIsDisplayed()

        // 2. Wait for content to load and verify "Popular Movies" header
        composeTestRule.waitUntil(timeoutMillis = 10000) {
            composeTestRule.onAllNodes(hasClickAction()).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.onNodeWithText("Popular Movies").assertIsDisplayed()

        // 3. Click the first movie card available (avoiding ambiguity if multiple clickable nodes exist)
        composeTestRule.onAllNodes(hasClickAction())[0].performClick()

        // 4. Verify Detail screen content
        composeTestRule.onNodeWithText("Overview").assertIsDisplayed()

        // 5. Toggle favorite
        composeTestRule.onNodeWithContentDescription("Toggle Favorite").performClick()

        // 6. Go back
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        // 7. Verify navigation works and we are back at Home
        composeTestRule.onNodeWithText("Movie Rating").assertIsDisplayed()
    }
}
