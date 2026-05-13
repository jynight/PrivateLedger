package com.xatcn.privateledger.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.xatcn.privateledger.data.model.AIAnalysisResult
import com.xatcn.privateledger.data.model.TransactionType
import com.xatcn.privateledger.ui.screen.chat.ConfirmationCardView
import com.xatcn.privateledger.ui.theme.PrivateLedgerTheme
import org.junit.Rule
import org.junit.Test

class ConfirmationCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `确认卡片 - 支出显示`() {
        val result = AIAnalysisResult(
            amount = 25.0f,
            category = "餐饮",
            type = TransactionType.EXPENSE,
            note = "午饭"
        )

        composeTestRule.setContent {
            PrivateLedgerTheme {
                ConfirmationCardView(
                    result = result,
                    onConfirm = {},
                    onCancel = {}
                )
            }
        }

        // 验证标题
        composeTestRule.onNodeWithText("📝 记账确认").assertIsDisplayed()

        // 验证类型
        composeTestRule.onNodeWithText("支出").assertIsDisplayed()

        // 验证金额
        composeTestRule.onNodeWithText("¥25.00").assertIsDisplayed()

        // 验证类别
        composeTestRule.onNodeWithText("餐饮").assertIsDisplayed()

        // 验证备注
        composeTestRule.onNodeWithText("备注：午饭").assertIsDisplayed()

        // 验证按钮
        composeTestRule.onNodeWithText("取消").assertIsDisplayed()
        composeTestRule.onNodeWithText("确认记账").assertIsDisplayed()
    }

    @Test
    fun `确认卡片 - 收入显示`() {
        val result = AIAnalysisResult(
            amount = 8000.0f,
            category = "工资",
            type = TransactionType.INCOME,
            note = "月薪"
        )

        composeTestRule.setContent {
            PrivateLedgerTheme {
                ConfirmationCardView(
                    result = result,
                    onConfirm = {},
                    onCancel = {}
                )
            }
        }

        // 验证类型
        composeTestRule.onNodeWithText("收入").assertIsDisplayed()

        // 验证金额
        composeTestRule.onNodeWithText("¥8000.00").assertIsDisplayed()

        // 验证类别
        composeTestRule.onNodeWithText("工资").assertIsDisplayed()
    }

    @Test
    fun `确认卡片 - 点击确认`() {
        var confirmed = false
        val result = AIAnalysisResult(
            amount = 25.0f,
            category = "餐饮",
            type = TransactionType.EXPENSE,
            note = "午饭"
        )

        composeTestRule.setContent {
            PrivateLedgerTheme {
                ConfirmationCardView(
                    result = result,
                    onConfirm = { confirmed = true },
                    onCancel = {}
                )
            }
        }

        // 点击确认按钮
        composeTestRule.onNodeWithText("确认记账").performClick()

        // 验证回调被调用
        assertTrue(confirmed)
    }

    @Test
    fun `确认卡片 - 点击取消`() {
        var cancelled = false
        val result = AIAnalysisResult(
            amount = 25.0f,
            category = "餐饮",
            type = TransactionType.EXPENSE,
            note = "午饭"
        )

        composeTestRule.setContent {
            PrivateLedgerTheme {
                ConfirmationCardView(
                    result = result,
                    onConfirm = {},
                    onCancel = { cancelled = true }
                )
            }
        }

        // 点击取消按钮
        composeTestRule.onNodeWithText("取消").performClick()

        // 验证回调被调用
        assertTrue(cancelled)
    }
}
