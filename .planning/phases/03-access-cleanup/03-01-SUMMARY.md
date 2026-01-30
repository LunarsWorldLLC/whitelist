---
phase: 03-access-cleanup
plan: 01
subsystem: auth
tags: [bukkit, scheduler, expiry, whitelist, cleanup-task]

# Dependency graph
requires:
  - phase: 01-storage
    provides: YamlStorage with WhitelistEntry expiry support
  - phase: 02-commands
    provides: Commands using YamlStorage for expiry operations
provides:
  - Expiry-aware access control tests confirming expired players blocked
  - removeExpiredEntries() method for storage cleanup
  - ExpiredEntriesCleanupTask scheduled background cleanup
  - Configurable cleanup interval via config.yml
affects: []

# Tech tracking
tech-stack:
  added: []
  patterns:
    - BukkitRunnable for scheduled tasks
    - Configurable interval with coerceAtLeast validation

key-files:
  created:
    - src/main/kotlin/com/cocahonka/comfywhitelist/tasks/ExpiredEntriesCleanupTask.kt
    - src/test/kotlin/com/cocahonka/comfywhitelist/tasks/ExpiredEntriesCleanupTaskTest.kt
  modified:
    - src/test/kotlin/com/cocahonka/comfywhitelist/listeners/PlayerPreLoginEventTest.kt
    - src/main/kotlin/com/cocahonka/comfywhitelist/storage/YamlStorage.kt
    - src/main/kotlin/com/cocahonka/comfywhitelist/config/general/GeneralConfig.kt
    - src/main/resources/config.yml
    - src/main/kotlin/com/cocahonka/comfywhitelist/ComfyWhitelist.kt

key-decisions:
  - "Cleanup task uses BukkitRunnable.runTaskTimer with configurable interval"
  - "Minimum cleanup interval enforced at 1 hour via coerceAtLeast"
  - "Task logs removal count only when entries were actually removed"

patterns-established:
  - "Tasks package for scheduled background operations"
  - "Config properties with companion object and Delegates.notNull()"

# Metrics
duration: 4min
completed: 2026-01-30
---

# Phase 3 Plan 1: Access Cleanup Summary

**Expiry-aware access control tests, background cleanup task with configurable interval, and removeExpiredEntries storage method**

## Performance

- **Duration:** 4 min 18 sec
- **Started:** 2026-01-30T15:17:46Z
- **Completed:** 2026-01-30T15:22:04Z
- **Tasks:** 3
- **Files modified:** 7

## Accomplishments
- Added 3 tests verifying expiry-aware access control (expired blocked, valid allowed, permanent allowed)
- Implemented removeExpiredEntries() method in YamlStorage with save persistence
- Created ExpiredEntriesCleanupTask BukkitRunnable for scheduled cleanup
- Added cleanupIntervalHours config property with 1-hour default
- Wired cleanup task into plugin lifecycle (start on enable, cancel on disable)

## Task Commits

Each task was committed atomically:

1. **Task 1: Verify expiry-aware access control with tests** - `19257b5` (test)
2. **Task 2: Implement cleanup task and storage method** - `aa33651` (feat)
3. **Task 3: Wire cleanup task into plugin lifecycle with config** - `0825bbb` (feat)

## Files Created/Modified
- `src/test/kotlin/com/cocahonka/comfywhitelist/listeners/PlayerPreLoginEventTest.kt` - Added 3 expiry tests
- `src/main/kotlin/com/cocahonka/comfywhitelist/storage/YamlStorage.kt` - Added removeExpiredEntries() method
- `src/main/kotlin/com/cocahonka/comfywhitelist/tasks/ExpiredEntriesCleanupTask.kt` - New cleanup task class
- `src/test/kotlin/com/cocahonka/comfywhitelist/tasks/ExpiredEntriesCleanupTaskTest.kt` - Cleanup task tests
- `src/main/kotlin/com/cocahonka/comfywhitelist/config/general/GeneralConfig.kt` - Added cleanupIntervalHours
- `src/main/resources/config.yml` - Added cleanup-interval-hours setting
- `src/main/kotlin/com/cocahonka/comfywhitelist/ComfyWhitelist.kt` - Wired cleanup task lifecycle

## Decisions Made
- Used BukkitRunnable.runTaskTimer for scheduled cleanup (standard Bukkit approach)
- Cleanup interval minimum enforced at 1 hour to prevent excessive file I/O
- Task only logs when entries were actually removed (reduces log noise)
- Cleanup task uses delay equal to interval (no immediate run on startup)

## Deviations from Plan

None - plan executed exactly as written.

## Issues Encountered

None - all tests passed on first run, build succeeded.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness
- Plugin is now fully functional with expiring whitelist entries
- Access control blocks expired players
- Background task cleans up expired entries automatically
- All Phase 3 requirements (ACC-01, ACC-02, CLN-01, CLN-02) verified

---
*Phase: 03-access-cleanup*
*Completed: 2026-01-30*
