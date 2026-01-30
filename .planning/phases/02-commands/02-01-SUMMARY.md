---
phase: 02-commands
plan: 01
subsystem: commands
tags: [kotlin, bukkit, whitelist, expiry, commands]

# Dependency graph
requires:
  - phase: 01-storage
    provides: WhitelistEntry data class, YamlStorage with expiry methods
provides:
  - AddCommand with 31-day expiry and extend functionality
  - ListCommand with days remaining display
  - CheckCommand for checking player whitelist status
  - Message infrastructure for expiry operations
affects: [03-background]

# Tech tracking
tech-stack:
  added: []
  patterns: [command-expiry-pattern, days-formatting-helper]

key-files:
  created:
    - src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/CheckCommand.kt
  modified:
    - src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/AddCommand.kt
    - src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/ListCommand.kt
    - src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/RemoveCommand.kt
    - src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/ClearCommand.kt
    - src/main/kotlin/com/cocahonka/comfywhitelist/commands/CommandHandler.kt
    - src/main/kotlin/com/cocahonka/comfywhitelist/storage/YamlStorage.kt
    - src/main/kotlin/com/cocahonka/comfywhitelist/config/message/Messages.kt
    - src/main/kotlin/com/cocahonka/comfywhitelist/config/message/MessageFormat.kt

key-decisions:
  - "All commands use YamlStorage instead of Storage interface for expiry access"
  - "AddCommand always sets 31-day expiry from current time (extends existing)"
  - "Days remaining helper duplicated in ListCommand and CheckCommand for simplicity"

patterns-established:
  - "formatDaysRemaining helper: permanent/0 days/singular day/plural days"
  - "Expiry constants: EXPIRY_DAYS=31, MILLIS_PER_DAY=24*60*60*1000"

# Metrics
duration: 3min
completed: 2026-01-30
---

# Phase 02 Plan 01: Expiry-aware Commands Summary

**AddCommand with 31-day auto-expiry, ListCommand with days remaining display, new CheckCommand for player status lookup**

## Performance

- **Duration:** 3 min
- **Started:** 2026-01-30T14:36:06Z
- **Completed:** 2026-01-30T14:38:35Z
- **Tasks:** 2
- **Files modified:** 9

## Accomplishments
- AddCommand creates entries with 31-day expiry from current time
- AddCommand extends existing valid entries (sends "extended" message)
- ListCommand displays "PlayerName (X days remaining)" format
- ListCommand shows "permanent" for null-expiry entries
- CheckCommand shows specific player's remaining time or "not found"
- All storage-using commands now use YamlStorage type for expiry access

## Task Commits

Each task was committed atomically:

1. **Task 1: Add storage method and message infrastructure** - `c06471f` (feat)
2. **Task 2: Modify commands and create CheckCommand** - `1dbd5d1` (feat)

## Files Created/Modified
- `src/main/kotlin/com/cocahonka/comfywhitelist/storage/YamlStorage.kt` - Added getAllValidEntries() method
- `src/main/kotlin/com/cocahonka/comfywhitelist/config/message/Messages.kt` - Added playerExpiryExtended and playerCheckResult messages
- `src/main/kotlin/com/cocahonka/comfywhitelist/config/message/MessageFormat.kt` - Added daysReplacementConfigBuilder
- `src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/AddCommand.kt` - 31-day expiry, extend logic, YamlStorage type
- `src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/ListCommand.kt` - Days remaining format, YamlStorage type
- `src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/RemoveCommand.kt` - YamlStorage type for consistency
- `src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/ClearCommand.kt` - YamlStorage type for consistency
- `src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/CheckCommand.kt` - New command for player status
- `src/main/kotlin/com/cocahonka/comfywhitelist/commands/CommandHandler.kt` - YamlStorage type, registers CheckCommand

## Decisions Made
- All commands changed to use YamlStorage instead of Storage interface to access expiry-specific methods (getEntry, getAllValidEntries, addPlayerWithExpiry)
- formatDaysRemaining helper logic duplicated in ListCommand and CheckCommand - small duplication acceptable for command isolation

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Updated CommandHandler and ClearCommand to use YamlStorage**
- **Found during:** Task 2 (compile verification)
- **Issue:** CommandHandler passed Storage type but commands now require YamlStorage
- **Fix:** Changed CommandHandler and ClearCommand to use YamlStorage type, added CheckCommand to command list
- **Files modified:** CommandHandler.kt, ClearCommand.kt
- **Verification:** Project compiles successfully
- **Committed in:** 1dbd5d1 (Task 2 commit)

---

**Total deviations:** 1 auto-fixed (blocking)
**Impact on plan:** Auto-fix necessary for compilation. All commands now consistently use YamlStorage.

## Issues Encountered
None

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Command layer complete with expiry support
- Ready for Phase 3: Background expiry checker task
- CheckCommand permission (comfywhitelist.check) should be documented in plugin.yml if not already present

---
*Phase: 02-commands*
*Completed: 2026-01-30*
