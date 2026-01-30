# Phase 3: Access & Cleanup - Research

**Researched:** 2026-01-30
**Domain:** Bukkit Event Listeners, Scheduler API, Plugin Lifecycle
**Confidence:** HIGH

## Summary

Phase 3 requires two core capabilities: (1) enforcing whitelist access control on player login, and (2) periodically cleaning up expired whitelist entries via a scheduled task. The existing codebase already has a functional `PlayerPreLoginEvent` listener that checks `storage.isPlayerWhitelisted()` - which correctly handles expiry checks via `WhitelistEntry.isValid()`. The access control requirement (ACC-01, ACC-02) is already implemented and working.

The cleanup task requires using Bukkit's `BukkitScheduler` to run a repeating task that removes expired entries from storage. The standard approach is to use `runTaskTimer()` for a synchronous repeating task, store the `BukkitTask` reference, and cancel it in `onDisable()`.

**Primary recommendation:** Implement a cleanup task class using `BukkitRunnable`, add `removeExpiredEntries()` method to `YamlStorage`, wire the task in `onEnable()`, and make the cleanup interval configurable in `config.yml`.

## Standard Stack

### Core (Already in Project)
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Paper API | 1.13.2-R0.1-SNAPSHOT | Bukkit scheduler, event system | Project already uses this |
| MockBukkit | 2.29.0 | Testing scheduler, events | Project already uses this |

### No Additional Dependencies Required
The Bukkit scheduler API is built into the Paper API that the project already depends on. No new libraries needed.

## Architecture Patterns

### Recommended Project Structure
```
src/main/kotlin/.../
├── tasks/
│   └── ExpiredEntriesCleanupTask.kt   # NEW: Cleanup task
├── listeners/
│   └── PlayerPreLoginEvent.kt          # EXISTS: Already implemented
├── storage/
│   └── YamlStorage.kt                   # ADD: removeExpiredEntries()
├── config/general/
│   └── GeneralConfig.kt                 # ADD: cleanupIntervalHours
└── ComfyWhitelist.kt                    # ADD: start/stop cleanup task
```

### Pattern 1: BukkitRunnable for Cleanup Task
**What:** Extend `BukkitRunnable` for the cleanup task, providing self-contained scheduling
**When to use:** For plugin-owned scheduled tasks that need clean lifecycle management
**Example:**
```kotlin
// Source: Bukkit Scheduler API + project conventions
class ExpiredEntriesCleanupTask(
    private val storage: YamlStorage,
    private val plugin: JavaPlugin
) : BukkitRunnable() {

    override fun run() {
        val removedCount = storage.removeExpiredEntries()
        if (removedCount > 0) {
            plugin.logger.info("Removed $removedCount expired whitelist entries")
        }
    }
}
```

### Pattern 2: Plugin Lifecycle Task Management
**What:** Store BukkitTask reference, start in onEnable, cancel in onDisable
**When to use:** For any repeating task that should stop when plugin is disabled
**Example:**
```kotlin
// Source: Bukkit best practices
class ComfyWhitelist : JavaPlugin() {
    private var cleanupTask: BukkitTask? = null

    override fun onEnable() {
        // ... other setup ...
        startCleanupTask()
    }

    override fun onDisable() {
        cleanupTask?.cancel()
    }

    private fun startCleanupTask() {
        val intervalTicks = GeneralConfig.cleanupIntervalHours * 20L * 60L * 60L
        cleanupTask = ExpiredEntriesCleanupTask(storage, this)
            .runTaskTimer(this, intervalTicks, intervalTicks)
    }
}
```

### Pattern 3: Storage Method for Bulk Removal
**What:** Add method to storage that iterates entries, removes expired, and saves
**When to use:** For batch operations that should be atomic
**Example:**
```kotlin
// Source: Project's existing YamlStorage patterns
fun removeExpiredEntries(): Int {
    val expiredKeys = whitelistedPlayers.entries
        .filter { !it.value.isValid() }
        .map { it.key }

    if (expiredKeys.isEmpty()) return 0

    expiredKeys.forEach { whitelistedPlayers.remove(it) }
    save()
    return expiredKeys.size
}
```

### Anti-Patterns to Avoid
- **Using async scheduler for file I/O then touching Bukkit API:** The cleanup task writes to YAML file, which is fine. But sync is safer since it saves atomically and the operation is fast. Don't over-engineer with async.
- **Not storing BukkitTask reference:** Always store the task reference so it can be canceled on disable.
- **Hardcoding cleanup interval:** Make it configurable via `config.yml`.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Repeating task | Manual Thread/Timer | BukkitScheduler.runTaskTimer() | Integrates with server tick system, automatic cleanup on disable |
| Task lifecycle | Manual tracking | BukkitTask.cancel() | Built-in cancellation, no race conditions |
| Time conversion | Manual math | Constants (20 ticks = 1 second) | Standard Bukkit convention |

**Key insight:** The Bukkit scheduler handles all the complexity of thread-safe task scheduling tied to server ticks. Using Java's `ScheduledExecutorService` or raw threads would require manual synchronization and wouldn't integrate with server lifecycle.

## Common Pitfalls

### Pitfall 1: Not Canceling Tasks on Disable
**What goes wrong:** Plugin is disabled/reloaded but tasks keep running, causing errors or duplicate tasks
**Why it happens:** Forgetting to store and cancel the BukkitTask reference
**How to avoid:** Always store `BukkitTask` returned from `runTaskTimer()`, cancel in `onDisable()`
**Warning signs:** Errors after `/reload`, duplicate log messages

### Pitfall 2: Using Async for Simple File Operations
**What goes wrong:** Overly complex code, potential race conditions with storage access
**Why it happens:** Assuming all I/O should be async
**How to avoid:** For fast YAML file operations, sync is fine. The cleanup runs infrequently (hourly) and the file is small.
**Warning signs:** Complex callback chains, synchronization issues

### Pitfall 3: Zero-Delay Initial Execution
**What goes wrong:** Task doesn't run on first tick as expected
**Why it happens:** MockBukkit issue with tick 0, but also good practice to delay startup
**How to avoid:** Use same value for delay and period (e.g., both are the interval)
**Warning signs:** Tests failing with 0 delay

### Pitfall 4: Thread Safety with Storage Access
**What goes wrong:** Race condition between cleanup task and commands modifying storage
**Why it happens:** Using async scheduler while commands run on main thread
**How to avoid:** Use sync scheduler (`runTaskTimer`, not `runTaskTimerAsync`) - all operations on main thread
**Warning signs:** Intermittent data corruption, ConcurrentModificationException

### Pitfall 5: Access Control Already Implemented
**What goes wrong:** Re-implementing listener that already exists and works
**Why it happens:** Not checking existing code thoroughly
**How to avoid:** The `PlayerPreLoginEvent.kt` listener is COMPLETE. It already calls `storage.isPlayerWhitelisted()` which checks `entry.isValid()` including expiry. Just verify with tests.
**Warning signs:** Duplicate listener code, redundant logic

## Code Examples

Verified patterns based on existing codebase and Bukkit API:

### Cleanup Task Implementation
```kotlin
// Based on: Bukkit BukkitRunnable API, project patterns
package com.cocahonka.comfywhitelist.tasks

import com.cocahonka.comfywhitelist.storage.YamlStorage
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scheduler.BukkitRunnable

class ExpiredEntriesCleanupTask(
    private val storage: YamlStorage,
    private val plugin: JavaPlugin
) : BukkitRunnable() {

    override fun run() {
        val removedCount = storage.removeExpiredEntries()
        if (removedCount > 0) {
            plugin.logger.info("Removed $removedCount expired whitelist entries")
        }
    }
}
```

### Storage Extension - removeExpiredEntries()
```kotlin
// Add to YamlStorage.kt, following existing patterns
/**
 * Removes all expired entries from storage and saves.
 *
 * @return Number of entries removed
 */
fun removeExpiredEntries(): Int {
    val expiredKeys = whitelistedPlayers.entries
        .filter { !it.value.isValid() }
        .map { it.key }

    if (expiredKeys.isEmpty()) return 0

    expiredKeys.forEach { whitelistedPlayers.remove(it) }
    save()
    return expiredKeys.size
}
```

### Config Extension
```yaml
# Add to config.yml
# Interval in hours between cleanup task runs (removes expired whitelist entries)
cleanup-interval-hours: 1
```

```kotlin
// Add to GeneralConfig.kt companion object
var cleanupIntervalHours: Int by Delegates.notNull()
    private set

private const val cleanupIntervalKey = "cleanup-interval-hours"

// Add to updateProperties()
cleanupIntervalHours = config.getInt(cleanupIntervalKey, 1).coerceAtLeast(1)
```

### Plugin Integration
```kotlin
// Add to ComfyWhitelist.kt
private var cleanupTask: BukkitTask? = null

// In onPluginEnable(), after loadStorage():
private fun startCleanupTask() {
    val intervalTicks = GeneralConfig.cleanupIntervalHours * 20L * 60L * 60L
    cleanupTask = ExpiredEntriesCleanupTask(storage, this)
        .runTaskTimer(this, intervalTicks, intervalTicks)
}

// In onDisable():
override fun onDisable() {
    cleanupTask?.cancel()
}
```

### Testing Cleanup Task with MockBukkit
```kotlin
// Based on: MockBukkit scheduler documentation, project test patterns
@Test
fun `cleanup task removes expired entries`() {
    // Setup expired entries
    val storage = YamlStorage(plugin.dataFolder)
    storage.addPlayerWithExpiry("expired1", System.currentTimeMillis() - 1000L)
    storage.addPlayerWithExpiry("expired2", System.currentTimeMillis() - 1000L)
    storage.addPlayerWithExpiry("valid", System.currentTimeMillis() + 86400000L)

    // Run cleanup
    val removed = storage.removeExpiredEntries()

    assertEquals(2, removed)
    assertFalse(storage.allWhitelistedPlayers.contains("expired1"))
    assertFalse(storage.allWhitelistedPlayers.contains("expired2"))
    assertTrue(storage.allWhitelistedPlayers.contains("valid"))
}

@Test
fun `cleanup task schedules correctly`() {
    val scheduler = server.scheduler
    val task = ExpiredEntriesCleanupTask(storage, plugin)
        .runTaskTimer(plugin, 20L, 20L)

    assertTrue(scheduler.isQueued(task.taskId))

    task.cancel()
    assertFalse(scheduler.isQueued(task.taskId))
}
```

### Testing Access Control (Verify Existing)
```kotlin
// Based on: Existing PlayerPreLoginEventTest.kt patterns
@Test
fun `expired player is blocked`() {
    generalConfig.enableWhitelist()
    (storage as YamlStorage).addPlayerWithExpiry(
        joiningPlayer.name,
        System.currentTimeMillis() - 1000L  // Expired
    )
    executeEvent()
    assertConnectedFalse()
}

@Test
fun `valid timed player is allowed`() {
    generalConfig.enableWhitelist()
    (storage as YamlStorage).addPlayerWithExpiry(
        joiningPlayer.name,
        System.currentTimeMillis() + 86400000L  // +1 day
    )
    executeEvent()
    assertConnectedTrue()
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Manual Thread scheduling | BukkitScheduler | Always | Use Bukkit's integrated scheduler |
| Global static task tracking | Instance-level BukkitTask field | Best practice | Cleaner lifecycle management |

**Deprecated/outdated:**
- `scheduleSyncRepeatingTask()` returning int ID: Still works but `runTaskTimer()` returning `BukkitTask` is preferred for cleaner cancellation

## Existing Implementation Analysis

### PlayerPreLoginEvent.kt - ALREADY COMPLETE
The existing listener at `src/main/kotlin/.../listeners/PlayerPreLoginEvent.kt`:
```kotlin
@EventHandler(priority = EventPriority.LOWEST)
fun onPlayerPreLoginEvent(event: AsyncPlayerPreLoginEvent) {
    if (!GeneralConfig.whitelistEnabled) return

    val playerName = event.name
    if (!storage.isPlayerWhitelisted(playerName)) {
        event.disallow(
            AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
            Messages.notWhitelisted.toLegacyText()
        )
    }
}
```

**Why this is complete:**
1. Checks `GeneralConfig.whitelistEnabled` - handles disabled whitelist case
2. Calls `storage.isPlayerWhitelisted(playerName)` which internally does:
   - `val entry = whitelistedPlayers[username.lowercase()]`
   - `return entry?.isValid() == true`
3. `WhitelistEntry.isValid()` checks: `expiryTimestamp == null || System.currentTimeMillis() < expiryTimestamp`

**Conclusion:** Access control for expired entries is ALREADY HANDLED. The listener blocks:
- Non-whitelisted players (not in storage)
- Expired players (in storage but `isValid()` returns false)

Only needed: Add tests to verify this behavior explicitly.

## Open Questions

None - all technical questions resolved:

1. **Sync vs Async for cleanup?** - Use sync. File I/O is fast, runs infrequently, avoids race conditions.
2. **How to test scheduled tasks?** - MockBukkit's `BukkitSchedulerMock` with `performTicks()` and `isQueued()`.
3. **Access control for expired entries?** - Already implemented via `isPlayerWhitelisted()` -> `isValid()`.

## Sources

### Primary (HIGH confidence)
- [BukkitScheduler Javadocs](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/scheduler/BukkitScheduler.html) - Complete API reference for runTaskTimer, cancelTask
- [Bukkit Wiki - Scheduler Programming](https://bukkit.fandom.com/wiki/Scheduler_Programming) - Best practices, examples
- [MockBukkit Documentation](https://mockbukkit.readthedocs.io/en/v1.15/scheduler_mock.html) - Scheduler testing
- Existing codebase: `PlayerPreLoginEvent.kt`, `YamlStorage.kt`, `WhitelistEntry.kt`

### Secondary (MEDIUM confidence)
- [SpigotMC Wiki - Scheduler Programming](https://www.spigotmc.org/wiki/scheduler-programming/) - Additional examples verified against Javadocs
- [PaperMC Docs - Scheduling](https://docs.papermc.io/paper/dev/scheduler/) - Paper-specific notes (though project targets 1.13.2)

### Tertiary (LOW confidence)
- None - all claims verified with primary sources

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - Using existing project dependencies only
- Architecture: HIGH - Following established Bukkit patterns and existing codebase conventions
- Pitfalls: HIGH - Based on documented issues and common mistakes in scheduler usage
- Access control: HIGH - Verified by reading existing implementation code

**Research date:** 2026-01-30
**Valid until:** 2026-03-30 (Bukkit scheduler API is stable, no expected changes)
