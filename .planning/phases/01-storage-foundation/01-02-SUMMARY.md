---
phase: 01-storage-foundation
plan: 02
subsystem: config
tags: [minimessage, messages, localization, kotlin]

# Dependency graph
requires:
  - phase: 01-01
    provides: expiry-aware storage system
provides:
  - Hardcoded English messages in Messages object
  - Simplified config without locale setting
  - Removed 17 locale files and Message sealed class
affects: [commands, ui]

# Tech tracking
tech-stack:
  added: []
  patterns:
    - Single Messages object for all plugin text

key-files:
  created:
    - src/main/kotlin/com/cocahonka/comfywhitelist/config/message/Messages.kt
  modified:
    - src/main/kotlin/com/cocahonka/comfywhitelist/config/message/MessageFormat.kt
    - src/main/kotlin/com/cocahonka/comfywhitelist/config/general/GeneralConfig.kt
    - src/main/kotlin/com/cocahonka/comfywhitelist/ComfyWhitelist.kt
    - src/main/resources/config.yml
    - All command files (SubCommand, CommandHandler, all sub-commands)
    - src/main/kotlin/com/cocahonka/comfywhitelist/listeners/PlayerPreLoginEvent.kt

key-decisions:
  - "Messages object with hardcoded English strings"
  - "Move prefixComponent to MessageFormat to eliminate Message dependency"
  - "Keep MessageFormat for MiniMessage custom tag styling"

patterns-established:
  - "All plugin messages accessed via Messages.propertyName"
  - "No locale files or runtime message loading"

# Metrics
duration: 12min
completed: 2026-01-30
---

# Phase 1 Plan 2: Remove Locale System Summary

**Replaced 17-locale system with single Messages object containing hardcoded English strings using MiniMessage formatting**

## Performance

- **Duration:** 12 min
- **Started:** 2026-01-30T14:04:58Z
- **Completed:** 2026-01-30T14:16:46Z
- **Tasks:** 3
- **Files modified:** 42

## Accomplishments
- Created Messages.kt object with all English messages as Component properties
- Removed Locale enum, Message sealed class, and MessageConfig class
- Deleted all 17 locale YAML files
- Removed locale property from GeneralConfig
- Updated all commands and listeners to use Messages instead of MessageConfig
- Restored and fixed all command tests to use Messages

## Task Commits

Each task was committed atomically:

1. **Task 1: Create Messages object** - `0cfbf9a` (feat)
2. **Task 2: Remove locale from GeneralConfig and ComfyWhitelist** - `4a25f58` (feat)
3. **Task 3: Delete locale system files and update references** - `c1707d6` (refactor)

## Files Created/Modified
- `src/main/kotlin/com/cocahonka/comfywhitelist/config/message/Messages.kt` - New Messages object with all English messages
- `src/main/kotlin/com/cocahonka/comfywhitelist/config/message/MessageFormat.kt` - Added prefixComponent, removed Message dependency
- `src/main/kotlin/com/cocahonka/comfywhitelist/config/general/GeneralConfig.kt` - Removed locale property
- `src/main/kotlin/com/cocahonka/comfywhitelist/ComfyWhitelist.kt` - Removed MessageConfig instantiation
- `src/main/resources/config.yml` - Removed locale setting
- All command files - Updated to use Messages.* instead of MessageConfig.*
- `src/main/kotlin/com/cocahonka/comfywhitelist/listeners/PlayerPreLoginEvent.kt` - Uses Messages.notWhitelisted
- Test files - Updated all command tests to use Messages

## Decisions Made
- **Move prefixComponent to MessageFormat** - Since Message.kt is being deleted, the prefix needed a new home. MessageFormat was chosen as it already handles formatting.
- **Keep MessageFormat.kt** - Still needed for MiniMessage custom tag styling (comfy, warning, success, etc.)
- **Recreate deleted tests** - Previous plan deleted some command tests that referenced Message class; recreated with Messages references

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] MessageFormat dependency on Message.prefixComponent**
- **Found during:** Task 1 (Creating Messages.kt)
- **Issue:** MessageFormat.Placeholders.prefix referenced Message.prefixComponent which would be deleted
- **Fix:** Moved prefixComponent definition to MessageFormat.kt
- **Files modified:** src/main/kotlin/com/cocahonka/comfywhitelist/config/message/MessageFormat.kt
- **Verification:** Compiles without errors
- **Committed in:** 0cfbf9a

**2. [Rule 1 - Bug] Invalid player name test using valid name**
- **Found during:** Task 3 (Fixing tests)
- **Issue:** Test used "motuzok" which matches player name regex (ASCII alphanumeric + underscore)
- **Fix:** Changed to use "invalid-name!" which contains invalid characters
- **Files modified:** AddCommandTest.kt, RemoveCommandTest.kt
- **Verification:** Tests pass
- **Committed in:** c1707d6

**3. [Rule 1 - Bug] GeneralConfigTest expecting wrong default**
- **Found during:** Task 3 (Running tests)
- **Issue:** Test expected whitelistEnabled=true but bundled config.yml has enabled: false
- **Fix:** Updated test to expect false (matches bundled config)
- **Files modified:** GeneralConfigTest.kt
- **Verification:** Test passes
- **Committed in:** c1707d6

---

**Total deviations:** 3 auto-fixed (1 blocking, 2 bugs)
**Impact on plan:** All fixes necessary for correct operation. No scope creep.

## Issues Encountered
None - plan executed smoothly after handling the deviations above.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Phase 1 complete - storage foundation with simplified English-only messages
- Ready for Phase 2: Commands implementation
- All tests pass (55 tests)

---
*Phase: 01-storage-foundation*
*Completed: 2026-01-30*
