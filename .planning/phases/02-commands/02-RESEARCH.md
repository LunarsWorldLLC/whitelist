# Phase 2: Commands - Research

**Researched:** 2026-01-30
**Domain:** Bukkit command implementation, Kotlin command patterns, expiry-aware whitelist management
**Confidence:** HIGH

## Summary

This phase implements all whitelist management commands for the simplified plugin. The current command system uses a SubCommand interface pattern with CommandHandler dispatching to individual command classes. Phase 1 established the storage foundation with `WhitelistEntry` (player name + nullable expiry timestamp) and `YamlStorage` methods (`addPlayerWithExpiry`, `getEntry`).

The key changes are:
1. Modify `AddCommand` to call `addPlayerWithExpiry()` with 31-day expiry, and extend existing entries
2. Modify `ListCommand` to show player names with "X days remaining" format
3. Create new `CheckCommand` for checking specific player's remaining time
4. Delete 6 unused commands: `EnableCommand`, `DisableCommand`, `StatusCommand`, `ClearCommand`, `ReloadCommand`, `HelpCommand`
5. Update `CommandHandler` to only register kept commands
6. Update `CommandTabCompleter` for `check` command tab completion
7. Update `plugin.yml` to remove deleted permissions
8. Simplify message format (SIM-03 requirement - remove MiniMessage tags where appropriate)

**Primary recommendation:** Modify existing commands to use expiry storage, delete unused commands and their tests, add new `CheckCommand`, update tab completion and permissions.

## Standard Stack

The established libraries/tools for this domain:

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Bukkit Command API | Paper 1.13.2+ | Command execution | Already used by CommandHandler |
| Kotlin stdlib | 1.8.10 | Extension functions, null safety | Project standard |
| Kyori Adventure | 4.13.1 | Text components and formatting | Keeping for styled output |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| MessageFormat | existing | MiniMessage styling | Color formatting in messages |
| Messages object | Phase 1 | Hardcoded English messages | All user-facing strings |
| WhitelistEntry | Phase 1 | Expiry-aware entries | Storage operations |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Modify AddCommand | New AddWithExpiryCommand | More code, less intuitive for users |
| Delete commands | Deprecate with warnings | Users asked for simplification, clean delete is better |
| MiniMessage tags | Plain Bukkit color codes | MiniMessage already set up, keep for consistency |

**Installation:**
No new dependencies required - all needed tools already available from Phase 1.

## Architecture Patterns

### Current Command Structure (Files to Keep/Modify)
```
src/main/kotlin/com/cocahonka/comfywhitelist/commands/
├── CommandHandler.kt        # MODIFY: Remove deleted command registrations
├── CommandTabCompleter.kt   # MODIFY: Add check command tab completion
├── SubCommand.kt            # KEEP: Interface unchanged
└── sub/
    ├── AddCommand.kt        # MODIFY: 31-day expiry, extend on re-add
    ├── RemoveCommand.kt     # KEEP: Works as-is
    ├── ListCommand.kt       # MODIFY: Show days remaining format
    ├── CheckCommand.kt      # CREATE: New command for player status
    ├── EnableCommand.kt     # DELETE
    ├── DisableCommand.kt    # DELETE
    ├── StatusCommand.kt     # DELETE
    ├── ClearCommand.kt      # DELETE
    ├── ReloadCommand.kt     # DELETE
    └── HelpCommand.kt       # DELETE
```

### Pattern 1: Extending Expiry on Re-Add
**What:** When adding an already-whitelisted player, extend their expiry by 31 days from current time
**When to use:** CMD-02 requirement
**Example:**
```kotlin
// In AddCommand.execute()
val existingEntry = storage.getEntry(playerName)
val newExpiry = System.currentTimeMillis() + (31L * 24 * 60 * 60 * 1000)

if (existingEntry != null && existingEntry.isValid()) {
    // Player exists and valid - extend from NOW (not from their current expiry)
    storage.addPlayerWithExpiry(playerName, newExpiry)
    sender.sendMessage(Messages.playerExpiryExtended) // Need new message
} else {
    // New player or expired entry - add fresh
    storage.addPlayerWithExpiry(playerName, newExpiry)
    sender.sendMessage(Messages.playerAdded)
}
```

### Pattern 2: Days Remaining Display
**What:** Convert milliseconds remaining to human-readable "X days" format
**When to use:** List and Check commands
**Example:**
```kotlin
// Utility function for commands
fun formatDaysRemaining(entry: WhitelistEntry): String {
    if (entry.isPermanent()) return "permanent"
    val remainingMillis = entry.remainingTimeMillis() ?: return "expired"
    if (remainingMillis <= 0) return "expired"
    val days = remainingMillis / (24 * 60 * 60 * 1000)
    return if (days == 1L) "1 day" else "$days days"
}

// In ListCommand - show: "PlayerName (5 days remaining)"
val playerList = storage.getAllWhitelistedPlayersWithEntries().map { entry ->
    "${entry.playerName} (${formatDaysRemaining(entry)} remaining)"
}
```

### Pattern 3: Check Command Structure
**What:** New subcommand to check specific player's whitelist status
**When to use:** CMD-05 requirement
**Example:**
```kotlin
class CheckCommand(private val storage: YamlStorage) : SubCommand {
    override val identifier = "check"
    override val permission = "comfywhitelist.check"
    override val usage = "/comfywl check <name>"

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (isInvalidUsage(sender) { args.size == 1 }) return false

        val playerName = args[0]
        if (!playerName.matches(SubCommand.playerNameRegex)) {
            sender.sendMessage(Messages.invalidPlayerName)
            return false
        }

        val entry = storage.getEntry(playerName)
        if (entry == null || !entry.isValid()) {
            // Player not found or expired
            val msg = Messages.playerNotFound.replaceText(nameReplacement(playerName))
            sender.sendMessage(msg)
            return true
        }

        // Found and valid
        val daysRemaining = formatDaysRemaining(entry)
        val msg = Messages.playerCheckResult.replaceText(
            nameReplacement(entry.playerName),
            daysReplacement(daysRemaining)
        )
        sender.sendMessage(msg)
        return true
    }
}
```

### Anti-Patterns to Avoid
- **Calculating expiry extension from old expiry:** Always extend from NOW, not from existing timestamp
- **Showing expired entries in list:** Filter them out (already handled by `getAllWhitelistedPlayers()`)
- **Leaving dead code:** Fully delete unused commands, don't just comment out
- **Forgetting test cleanup:** Delete tests for deleted commands

## Don't Hand-Roll

Problems that look simple but have existing solutions:

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Time formatting | Custom date parser | Simple division math | Only need days, no complex formatting |
| Message replacement | Manual string.replace() | MessageFormat.ConfigBuilders | Already handles MiniMessage tags |
| Tab completion | Custom player lookup | Bukkit's `server.onlinePlayers` | Standard Bukkit API |
| Permission checking | Manual sender checks | CommandHandler already does it | Centralized permission logic |

**Key insight:** The existing SubCommand pattern and CommandHandler infrastructure handles 90% of the work. Focus on modifying execute() methods, not restructuring.

## Common Pitfalls

### Pitfall 1: Breaking Storage Type Compatibility
**What goes wrong:** Using `Storage` interface instead of `YamlStorage` for expiry methods
**Why it happens:** `Storage` interface doesn't have `addPlayerWithExpiry()` or `getEntry()`
**How to avoid:** Commands needing expiry must receive `YamlStorage` not `Storage`
**Warning signs:** Compile errors about missing methods, casting to YamlStorage

### Pitfall 2: 31-Day Calculation Overflow
**What goes wrong:** Integer overflow when calculating milliseconds
**Why it happens:** `31 * 24 * 60 * 60 * 1000` overflows Int
**How to avoid:** Use `31L * 24 * 60 * 60 * 1000` (Long literal)
**Warning signs:** Negative or zero expiry timestamps

### Pitfall 3: Forgetting Tab Completion for Check
**What goes wrong:** Check command has no tab completion for player names
**Why it happens:** Only add/remove had special handling
**How to avoid:** Add CheckCommand case to CommandTabCompleter
**Warning signs:** No suggestions when typing `/comfywl check <tab>`

### Pitfall 4: Not Updating plugin.yml Permissions
**What goes wrong:** Deleted commands still listed in permissions, or check missing
**Why it happens:** Forgetting to sync plugin.yml with code changes
**How to avoid:** Update plugin.yml as part of command deletion/addition
**Warning signs:** Permission warnings in console, check command inaccessible

### Pitfall 5: Test Files Left Behind
**What goes wrong:** Tests for deleted commands cause failures
**Why it happens:** Deleting source but not test files
**How to avoid:** Delete test files for each deleted command
**Warning signs:** Compile errors in test directory

### Pitfall 6: Messages Object Missing New Messages
**What goes wrong:** Missing messages for extended expiry, check results
**Why it happens:** New command responses need new message definitions
**How to avoid:** Add messages to Messages object before using them
**Warning signs:** Unresolved reference compile errors

## Code Examples

Verified patterns from existing codebase:

### Modified AddCommand (Full)
```kotlin
// Source: Existing AddCommand.kt pattern + Phase 1 storage
package com.cocahonka.comfywhitelist.commands.sub

import com.cocahonka.comfywhitelist.LegacyUtils.sendMessage
import com.cocahonka.comfywhitelist.commands.SubCommand
import com.cocahonka.comfywhitelist.config.message.Messages
import com.cocahonka.comfywhitelist.config.message.MessageFormat
import com.cocahonka.comfywhitelist.storage.YamlStorage
import org.bukkit.command.CommandSender

class AddCommand(private val storage: YamlStorage) : SubCommand {

    override val identifier = "add"
    override val permission = "comfywhitelist.add"
    override val usage = "/comfywl add <name>"

    companion object {
        const val EXPIRY_DAYS = 31L
        const val MILLIS_PER_DAY = 24L * 60L * 60L * 1000L
    }

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (isInvalidUsage(sender) { args.size == 1 }) return false

        val playerName = args[0]
        if (!playerName.matches(SubCommand.playerNameRegex)) {
            sender.sendMessage(Messages.invalidPlayerName)
            return false
        }

        val newExpiry = System.currentTimeMillis() + (EXPIRY_DAYS * MILLIS_PER_DAY)
        val existingEntry = storage.getEntry(playerName)
        val replacementConfig = MessageFormat.ConfigBuilders.nameReplacementConfigBuilder(playerName)

        val message = if (existingEntry != null && existingEntry.isValid()) {
            // Existing valid player - extend expiry
            Messages.playerExpiryExtended.replaceText(replacementConfig)
        } else {
            // New player or expired - fresh add
            Messages.playerAdded.replaceText(replacementConfig)
        }

        storage.addPlayerWithExpiry(playerName, newExpiry)
        sender.sendMessage(message)
        return true
    }
}
```

### New CheckCommand (Full)
```kotlin
// Source: Based on existing command patterns
package com.cocahonka.comfywhitelist.commands.sub

import com.cocahonka.comfywhitelist.LegacyUtils.sendMessage
import com.cocahonka.comfywhitelist.commands.SubCommand
import com.cocahonka.comfywhitelist.config.message.Messages
import com.cocahonka.comfywhitelist.config.message.MessageFormat
import com.cocahonka.comfywhitelist.storage.YamlStorage
import com.cocahonka.comfywhitelist.storage.WhitelistEntry
import org.bukkit.command.CommandSender

class CheckCommand(private val storage: YamlStorage) : SubCommand {

    override val identifier = "check"
    override val permission = "comfywhitelist.check"
    override val usage = "/comfywl check <name>"

    override fun execute(sender: CommandSender, args: Array<String>): Boolean {
        if (isInvalidUsage(sender) { args.size == 1 }) return false

        val playerName = args[0]
        if (!playerName.matches(SubCommand.playerNameRegex)) {
            sender.sendMessage(Messages.invalidPlayerName)
            return false
        }

        val entry = storage.getEntry(playerName)
        val nameReplacement = MessageFormat.ConfigBuilders.nameReplacementConfigBuilder(playerName)

        if (entry == null || !entry.isValid()) {
            val message = Messages.nonExistentPlayerName.replaceText(nameReplacement)
            sender.sendMessage(message)
            return true
        }

        // Build message with days remaining
        val daysRemaining = formatDaysRemaining(entry)
        val message = Messages.playerCheckResult
            .replaceText(nameReplacement)
            .replaceText(MessageFormat.ConfigBuilders.daysReplacementConfigBuilder(daysRemaining))
        sender.sendMessage(message)
        return true
    }

    private fun formatDaysRemaining(entry: WhitelistEntry): String {
        if (entry.isPermanent()) return "permanent"
        val remainingMillis = entry.remainingTimeMillis() ?: return "0"
        if (remainingMillis <= 0) return "0"
        val days = remainingMillis / (24 * 60 * 60 * 1000)
        return days.toString()
    }
}
```

### Modified ListCommand (Key Change)
```kotlin
// In execute() - show format: "PlayerName (X days remaining)"
override fun execute(sender: CommandSender, args: Array<String>): Boolean {
    if (isInvalidUsage(sender) { args.isEmpty() }) return false

    val entries = storage.getAllEntriesWithExpiry() // Need new method

    if (entries.isEmpty()) {
        sender.sendMessage(Messages.emptyWhitelistedPlayersList)
        return true
    }

    // Format each entry as "name (X days remaining)"
    val formattedList = entries.map { entry ->
        val days = formatDaysRemaining(entry)
        "${entry.playerName} ($days remaining)"
    }.toSet()

    val replacementConfig = MessageFormat.ConfigBuilders.playersReplacementConfigBuilder(formattedList)
    val message = Messages.whitelistedPlayersList.replaceText(replacementConfig)
    sender.sendMessage(message)
    return true
}

private fun formatDaysRemaining(entry: WhitelistEntry): String {
    if (entry.isPermanent()) return "permanent"
    val remainingMillis = entry.remainingTimeMillis() ?: return "0 days"
    if (remainingMillis <= 0) return "0 days"
    val days = remainingMillis / (24 * 60 * 60 * 1000)
    return if (days == 1L) "1 day" else "$days days"
}
```

### Modified CommandHandler.init
```kotlin
// Source: Existing CommandHandler.kt
init {
    val commands = listOf(
        AddCommand(storage),       // KEEP - modified for expiry
        RemoveCommand(storage),    // KEEP - unchanged
        ListCommand(storage),      // KEEP - modified for days display
        CheckCommand(storage),     // NEW
        // DELETE: StatusCommand, EnableCommand, DisableCommand, ClearCommand, ReloadCommand, HelpCommand
    )
    subCommands = commands
}
```

### Modified CommandTabCompleter
```kotlin
// Source: Existing CommandTabCompleter.kt
override fun onTabComplete(...): MutableList<String> {
    if (args.size == 1) {
        // Existing: suggest subcommand names
        return subCommands.map { it.identifier }
            .filter { it.startsWith(args[0], ignoreCase = true) }
            .toMutableList()
    }

    if (args.size == 2) {
        val subCommandIdentifier = args[0]
        val subCommandParam = args[1]

        when (subCommandIdentifier.lowercase()) {
            "add" -> {
                // Suggest online players
                return sender.server.onlinePlayers
                    .map { it.name }
                    .filter { it.startsWith(subCommandParam, ignoreCase = true) }
                    .toMutableList()
            }
            "remove", "check" -> {  // ADD check here
                // Suggest whitelisted players
                return storage.allWhitelistedPlayers
                    .filter { it.startsWith(subCommandParam, ignoreCase = true) }
                    .toMutableList()
            }
        }
    }

    return mutableListOf()
}
```

### New Messages Needed
```kotlin
// Add to Messages object
val playerExpiryExtended: Component = styled("<comfy>Player <success><name></success> whitelist <success>extended</success> by 31 days.")
val playerCheckResult: Component = styled("<comfy>Player <success><name></success> has <success><days> days</success> remaining.")
```

### New MessageFormat.ConfigBuilders
```kotlin
// Add to MessageFormat.ConfigBuilders
val daysReplacementConfigBuilder = { days: String ->
    TextReplacementConfig.builder()
        .match("<days>")
        .replacement(days)
        .build()
}
```

## Files to Modify/Create/Delete

### CREATE
| File | Purpose |
|------|---------|
| `src/main/kotlin/.../commands/sub/CheckCommand.kt` | New check command |
| `src/test/kotlin/.../commands/sub/CheckCommandTest.kt` | Tests for check command |

### MODIFY
| File | Changes |
|------|---------|
| `src/main/kotlin/.../commands/sub/AddCommand.kt` | Use YamlStorage, add 31-day expiry, extend on re-add |
| `src/main/kotlin/.../commands/sub/ListCommand.kt` | Show days remaining format |
| `src/main/kotlin/.../commands/sub/RemoveCommand.kt` | Change Storage to YamlStorage type |
| `src/main/kotlin/.../commands/CommandHandler.kt` | Remove deleted commands from init list |
| `src/main/kotlin/.../commands/CommandTabCompleter.kt` | Add check command completion |
| `src/main/kotlin/.../config/message/Messages.kt` | Add new messages for extend/check |
| `src/main/kotlin/.../config/message/MessageFormat.kt` | Add daysReplacementConfigBuilder |
| `src/main/resources/plugin.yml` | Remove deleted permissions, add check permission |
| `src/test/kotlin/.../commands/sub/AddCommandTest.kt` | Test expiry functionality |
| `src/test/kotlin/.../commands/sub/ListCommandTest.kt` | Test days remaining display |

### DELETE
| File | Reason |
|------|--------|
| `src/main/kotlin/.../commands/sub/EnableCommand.kt` | SIM-02: Unused command |
| `src/main/kotlin/.../commands/sub/DisableCommand.kt` | SIM-02: Unused command |
| `src/main/kotlin/.../commands/sub/StatusCommand.kt` | SIM-02: Unused command |
| `src/main/kotlin/.../commands/sub/ClearCommand.kt` | SIM-02: Unused command |
| `src/main/kotlin/.../commands/sub/ReloadCommand.kt` | SIM-02: Unused command |
| `src/main/kotlin/.../commands/sub/HelpCommand.kt` | SIM-02: Unused command |
| `src/test/kotlin/.../commands/sub/EnableCommandTest.kt` | Test for deleted command |
| `src/test/kotlin/.../commands/sub/DisableCommandTest.kt` | Test for deleted command |
| `src/test/kotlin/.../commands/sub/StatusCommandTest.kt` | Test for deleted command |
| `src/test/kotlin/.../commands/sub/ClearCommandTest.kt` | Test for deleted command |
| `src/test/kotlin/.../commands/sub/ReloadCommandTest.kt` | Test for deleted command |
| `src/test/kotlin/.../commands/sub/HelpCommandTest.kt` | Test for deleted command |

## Storage Method Requirements

The commands need these YamlStorage methods (from Phase 1 or to be added):

| Method | Exists? | Used By |
|--------|---------|---------|
| `addPlayerWithExpiry(name, timestamp)` | Yes (Phase 1) | AddCommand |
| `getEntry(name): WhitelistEntry?` | Yes (Phase 1) | AddCommand, CheckCommand |
| `removePlayer(name)` | Yes | RemoveCommand |
| `isPlayerWhitelisted(name)` | Yes | RemoveCommand pre-check |
| `getAllWhitelistedPlayers(): Set<String>` | Yes | Tab completion |
| `getAllEntriesWithExpiry(): List<WhitelistEntry>` | **NO - NEED** | ListCommand |

**Note:** ListCommand needs a way to get entries with their expiry info, not just names. Add to YamlStorage:

```kotlin
fun getAllValidEntries(): List<WhitelistEntry> {
    return whitelistedPlayers.values.filter { it.isValid() }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| 9 commands | 4 commands | This phase | Simpler user experience |
| Permanent-only whitelist | 31-day expiry default | This phase | Automatic cleanup |
| Just player names | Names + days remaining | This phase | Better visibility |
| Generic "added" message | Distinct add vs extend | This phase | Clearer feedback |

**Deprecated/outdated:**
- `EnableCommand`, `DisableCommand`: Not needed - whitelist always on
- `StatusCommand`: No on/off state to check
- `ClearCommand`: Dangerous, rarely used
- `ReloadCommand`: Config is minimal, restart if needed
- `HelpCommand`: Only 4 commands, self-explanatory

## Open Questions

Things that couldn't be fully resolved:

1. **Message simplification scope (SIM-03)**
   - What we know: Requirement says "simplify message formatting (no MiniMessage tags)"
   - What's unclear: Does this mean remove ALL MiniMessage tags or just complex ones?
   - Recommendation: Keep MiniMessage for colors (comfy, warning, success), just ensure messages are simple English strings. The Phase 1 Messages object already uses this pattern.

2. **Extension behavior on add**
   - What we know: CMD-02 says "extends expiry by 31 days"
   - What's unclear: Extend from current time or from existing expiry?
   - Recommendation: Extend from NOW (current time + 31 days). This is simpler and prevents manipulation.

3. **Handling permanent entries in list**
   - What we know: Legacy permanent entries (null expiry) exist
   - What's unclear: How to display them in list
   - Recommendation: Show as "permanent" instead of "X days remaining"

## Sources

### Primary (HIGH confidence)
- Existing codebase: `CommandHandler.kt`, `SubCommand.kt`, all command implementations
- Phase 1 research: `01-RESEARCH.md` - storage patterns
- Phase 1 implementation: `YamlStorage.kt`, `WhitelistEntry.kt`, `Messages.kt`

### Secondary (MEDIUM confidence)
- Bukkit Command API documentation
- Existing test patterns in `CommandTestBase.kt`, `AddCommandTest.kt`

### Tertiary (LOW confidence)
- None - all patterns derived from existing codebase

## Metadata

**Confidence breakdown:**
- Command structure: HIGH - direct analysis of existing code
- Storage integration: HIGH - Phase 1 already implemented needed methods
- Message patterns: HIGH - existing Messages object and MessageFormat
- Test patterns: HIGH - existing test base provides template

**Research date:** 2026-01-30
**Valid until:** 2026-03-01 (stable domain, no external changes expected)
