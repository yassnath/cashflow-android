package com.solvix.tabungan

import android.content.Context
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId

/**
 * Handles fetching, milestone calculation, and deadline workers for user data.
 */
fun resolveStartYear(currentUser: UserProfile?): Int {
  val fallback = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)
  val createdAt = currentUser?.createdAt.orEmpty()
  if (createdAt.isBlank()) return fallback
  val millis = parseCreatedAtMillis(createdAt) ?: return fallback
  return Instant.ofEpochMilli(millis).atZone(ZoneId.of("Asia/Jakarta")).year
}

suspend fun loadUserData(
  userId: String,
  authId: String,
  incomeEntries: SnapshotStateList<MoneyEntry>,
  expenseEntries: SnapshotStateList<MoneyEntry>,
  dreamEntries: SnapshotStateList<DreamEntry>,
  achievedGoalIds: SnapshotStateList<String>,
  strings: AppStrings,
  context: Context,
  onGoalReachEvent: (GoalReachEvent) -> Unit,
  onToast: (String) -> Unit,
) {
  if (userId.isBlank()) return
  val loaded = fetchUserData(userId, authId)
  if (loaded.pendingDateUpdates.isNotEmpty()) {
    loaded.pendingDateUpdates.distinctBy { it.first }.forEach { (id, date) ->
      runCatching { SupabaseRepository.updateMoneyEntryDate(id, date) }
    }
  }
  withContext(Dispatchers.Main) {
    incomeEntries.clear()
    incomeEntries.addAll(loaded.income)
    expenseEntries.clear()
    expenseEntries.addAll(loaded.expense)
    dreamEntries.clear()
    dreamEntries.addAll(loaded.dreams)
    updateGoalMilestones(
      incomeEntries = incomeEntries,
      expenseEntries = expenseEntries,
      dreamEntries = dreamEntries,
      achievedGoalIds = achievedGoalIds,
      lastIncomeTotal = 0,
      lastExpenseTotal = 0,
      lastBalanceTotal = 0,
      strings = strings,
      context = context,
      notify = false,
      onTotalsUpdate = { _, _, _ -> },
      onGoalReachEvent = onGoalReachEvent,
      onToast = onToast,
    )
  }
}

fun updateGoalMilestones(
  incomeEntries: List<MoneyEntry>,
  expenseEntries: List<MoneyEntry>,
  dreamEntries: List<DreamEntry>,
  achievedGoalIds: SnapshotStateList<String>,
  lastIncomeTotal: Int,
  lastExpenseTotal: Int,
  lastBalanceTotal: Int,
  strings: AppStrings,
  context: Context,
  notify: Boolean = true,
  onTotalsUpdate: (inc: Int, exp: Int, bal: Int) -> Unit,
  onGoalReachEvent: (GoalReachEvent) -> Unit,
  onToast: (String) -> Unit,
) {
  val totalIncome = incomeEntries.sumOf { it.amount }
  val totalExpense = expenseEntries.sumOf { it.amount }
  val totalBalance = totalIncome - totalExpense
  val previousIncome = lastIncomeTotal
  val previousExpense = lastExpenseTotal
  val previousBalance = lastBalanceTotal
  onTotalsUpdate(totalIncome, totalExpense, totalBalance)

  achievedGoalIds.removeAll { id -> dreamEntries.none { it.id == id } }
  val newlyReached = mutableListOf<DreamEntry>()
  dreamEntries.forEach { goal ->
    val progress = when (goal.sourceType) {
      "balance" -> totalBalance
      "expense" -> totalExpense
      else -> totalIncome
    }
    val previousProgress = when (goal.sourceType) {
      "balance" -> previousBalance
      "expense" -> previousExpense
      else -> previousIncome
    }
    val reached = goal.target > 0 && progress >= goal.target
    val hasReached = achievedGoalIds.contains(goal.id)
    val justReached = previousProgress < goal.target && progress >= goal.target
    if (reached && !hasReached) {
      achievedGoalIds.add(goal.id)
      if (notify && justReached) {
        newlyReached.add(goal)
      }
    }
    if (!reached && hasReached) {
      achievedGoalIds.remove(goal.id)
    }
  }
  if (notify && newlyReached.isNotEmpty()) {
    val msg = if (newlyReached.size == 1) {
      strings["goal_reached_single"].replace("{title}", newlyReached.first().title)
    } else {
      strings["goal_reached_multi"].replace("{count}", newlyReached.size.toString())
    }
    onToast(msg)
    val firstReached = newlyReached.first()
    onGoalReachEvent(
      GoalReachEvent(
        goalId = firstReached.id,
        sourceType = firstReached.sourceType.ifBlank { "income" },
      )
    )
    newlyReached.forEach { goal ->
      showGoalReachedNotification(context, goal, strings)
    }
  }
}
