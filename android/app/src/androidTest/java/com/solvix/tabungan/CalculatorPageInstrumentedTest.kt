package com.solvix.tabungan

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class CalculatorPageInstrumentedTest {

  @get:Rule
  val composeRule = createComposeRule()

  @Test
  fun calculatesSimpleExpressionOnTapSequence() {
    composeRule.setContent {
      TabunganTheme(theme = ThemeName.StandardLight) {
        CompositionLocalProvider(
          LocalStrings provides stringsFor(AppLanguage.EN),
          LocalLanguage provides AppLanguage.EN,
        ) {
          CalculatorPage()
        }
      }
    }

    composeRule.onNodeWithText("2").performClick()
    composeRule.onNodeWithText("+").performClick()
    composeRule.onNodeWithText("3").performClick()
    composeRule.onNodeWithText("=").performClick()

    composeRule.onNodeWithTag("calculator_display").assertTextContains("5")
  }
}

