# Codebase Concerns

**Analysis Date:** 2026-01-30

## Tech Debt

**Global Mutable State via Companion Objects:**
- Issue: Configuration values stored in companion object static variables (`GeneralConfig.whitelistEnabled`, `GeneralConfig.locale`, `MessageConfig.*`). This pattern couples all code to global state and makes testing difficult.
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/config/general/GeneralConfig.kt`, `src/main/kotlin/com/cocahonka/comfywhitelist/config/message/MessageConfig.kt`
- Impact: Cannot run isolated unit tests; state leaks between tests. Difficult to support multiple plugin instances. Prevents dependency injection patterns.
- Fix approach: Convert to instance-based configuration that is passed to components via constructor injection. Access config through the plugin instance rather than static access.

**Hardcoded Message Translations in Code:**
- Issue: The `Message.kt` file contains 463 lines of hardcoded translations in a giant sealed class with when-expressions for each locale. Adding a new message requires modifying source code in 17+ places.
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/config/message/Message.kt`
- Impact: Adding new languages or messages is error-prone. Translations cannot be contributed without code changes. File is difficult to maintain.
- Fix approach: Move all default translations to resource files. Load defaults from bundled YAML files rather than compiled-in when-expressions.

**Deprecated API Usage for Testing:**
- Issue: Plugin uses deprecated `JavaPluginLoader` constructor for unit testing purposes, with `@Suppress("removal", "DEPRECATION")` annotations.
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/ComfyWhitelist.kt` (lines 33-41)
- Impact: May break with future Paper/Bukkit API updates. MockBukkit dependency is pinned to old version (v1.19).
- Fix approach: Update to MockBukkit 3.x which uses newer testing patterns without deprecated constructors.

**Kotlin 1.8 and Java 8 Target:**
- Issue: Build targets Java 8 and Kotlin 1.8.10 for broad compatibility, but tests require Java 17.
- Files: `build.gradle.kts` (lines 12-21, 100-104)
- Impact: Cannot use modern Kotlin/Java language features in production code. Split JVM targets create build complexity.
- Fix approach: Consider minimum supported MC version and whether Java 8 target is still necessary. MC 1.17+ requires Java 16+.

## Known Bugs

**Case-Sensitive Whitelist Checking:**
- Symptoms: Player "Steve" can be whitelisted but "steve" or "STEVE" will not match, potentially allowing same player to be added multiple times with different casing.
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/storage/YamlStorage.kt` (line 49), `src/main/kotlin/com/cocahonka/comfywhitelist/listeners/PlayerPreLoginEvent.kt` (line 34)
- Trigger: Add player "Steve", player joins as "steve" - will be rejected even though Minecraft usernames are case-insensitive.
- Workaround: Always use exact casing when adding players, or normalize to lowercase.

**Success Message Sent Before Operation Completes:**
- Symptoms: User sees "Player added to whitelist" message but if file save fails, the player is not actually persisted.
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/AddCommand.kt` (lines 30-33), `src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/RemoveCommand.kt` (lines 37-40)
- Trigger: YAML file write fails due to permissions or disk issues.
- Workaround: None. User believes operation succeeded when it may have failed silently (save() returns false but is ignored).

## Security Considerations

**No Input Length Validation:**
- Risk: Player names only validated against regex `^[a-zA-Z0-9_]+$` but no maximum length check. Malicious input could cause memory issues if processing extremely long strings.
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/commands/SubCommand.kt` (line 63)
- Current mitigation: Minecraft usernames are 3-16 characters, but plugin doesn't enforce this.
- Recommendations: Add length validation (3-16 chars) to match Minecraft username requirements.

**Clear Command Risk:**
- Risk: `/comfywl clear` permanently deletes entire whitelist with no confirmation and no backup.
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/ClearCommand.kt`
- Current mitigation: Command is disabled by default in `config.yml` and requires explicit config change to enable.
- Recommendations: Add confirmation prompt or automatic backup before clear.

**Console Bypasses Permission Checks:**
- Risk: Console command sender bypasses all permission checks entirely.
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/commands/CommandHandler.kt` (line 66)
- Current mitigation: Console is generally trusted on Minecraft servers.
- Recommendations: This is intentional behavior but should be documented.

## Performance Bottlenecks

**Synchronous File I/O on Every Whitelist Operation:**
- Problem: Every add/remove operation immediately saves to disk synchronously.
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/storage/YamlStorage.kt` (lines 35-41)
- Cause: `save()` called after every `addPlayer()` and `removePlayer()` operation with no batching or async handling.
- Improvement path: Implement dirty-flag and periodic save, or use async file operations. Consider caching with write-behind.

**Full Whitelist Iteration on Tab Complete:**
- Problem: Tab completion for `/comfywl remove` iterates entire whitelist on every keystroke.
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/commands/CommandTabCompleter.kt` (lines 38-40)
- Cause: `storage.allWhitelistedPlayers` returns copy of entire Set, then filters.
- Improvement path: For large whitelists (1000+ players), consider indexed prefix search or result caching.

## Fragile Areas

**AsyncPlayerPreLoginEvent Handler:**
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/listeners/PlayerPreLoginEvent.kt`
- Why fragile: Event handler runs on async thread but accesses `GeneralConfig.whitelistEnabled` static variable and `storage.isPlayerWhitelisted()` which uses non-thread-safe `MutableSet`. Race condition possible if whitelist modified during player join.
- Safe modification: Any changes must consider thread safety. Storage access should be synchronized.
- Test coverage: Tests exist but use `CountDownLatch` timing which may be flaky.

**Message Formatting Chain:**
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/config/message/MessageFormat.kt`, `src/main/kotlin/com/cocahonka/comfywhitelist/config/message/MessageConfig.kt`
- Why fragile: Messages go through multiple transformation steps: YAML load -> MiniMessage parse -> tag resolution -> text replacement -> legacy serialization. Any malformed tag or replacement can cause runtime errors.
- Safe modification: Always test message changes with all custom tags (`<comfy>`, `<warning>`, `<success>`, `<remove>`, `<off>`, `<name>`, `<players>`, `<usage>`).
- Test coverage: Basic tests exist but don't cover all tag combinations.

## Scaling Limits

**In-Memory Whitelist Storage:**
- Current capacity: Entire whitelist held in `MutableSet<String>` in memory.
- Limit: Memory constrained. At ~50 bytes per username (16 chars + object overhead), 100K players ~= 5MB RAM.
- Scaling path: For very large servers, consider database-backed storage with caching.

**Single-File YAML Storage:**
- Current capacity: All players in single `whitelist.yml` file.
- Limit: YAML parsing performance degrades with file size. 10K+ entries may cause noticeable reload times.
- Scaling path: Consider SQLite storage option for large deployments.

## Dependencies at Risk

**MockBukkit Version Pinned to 1.19:**
- Risk: `com.github.seeseemelk:MockBukkit-v1.19:2.29.0` is outdated and may not reflect newer Bukkit API behavior.
- Impact: Tests may pass but fail on newer server versions.
- Migration plan: Update to MockBukkit 3.x which supports dynamic API version matching.

**Paper API 1.13.2 Compile Target:**
- Risk: Compiling against very old API (1.13.2-R0.1-SNAPSHOT) while claiming 1.13-1.20 support.
- Impact: May miss deprecations or API changes in newer versions.
- Migration plan: Consider multi-version compilation or updating minimum supported version.

## Missing Critical Features

**No Whitelist Backup/Export:**
- Problem: No way to backup whitelist before destructive operations.
- Blocks: Safe migration, disaster recovery.

**No Bulk Import:**
- Problem: Adding many players requires individual commands.
- Blocks: Migration from other whitelist plugins, large server setup.

## Test Coverage Gaps

**Thread Safety Not Tested:**
- What's not tested: Concurrent access to storage from multiple threads (async login event + command execution).
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/storage/YamlStorage.kt`
- Risk: Race conditions in production could cause data corruption or inconsistent reads.
- Priority: High - this is a real production scenario.

**File I/O Failure Paths:**
- What's not tested: Behavior when config/whitelist files cannot be read or written.
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/storage/YamlStorage.kt`, `src/main/kotlin/com/cocahonka/comfywhitelist/config/base/ConfigManager.kt`
- Risk: Silent failures or crashes when disk is full or permissions wrong.
- Priority: Medium - affects reliability.

**Tab Completion:**
- What's not tested: `CommandTabCompleter` has no test coverage.
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/commands/CommandTabCompleter.kt`
- Risk: Tab completion regressions undetected.
- Priority: Low - user experience only.

**StatusCommand:**
- What's not tested: `StatusCommand` has no dedicated test file.
- Files: `src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/StatusCommand.kt`
- Risk: Status command regressions undetected.
- Priority: Low - simple command.

---

*Concerns audit: 2026-01-30*
