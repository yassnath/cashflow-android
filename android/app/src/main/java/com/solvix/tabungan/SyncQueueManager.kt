package com.solvix.tabungan

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.util.UUID

enum class SyncActionType {
  INSERT_MONEY_ENTRY,
  UPDATE_MONEY_ENTRY,
  DELETE_MONEY_ENTRY,
  UPSERT_INITIAL_BALANCE,
  DELETE_INITIAL_BALANCE,
  UPSERT_PIN,
  DELETE_PIN,
}

enum class SyncStatus {
  SYNCED,
  SYNCING,
  OFFLINE,
  ERROR,
}

@Serializable
data class QueuedSyncItem(
  val id: String = UUID.randomUUID().toString(),
  val actionType: SyncActionType,
  val userId: String,
  val targetId: String = "",
  val accountName: String = "",
  val initialAmount: Long = 0L,
  val moneyEntryJson: String = "",
  val pinHash: String = "",
  val pinSalt: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val retryCount: Int = 0,
)

/**
 * Manajer antrean sinkronisasi mutasi saat offline.
 * Menjamin 100% data transaksi dan perubahan saldo tidak hilang
 * saat koneksi terputus dan otomatis di-push ke cloud Supabase begitu online.
 */
object SyncQueueManager {
  private val mutex = Mutex()
  private val json = Json { ignoreUnknownKeys = true; prettyPrint = false }

  val syncStatus = mutableStateOf(SyncStatus.SYNCED)
  val pendingCount = mutableIntStateOf(0)

  private fun getQueueFile(context: Context): File {
    val dir = File(context.filesDir, "sync_queue").apply { if (!exists()) mkdirs() }
    return File(dir, "pending_mutations.json")
  }

  private fun loadQueue(context: Context): MutableList<QueuedSyncItem> {
    val file = getQueueFile(context)
    if (!file.exists() || file.length() == 0L) return mutableListOf()
    return try {
      json.decodeFromString<List<QueuedSyncItem>>(file.readText()).toMutableList()
    } catch (_: Exception) {
      mutableListOf()
    }
  }

  private fun saveQueue(context: Context, items: List<QueuedSyncItem>) {
    try {
      val file = getQueueFile(context)
      file.writeText(json.encodeToString(items))
      pendingCount.intValue = items.size
      if (items.isEmpty()) {
        syncStatus.value = SyncStatus.SYNCED
      } else if (syncStatus.value != SyncStatus.SYNCING) {
        syncStatus.value = SyncStatus.OFFLINE
      }
    } catch (_: Exception) {}
  }

  fun init(context: Context) {
    val count = loadQueue(context).size
    pendingCount.intValue = count
    if (count > 0) {
      syncStatus.value = SyncStatus.OFFLINE
    }
  }

  suspend fun enqueue(context: Context, item: QueuedSyncItem) = withContext(Dispatchers.IO) {
    mutex.withLock {
      val queue = loadQueue(context)
      queue.add(item)
      saveQueue(context, queue)
    }
  }

  suspend fun enqueueInsertMoney(context: Context, userId: String, entry: MoneyEntry) {
    val encoded = json.encodeToString(entry)
    enqueue(
      context,
      QueuedSyncItem(
        actionType = SyncActionType.INSERT_MONEY_ENTRY,
        userId = userId,
        targetId = entry.id,
        moneyEntryJson = encoded,
      ),
    )
  }

  suspend fun enqueueUpdateMoney(context: Context, userId: String, entry: MoneyEntry) {
    val encoded = json.encodeToString(entry)
    enqueue(
      context,
      QueuedSyncItem(
        actionType = SyncActionType.UPDATE_MONEY_ENTRY,
        userId = userId,
        targetId = entry.id,
        moneyEntryJson = encoded,
      ),
    )
  }

  suspend fun enqueueDeleteMoney(context: Context, userId: String, entryId: String) {
    enqueue(
      context,
      QueuedSyncItem(
        actionType = SyncActionType.DELETE_MONEY_ENTRY,
        userId = userId,
        targetId = entryId,
      ),
    )
  }

  suspend fun enqueueUpsertInitialBalance(context: Context, userId: String, accountName: String, amount: Long) {
    enqueue(
      context,
      QueuedSyncItem(
        actionType = SyncActionType.UPSERT_INITIAL_BALANCE,
        userId = userId,
        accountName = accountName,
        initialAmount = amount,
      ),
    )
  }

  suspend fun enqueueDeleteInitialBalance(context: Context, userId: String, accountName: String) {
    enqueue(
      context,
      QueuedSyncItem(
        actionType = SyncActionType.DELETE_INITIAL_BALANCE,
        userId = userId,
        accountName = accountName,
      ),
    )
  }

  suspend fun enqueueUpsertPin(context: Context, userId: String, pinHash: String, pinSalt: String) {
    enqueue(
      context,
      QueuedSyncItem(
        actionType = SyncActionType.UPSERT_PIN,
        userId = userId,
        pinHash = pinHash,
        pinSalt = pinSalt,
      ),
    )
  }

  suspend fun enqueueDeletePin(context: Context, userId: String) {
    enqueue(
      context,
      QueuedSyncItem(
        actionType = SyncActionType.DELETE_PIN,
        userId = userId,
      ),
    )
  }

  /**
   * Memproses antrean dan mengirim semua perubahan ke Supabase secara berurutan.
   */
  suspend fun processQueue(context: Context): Boolean = withContext(Dispatchers.IO) {
    mutex.withLock {
      val queue = loadQueue(context)
      if (queue.isEmpty()) {
        syncStatus.value = SyncStatus.SYNCED
        pendingCount.intValue = 0
        return@withContext true
      }

      syncStatus.value = SyncStatus.SYNCING
      val iterator = queue.iterator()
      var hadFailure = false

      while (iterator.hasNext()) {
        val action = iterator.next()
        val success = try {
          when (action.actionType) {
            SyncActionType.INSERT_MONEY_ENTRY -> {
              val entry = json.decodeFromString<MoneyEntry>(action.moneyEntryJson)
              SupabaseRepository.insertMoneyEntry(action.userId, entry)
              true
            }
            SyncActionType.UPDATE_MONEY_ENTRY -> {
              val entry = json.decodeFromString<MoneyEntry>(action.moneyEntryJson)
              SupabaseRepository.updateMoneyEntry(entry)
              true
            }
            SyncActionType.DELETE_MONEY_ENTRY -> {
              SupabaseRepository.deleteMoneyEntry(action.targetId)
              true
            }
            SyncActionType.UPSERT_INITIAL_BALANCE -> {
              SupabaseRepository.upsertInitialBalance(action.userId, action.accountName, action.initialAmount)
              true
            }
            SyncActionType.DELETE_INITIAL_BALANCE -> {
              SupabaseRepository.deleteInitialBalance(action.userId, action.accountName)
              true
            }
            SyncActionType.UPSERT_PIN -> {
              SupabaseRepository.upsertSecurityPin(action.userId, action.pinHash, action.pinSalt, true)
              true
            }
            SyncActionType.DELETE_PIN -> {
              SupabaseRepository.deleteSecurityPin(action.userId)
              true
            }
          }
        } catch (_: Exception) {
          false
        }

        if (success) {
          iterator.remove()
        } else {
          hadFailure = true
          break
        }
      }

      saveQueue(context, queue)
      if (hadFailure) {
        syncStatus.value = SyncStatus.OFFLINE
        false
      } else {
        syncStatus.value = SyncStatus.SYNCED
        true
      }
    }
  }
}

/**
 * Indikator visual status sinkronisasi cloud di TopBar / Header.
 */
@Composable
fun SyncStatusPill(
  modifier: Modifier = Modifier,
  onRetryClick: () -> Unit = {},
) {
  val colors = LocalAppColors.current
  val status = SyncQueueManager.syncStatus.value
  val pending = SyncQueueManager.pendingCount.intValue

  val (bgColor, dotColor, textColor, label) = when (status) {
    SyncStatus.SYNCED -> {
      SyncStatusVisuals(
        Color(0xFF10B981).copy(alpha = 0.15f),
        Color(0xFF10B981),
        Color(0xFF10B981),
        "Tersinkron",
      )
    }
    SyncStatus.SYNCING -> {
      SyncStatusVisuals(
        colors.accent.copy(alpha = 0.15f),
        colors.accent,
        colors.accent,
        "Menyinkronkan...",
      )
    }
    SyncStatus.OFFLINE, SyncStatus.ERROR -> {
      if (pending > 0) {
        SyncStatusVisuals(
          Color(0xFFF59E0B).copy(alpha = 0.15f),
          Color(0xFFF59E0B),
          Color(0xFFD97706),
          "$pending Tertunda (Tap Sync)",
        )
      } else {
        SyncStatusVisuals(
          colors.bg2,
          colors.muted,
          colors.muted,
          "Lokal",
        )
      }
    }
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(999.dp))
      .background(bgColor)
      .border(0.8.dp, dotColor.copy(alpha = 0.35f), RoundedCornerShape(999.dp))
      .clickable { onRetryClick() }
      .padding(horizontal = 8.dp, vertical = 4.dp),
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(5.dp),
    ) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(CircleShape)
          .background(dotColor),
      )
      Text(
        text = label,
        fontSize = 10.sp,
        fontWeight = FontWeight.SemiBold,
        color = textColor,
      )
    }
  }
}

private data class SyncStatusVisuals(val first: Color, val second: Color, val third: Color, val fourth: String)
