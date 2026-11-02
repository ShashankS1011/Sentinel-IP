package com.example.sentinel_ip

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.sentinel_ip.ui.theme.SentinelIPTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SentinelAppUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testSentinelAppThemeRendering() {
        composeTestRule.setContent {
            SentinelIPTheme {
                androidx.compose.material3.Text("Sentinel-IP Cybersecurity Suite")
            }
        }
        composeTestRule.onNodeWithText("Sentinel-IP Cybersecurity Suite").assertExists()
    }
}
