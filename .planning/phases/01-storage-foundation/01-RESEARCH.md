# Phase 1: Storage Foundation - Research

**Researched:** 2026-01-30
**Domain:** Kotlin data storage, Bukkit YAML configuration, expiry-aware timestamps
**Confidence:** HIGH

## Summary

This phase transforms a simple string-based player whitelist into an expiry-aware storage system. The current `YamlStorage` stores players as a simple `Set<String>` persisted to YAML. The new system must store `WhitelistEntry` objects containing player name and optional expiry timestamp (null = permanent).

The codebase uses Kotlin 1.8.10 with Paper API 1.13.2+, targeting Java 8. The existing storage pattern uses Bukkit's `YamlConfiguration` for persistence. The locale system (`Message` sealed class, `MessageConfig`, `Locale` enum, and 17 locale YAML files) should be removed, replacing all messages with hardcoded English strings.

**Primary recommendation:** Create a `WhitelistEntry` data class with nullable `Long` expiry timestamp, modify `YamlStorage` to store entries as YAML sections with player-name keys, and inline all English messages directly in code while removing the locale infrastructure.

## Standard Stack

The established libraries/tools for this domain:

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kotlin stdlib | 1.8.10 | Data classes, null safety | Already in project |
| Bukkit YamlConfiguration | Paper 1.13.2+ | YAML persistence | Already used by YamlStorage |
| java.time.Instant | JDK 8+ | Timestamp representation | Standard JDK, no dependencies |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| System.currentTimeMillis() | JDK | Epoch milliseconds | For creating/comparing timestamps |
| Kyori Adventure | 4.13.1 | Text components | Already used for messages (keep for formatting) |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Long epoch millis | kotlinx-datetime Instant | Adds dependency, requires Kotlin 2.1.20+ for best support |
| Nullable Long | Sealed class (Permanent/Temporary) | More type-safe but more complex for YAML serialization |
| YAML sections | ConfigurationSerializable | More boilerplate, overkill for simple structure |

**Installation:**
No new dependencies required - all needed tools are already available.

## Architecture Patterns

### Recommended Data Structure
```kotlin
/**
 * Represents a whitelist entry with optional expiry.
 * @param playerName The player's username (case-sensitive)
 * @param expiryTimestamp Epoch milliseconds when entry expires, or null for permanent
 */
data class WhitelistEntry(
    val playerName: String,
    val expiryTimestamp: Long?  // null = permanent, non-null = temporary
) {
    /**
     * Check if this entry is currently valid (not expired).
     * Permanent entries (null expiry) are always valid.
     */
    fun isValid(): Boolean = expiryTimestamp == null || System.currentTimeMillis() < expiryTimestamp

    /**
     * Check if this is a permanent entry.
     */
    fun isPermanent(): Boolean = expiryTimestamp == null
}
```

### Recommended YAML Structure
```yaml
# whitelist.yml - new format
players:
  Notch:
    expiry: null        # permanent entry (or omit key entirely)
  Steve:
    expiry: 1735689600000  # temporary - expires at this epoch millis
  Alex:
    # no expiry key = permanent (backward compatible with migration)
```

### Recommended Project Structure Changes
```
src/main/kotlin/com/cocahonka/comfywhitelist/
├── storage/
│   ├── WhitelistEntry.kt       # NEW: Data class for entries
│   └── YamlStorage.kt          # MODIFY: Store entries instead of strings
├── config/
│   ├── base/
│   │   ├── ConfigManager.kt    # KEEP: Used by GeneralConfig
│   │   └── Locale.kt           # DELETE: No longer needed
│   ├── general/
│   │   └── GeneralConfig.kt    # MODIFY: Remove locale property
│   └── message/
│       ├── Message.kt          # DELETE: Entire sealed class
│       ├── MessageConfig.kt    # DELETE: Locale loading
│       └── MessageFormat.kt    # KEEP: Styling utilities
└── ...

src/main/resources/
├── locales/                    # DELETE: Entire directory (17 files)
└── config.yml                  # MODIFY: Remove locale setting
```

### Pattern 1: Nullable Timestamp for Permanent vs Temporary
**What:** Use `null` to represent permanent entries, non-null `Long` for temporary
**When to use:** Simple either/or state with minimal overhead
**Example:**
```kotlin
// Source: Standard Kotlin nullable pattern
val permanent = WhitelistEntry("Notch", null)
val temporary = WhitelistEntry("Steve", System.currentTimeMillis() + 86400000L) // +1 day

if (entry.isValid()) {
    // Allow login
}
```

### Pattern 2: YAML Section-Based Storage
**What:** Store each player as a YAML section with configurable properties
**When to use:** When entries have multiple properties beyond just a name
**Example:**
```kotlin
// Source: Bukkit Configuration API Reference
// https://bukkit.fandom.com/wiki/Configuration_API_Reference

// Writing
config.set("players.$playerName.expiry", entry.expiryTimestamp)

// Reading
val section = config.getConfigurationSection("players")
section?.getKeys(false)?.forEach { playerName ->
    val expiry = section.get("$playerName.expiry") as? Long  // null if missing or explicitly null
    entries[playerName] = WhitelistEntry(playerName, expiry)
}
```

### Pattern 3: Hardcoded Messages with MiniMessage Formatting
**What:** Define messages as constants, use MessageFormat.applyStyles() for coloring
**When to use:** After removing locale system, for all user-facing strings
**Example:**
```kotlin
// In a Messages object or directly in commands
object Messages {
    private fun styled(raw: String) = MessageFormat.applyStyles(raw)

    val noPermission = styled("<comfy><warning>You do not have permission to use this command.</warning>")
    val playerAdded = styled("<comfy>Player <success><name></success> has been <success>added</success> to the whitelist.")
    // ... etc
}
```

### Anti-Patterns to Avoid
- **Storing timestamps as formatted strings:** Hard to compare, timezone issues, parsing overhead
- **Using Date objects in YAML:** YAML serialization is inconsistent across implementations
- **Checking expiry on every method call:** Only check on `isPlayerWhitelisted()`, not on add/remove
- **Modifying external API interface:** The `Storage` interface from `comfy-whitelist-api` should NOT be changed

## Don't Hand-Roll

Problems that look simple but have existing solutions:

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Timestamp representation | Custom date/time class | `Long` epoch milliseconds | Standard, simple, no parsing |
| YAML sections | Manual string building | `config.getConfigurationSection()` | Handles nesting, type conversion |
| Color formatting | Manual color codes | `MessageFormat.applyStyles()` | Already handles MiniMessage tags |
| Time duration parsing | Custom parser | Built-in or simple multipliers | Phase 2 concern, not this phase |

**Key insight:** The Bukkit YamlConfiguration API already handles nested sections, null values, and type conversion. Don't fight it with custom serialization.

## Common Pitfalls

### Pitfall 1: Breaking the External API Interface
**What goes wrong:** Changing `Storage` interface methods breaks dependent plugins
**Why it happens:** Interface is from `comfy-whitelist-api` external library
**How to avoid:** Keep existing interface methods working, add new internal methods separately
**Warning signs:** Any modification to method signatures in `YamlStorage` that would break `Storage` interface

### Pitfall 2: Timezone Confusion with Timestamps
**What goes wrong:** Timestamps interpreted differently on different servers
**Why it happens:** Using formatted date strings or LocalDateTime without timezone
**How to avoid:** Always use epoch milliseconds (UTC by definition), compare with `System.currentTimeMillis()`
**Warning signs:** Any use of `SimpleDateFormat`, `DateTimeFormatter`, or `LocalDateTime`

### Pitfall 3: YAML Null vs Missing Key
**What goes wrong:** `null` expiry stored as literal "null" string, or missing key causes NPE
**Why it happens:** YAML configuration has specific null handling
**How to avoid:** Use `config.get("key") as? Long` which returns null for both missing and null values
**Warning signs:** Explicit null checks that don't account for missing keys

### Pitfall 4: Case Sensitivity in Player Names
**What goes wrong:** "Notch" and "notch" treated as different players
**Why it happens:** Minecraft usernames are case-insensitive but Map keys are case-sensitive
**How to avoid:** Store player names as-provided but compare case-insensitively, or normalize to lowercase
**Warning signs:** Using `Map<String, Entry>` with direct `.get()` without normalization

### Pitfall 5: Forgetting to Remove All Locale References
**What goes wrong:** Compile errors or runtime crashes from missing classes
**Why it happens:** Locale system is deeply integrated (config, messages, defaults)
**How to avoid:** Search for all usages of `Locale`, `Message`, `MessageConfig` before deletion
**Warning signs:** Imports of deleted classes, references to `GeneralConfig.locale`

## Code Examples

Verified patterns from official sources:

### WhitelistEntry Data Class
```kotlin
// New file: src/main/kotlin/com/cocahonka/comfywhitelist/storage/WhitelistEntry.kt
package com.cocahonka.comfywhitelist.storage

/**
 * Represents a whitelist entry with optional expiry.
 *
 * @property playerName The player's Minecraft username
 * @property expiryTimestamp Epoch milliseconds when entry expires, null for permanent
 */
data class WhitelistEntry(
    val playerName: String,
    val expiryTimestamp: Long?
) {
    /**
     * Returns true if entry is valid (permanent or not yet expired).
     */
    fun isValid(): Boolean = expiryTimestamp == null || System.currentTimeMillis() < expiryTimestamp

    /**
     * Returns true if this is a permanent (non-expiring) entry.
     */
    fun isPermanent(): Boolean = expiryTimestamp == null

    /**
     * Returns remaining time in milliseconds, or null if permanent.
     */
    fun remainingTimeMillis(): Long? = expiryTimestamp?.let { it - System.currentTimeMillis() }
}
```

### Modified YamlStorage Key Methods
```kotlin
// Source: Bukkit Configuration API patterns
// Modified from existing YamlStorage.kt

private val whitelistedPlayers: MutableMap<String, WhitelistEntry> = mutableMapOf()

override fun addPlayer(username: String): Boolean {
    // Existing API: add permanently (maintains backward compatibility)
    return addPlayerWithExpiry(username, null)
}

fun addPlayerWithExpiry(username: String, expiryTimestamp: Long?): Boolean {
    val normalized = username.lowercase()
    val existing = whitelistedPlayers[normalized]
    if (existing != null && existing.playerName == username && existing.expiryTimestamp == expiryTimestamp) {
        return false // No change needed
    }
    whitelistedPlayers[normalized] = WhitelistEntry(username, expiryTimestamp)
    return save()
}

override fun isPlayerWhitelisted(username: String): Boolean {
    val entry = whitelistedPlayers[username.lowercase()]
    return entry?.isValid() == true
}

override fun load(): Boolean {
    return try {
        config.load(storageFile)
        whitelistedPlayers.clear()

        val section = config.getConfigurationSection(PLAYERS_KEY)
        section?.getKeys(false)?.forEach { playerName ->
            val expiry = section.get("$playerName.$EXPIRY_KEY") as? Long
            whitelistedPlayers[playerName.lowercase()] = WhitelistEntry(playerName, expiry)
        }
        true
    } catch (e: Exception) {
        getLogger().warning(e.stackTraceToString())
        false
    }
}

override fun save(): Boolean {
    return try {
        config.set(PLAYERS_KEY, null) // Clear section
        whitelistedPlayers.values.forEach { entry ->
            config.set("$PLAYERS_KEY.${entry.playerName}.$EXPIRY_KEY", entry.expiryTimestamp)
        }
        config.save(storageFile)
        true
    } catch (e: Exception) {
        getLogger().warning(e.stackTraceToString())
        false
    }
}

companion object {
    private const val PLAYERS_KEY = "players"
    private const val EXPIRY_KEY = "expiry"
    private const val FILE_NAME = "whitelist.yml"
}
```

### Messages Object (Replacing Locale System)
```kotlin
// New content for MessageConfig.kt or new Messages.kt object
package com.cocahonka.comfywhitelist.config.message

import net.kyori.adventure.text.Component

/**
 * All plugin messages in English.
 * Uses MessageFormat.applyStyles() for MiniMessage formatting.
 */
object Messages {
    private fun styled(raw: String): Component = MessageFormat.applyStyles(raw)

    // General messages
    val noPermission = styled("<comfy><warning>You do not have permission to use this command.</warning>")
    val inactiveCommand = styled("<comfy>This command is <off>disabled</off> via config.")
    val invalidUsage = styled("<comfy><warning>Invalid command usage.</warning>\nUse: <usage>")
    val unknownSubcommand = styled("<comfy><warning>Unknown subcommand.</warning> Type /comfywl help for a list of commands.")
    val invalidPlayerName = styled("<comfy><warning>Invalid player name.</warning>")
    val pluginReloaded = styled("<comfy>ComfyWhitelist <success>has been successfully reloaded.</success>")

    // Whitelist status messages
    val whitelistEnabled = styled("<comfy>ComfyWhitelist <success>enabled.</success>")
    val whitelistDisabled = styled("<comfy>ComfyWhitelist <off>disabled.</off>")
    val whitelistAlreadyEnabled = styled("<comfy>ComfyWhitelist <success>already enabled.</success>")
    val whitelistAlreadyDisabled = styled("<comfy>ComfyWhitelist <off>already disabled.</off>")

    // Player management messages
    val notWhitelisted = styled("<warning>You are not whitelisted.</warning>")
    val playerAdded = styled("<comfy>Player <success><name></success> has been <success>added</success> to the whitelist.")
    val playerRemoved = styled("<comfy>Player <remove><name></remove> has been <remove>removed</remove> from the whitelist.")
    val nonExistentPlayerName = styled("<comfy>There is <warning>no</warning> player named <warning><name></warning> in the whitelist.")

    // Whitelist display messages
    val whitelistedPlayersList = styled("<comfy>Whitelisted players: <success><players></success>")
    val emptyWhitelistedPlayersList = styled("<comfy>Whitelist is <off>empty.</off>")
    val whitelistCleared = styled("<comfy>All players have been <remove>removed</remove> from the whitelist.")
}
```

## Files to Modify/Create/Delete

### CREATE
| File | Purpose |
|------|---------|
| `src/main/kotlin/.../storage/WhitelistEntry.kt` | Data class for whitelist entries |

### MODIFY
| File | Changes |
|------|---------|
| `src/main/kotlin/.../storage/YamlStorage.kt` | Change `Set<String>` to `Map<String, WhitelistEntry>`, update load/save |
| `src/main/kotlin/.../config/general/GeneralConfig.kt` | Remove `locale` property and related code |
| `src/main/kotlin/.../config/message/MessageFormat.kt` | Keep as-is (still needed for styling) |
| `src/main/kotlin/.../ComfyWhitelist.kt` | Remove MessageConfig instantiation, simplify loadConfigs() |
| `src/main/resources/config.yml` | Remove `locale` setting |
| All commands using `MessageConfig.*` | Change to `Messages.*` |
| `src/main/kotlin/.../listeners/PlayerPreLoginEvent.kt` | Change to `Messages.notWhitelisted` |

### DELETE
| File | Reason |
|------|--------|
| `src/main/kotlin/.../config/base/Locale.kt` | Locale enum no longer needed |
| `src/main/kotlin/.../config/message/Message.kt` | Sealed class replaced by Messages object |
| `src/main/kotlin/.../config/message/MessageConfig.kt` | Locale-loading config manager not needed |
| `src/main/resources/locales/` (entire directory, 17 files) | No more locale files |

### TEST FILES
| File | Changes |
|------|---------|
| `src/test/kotlin/.../storage/StorageTestBase.kt` | Add tests for expiry functionality |
| `src/test/kotlin/.../storage/YamlStorageTest.kt` | May need updates for new storage format |

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `Set<String>` for players | `Map<String, WhitelistEntry>` | This phase | Supports expiry metadata |
| 17-locale system | English-only Messages object | This phase | Simpler maintenance |
| Locale enum + YAML files | Hardcoded strings | This phase | Removes ~500 lines of code |

**Deprecated/outdated:**
- `Message` sealed class: Being removed - too complex for single-language support
- `MessageConfig`: Being removed - no longer loading locale files
- `Locale` enum: Being removed - not supporting multiple languages

## Open Questions

Things that couldn't be fully resolved:

1. **Case sensitivity for player name lookups**
   - What we know: Minecraft usernames are case-insensitive ("Notch" = "notch")
   - What's unclear: Should we normalize on storage or on lookup?
   - Recommendation: Normalize to lowercase for map keys, preserve original case in entry

2. **External API compatibility for expiry methods**
   - What we know: `Storage` interface is from external library `comfy-whitelist-api`
   - What's unclear: Can we add new methods without breaking the interface?
   - Recommendation: Keep `addPlayer(String)` as permanent-add, add internal `addPlayerWithExpiry` method. Full API update deferred to later.

3. **Migration of existing whitelist.yml files**
   - What we know: Current format is `players: [name1, name2, ...]` (list of strings)
   - What's unclear: Should we auto-migrate or require manual migration?
   - Recommendation: Support both formats on load (detect list vs section), save always in new format

## Sources

### Primary (HIGH confidence)
- Existing codebase analysis: `YamlStorage.kt`, `Message.kt`, `MessageConfig.kt`, `Locale.kt`
- [Bukkit Configuration API Reference](https://bukkit.fandom.com/wiki/Configuration_API_Reference) - YAML section patterns

### Secondary (MEDIUM confidence)
- [comfy-whitelist-api GitHub](https://github.com/cocahonka/comfy-whitelist-api) - Storage interface methods
- [Bukkit YamlConfiguration JavaDoc](https://hub.spigotmc.org/javadocs/spigot/org/bukkit/configuration/file/YamlConfiguration.html) - API details

### Tertiary (LOW confidence)
- WebSearch results on Kotlin timestamp patterns - general best practices

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - using existing project dependencies only
- Architecture: HIGH - clear patterns from existing codebase
- Pitfalls: HIGH - derived from code analysis and Bukkit documentation
- Migration strategy: MEDIUM - need to verify backward compatibility during implementation

**Research date:** 2026-01-30
**Valid until:** 2026-03-01 (stable domain, no external dependencies changing)
