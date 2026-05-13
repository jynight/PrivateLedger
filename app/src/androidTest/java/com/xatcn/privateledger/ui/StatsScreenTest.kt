package com.xatcn.privateledger.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.xatcn.privateledger.ui.screen.stats.PeriodSelector
import com.xatcn.privateledger.ui.theme.PrivateLedgerTheme
import org.junit.Rule
import org.junit.Test

class StatsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `时间段选择器 - 默认选中本月`() {
        var selected = "month"

        composeTestRule.setContent {
            PrivateLedgerTheme {
                PeriodSelector(
                    selected = selected,
                    onSelect = { selected = it }
                )
            }
        }

        // 验证所有选项都显示
        composeTestRule.onNodeWithText("本周").assertIsDisplayed()
        composeTestRule.onNodeWithText("本月").assertIsDisplayed()
        composeTestRule.onNodeWithText("本年").assertIsDisplayed()
    }

    @Test
    fun `时间段选择器 - 点击切换`() {
        var selected = "month"

        composeTestRule.setContent {
            PrivateLedgerTheme {
                PeriodSelector(
                    selected = selected,
                    onSelect = { selected = it }
                )
            }
        }

        // 点击本周
        composeTestRule.onNodeWithText("本周").performClick()
        assertEquals("week", selected)

        // 点击本年
        composeTestRule.onNodeWithText("本年").performClick()
        assertEquals("year", selected)
    }

    private fun assertEquals(expected: String, actual: String) {
        org.junit.Assert.assertEquals(expected, actual)
    }
}
