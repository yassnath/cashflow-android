package com.solvix.tabungan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.solvix.tabungan.AppCard
import com.solvix.tabungan.AppStrings
import com.solvix.tabungan.AppTextField
import com.solvix.tabungan.ChatMessage
import com.solvix.tabungan.GhostButton
import com.solvix.tabungan.GradientButton
import com.solvix.tabungan.LocalAppColors
import com.solvix.tabungan.LocalThemeName
import com.solvix.tabungan.Page
import com.solvix.tabungan.SectionTitle
import com.solvix.tabungan.themePageIcon

@Composable
fun AiChatPage(
  messages: List<ChatMessage>,
  isLoading: Boolean,
  onSend: (String) -> Unit,
  onClear: () -> Unit,
  strings: AppStrings,
) {
  val colors = LocalAppColors.current
  val screenHeight = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }
  val chatCardHeight = (screenHeight - 250.dp).coerceIn(340.dp, 560.dp)
  var prompt by rememberSaveable { mutableStateOf("") }
  val messageScrollState = rememberScrollState()

  LaunchedEffect(messages.size, isLoading) {
    messageScrollState.scrollTo(messageScrollState.maxValue)
  }

  Column {
    SectionTitle(
      icon = themePageVisualIcon(LocalThemeName.current, Page.AIChat),
      title = strings["section_ai_chat_title"],
      subtitle = strings["section_ai_chat_subtitle"],
    )
    AppCard {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .height(chatCardHeight),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = strings["ai_disclaimer"],
          color = colors.muted,
          fontSize = 12.sp,
        )
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bg2.copy(alpha = 0.4f))
            .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
            .padding(10.dp),
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .verticalScroll(messageScrollState),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            if (messages.isEmpty()) {
              Text(
                text = strings["ai_prompt_placeholder"],
                color = colors.placeholder,
                fontSize = 12.sp,
              )
            } else {
              messages.forEach { message ->
                val userMessage = message.role.equals("user", ignoreCase = true)
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = if (userMessage) Arrangement.End else Arrangement.Start,
                ) {
                  Box(
                    modifier = Modifier
                      .widthIn(max = 280.dp)
                      .clip(RoundedCornerShape(14.dp))
                      .background(
                        if (userMessage) {
                          Brush.linearGradient(listOf(colors.accent, colors.accent2))
                        } else {
                          Brush.linearGradient(listOf(colors.bg2, colors.bg2))
                        },
                      )
                      .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
                      .padding(horizontal = 12.dp, vertical = 10.dp),
                  ) {
                    Text(
                      text = message.content,
                      color = if (userMessage) Color.White else colors.text,
                      fontSize = 13.sp,
                    )
                  }
                }
              }
            }
            if (isLoading) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start,
              ) {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.bg2)
                    .border(1.dp, colors.cardBorder, RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                  Text(
                    text = strings["ai_typing"],
                    color = colors.muted,
                    fontSize = 12.sp,
                  )
                }
              }
            }
          }
        }
        AppTextField(
          label = "",
          value = prompt,
          onValueChange = { prompt = it },
          placeholder = strings["ai_prompt_placeholder"],
          minLines = 2,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
          GhostButton(
            text = strings["ai_clear"],
            fillMaxWidth = false,
            modifier = Modifier.weight(1f),
            onClick = {
              prompt = ""
              onClear()
            },
          )
          GradientButton(
            text = strings["ai_send"],
            enabled = prompt.trim().isNotBlank() && !isLoading,
            modifier = Modifier.weight(1f),
            onClick = {
              val value = prompt.trim()
              if (value.isBlank()) return@GradientButton
              onSend(value)
              prompt = ""
            },
          )
        }
      }
    }
  }
}
