# Architecture

**Analysis Date:** 2026-01-30

## Pattern Overview

**Overall:** Plugin Architecture with Command Pattern and Service Locator

**Key Characteristics:**
- Bukkit/Paper plugin extending `JavaPlugin` as the central orchestrator
- Command Pattern for handling subcommands via `SubCommand` interface
- Static companion objects for configuration state (singleton-like access)
- External API library (`comfy-whitelist-api`) defining core interfaces
- Localization through enum-based locale system with YAML message files

## Layers

**Plugin Layer:**
- Purpose: Entry point and lifecycle management for the Bukkit plugin
- Location: `src/main/kotlin/com/cocahonka/comfywhitelist/ComfyWhitelist.kt`
- Contains: Plugin initialization, dependency wiring, event/command registration
- Depends on: All other layers
- Used by: Bukkit server runtime

**Command Layer:**
- Purpose: Handle player/console commands and dispatch to subcommands
- Location: `src/main/kotlin/com/cocahonka/comfywhitelist/commands/`
- Contains: `CommandHandler`, `CommandTabCompleter`, `SubCommand` interface, subcommand implementations
- Depends on: Storage layer, Config layer, API interfaces
- Used by: Plugin layer (registered as command executor)

**Config Layer:**
- Purpose: Manage plugin configuration and localized messages
- Location: `src/main/kotlin/com/cocahonka/comfywhitelist/config/`
- Contains: `ConfigManager` base class, `GeneralConfig`, `MessageConfig`, `Locale` enum, `Message` sealed class
- Depends on: Bukkit configuration API, Kyori Adventure API
- Used by: Command layer, Listener layer

**Storage Layer:**
- Purpose: Persist and retrieve whitelist data
- Location: `src/main/kotlin/com/cocahonka/comfywhitelist/storage/`
- Contains: `YamlStorage` implementing external `Storage` interface
- Depends on: Bukkit YAML configuration API, API interfaces
- Used by: Command layer, Listener layer

**Listener Layer:**
- Purpose: Handle Bukkit events (player login)
- Location: `src/main/kotlin/com/cocahonka/comfywhitelist/listeners/`
- Contains: `PlayerPreLoginEvent` listener
- Depends on: Storage layer, Config layer
- Used by: Plugin layer (registered as event listener)

**Utility Layer:**
- Purpose: Cross-cutting utilities
- Location: `src/main/kotlin/com/cocahonka/comfywhitelist/LegacyUtils.kt`
- Contains: Extension functions for Component serialization
- Depends on: Kyori Adventure API
- Used by: Command layer, Listener layer

## Data Flow

**Player Login Check:**

1. Player attempts to join server
2. `AsyncPlayerPreLoginEvent` fires
3. `PlayerPreLoginEvent.onPlayerPreLoginEvent()` checks `GeneralConfig.whitelistEnabled`
4. If enabled, checks `Storage.isPlayerWhitelisted(playerName)`
5. If not whitelisted, disallows login with `KICK_WHITELIST` result and localized message

**Command Execution:**

1. Player/console executes `/comfywl <subcommand> [args]`
2. `CommandHandler.onCommand()` receives command
3. Finds matching `SubCommand` by identifier
4. Validates sender permission
5. Delegates to `SubCommand.execute()` with remaining args
6. SubCommand interacts with `Storage` or `GeneralConfig` as needed
7. Sends localized response via `MessageConfig`

**State Management:**
- Whitelist enabled/disabled state stored in `GeneralConfig.whitelistEnabled` (static companion property)
- Player list stored in `YamlStorage.whitelistedPlayers` (in-memory Set, persisted to YAML)
- Messages stored as static `Component` properties in `MessageConfig.Companion`

## Key Abstractions

**SubCommand Interface:**
- Purpose: Defines contract for plugin subcommands
- Examples: `src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/AddCommand.kt`, `RemoveCommand.kt`, `ListCommand.kt`
- Pattern: Command Pattern with common validation helper `isInvalidUsage()`

**ConfigManager Abstract Class:**
- Purpose: Template for loading YAML configurations
- Examples: `src/main/kotlin/com/cocahonka/comfywhitelist/config/general/GeneralConfig.kt`, `MessageConfig.kt`
- Pattern: Template Method (`createConfig()` and `updateProperties()` hooks)

**Message Sealed Class:**
- Purpose: Type-safe message definitions with locale-specific defaults
- Examples: `src/main/kotlin/com/cocahonka/comfywhitelist/config/message/Message.kt` (contains `NoPermission`, `PlayerAdded`, etc.)
- Pattern: Sealed class hierarchy with `getDefault(locale)` polymorphism

**Storage Interface (External API):**
- Purpose: Abstract whitelist data operations
- Location: External library `com.github.cocahonka:comfy-whitelist-api`
- Implementation: `src/main/kotlin/com/cocahonka/comfywhitelist/storage/YamlStorage.kt`
- Pattern: Repository Pattern

**Locale Enum:**
- Purpose: Enumerate supported locales with file path mapping
- Location: `src/main/kotlin/com/cocahonka/comfywhitelist/config/base/Locale.kt`
- Pattern: Enum with properties and factory method `fromString()`

## Entry Points

**Plugin Enable:**
- Location: `src/main/kotlin/com/cocahonka/comfywhitelist/ComfyWhitelist.kt` (`onEnable()`)
- Triggers: Server startup or plugin reload
- Responsibilities: Load configs, load storage, register events, register commands, emit API

**Command Execution:**
- Location: `src/main/kotlin/com/cocahonka/comfywhitelist/commands/CommandHandler.kt` (`onCommand()`)
- Triggers: `/comfywhitelist` or `/comfywl` command
- Responsibilities: Parse subcommand, validate permissions, delegate execution

**Player Login:**
- Location: `src/main/kotlin/com/cocahonka/comfywhitelist/listeners/PlayerPreLoginEvent.kt` (`onPlayerPreLoginEvent()`)
- Triggers: `AsyncPlayerPreLoginEvent` (player connecting)
- Responsibilities: Check whitelist status, allow/deny login

## Error Handling

**Strategy:** Return-based flow control with user feedback

**Patterns:**
- Commands return `Boolean` indicating success/failure
- Invalid usage detected via `isInvalidUsage()` helper which sends message and returns early
- Storage operations catch exceptions, log warnings, and return `false`
- Missing locale files fall back to English with console warning

## Cross-Cutting Concerns

**Logging:** Bukkit logger accessed via `Bukkit.getLogger()` for warnings (e.g., invalid locale, storage errors)

**Validation:**
- Player name validation via regex `^[a-zA-Z0-9_]+$` in `SubCommand.Companion`
- Command argument validation via `isInvalidUsage()` in each subcommand

**Authentication:** Bukkit permission system checked via `sender.hasPermission(permission)` in `CommandHandler`

**Localization:** MiniMessage format with custom tags (`<comfy>`, `<warning>`, `<success>`, `<remove>`, `<off>`) resolved via `MessageFormat`

---

*Architecture analysis: 2026-01-30*
