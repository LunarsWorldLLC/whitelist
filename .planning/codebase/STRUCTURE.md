# Codebase Structure

**Analysis Date:** 2026-01-30

## Directory Layout

```
whitelist/
├── src/
│   ├── main/
│   │   ├── kotlin/com/cocahonka/comfywhitelist/
│   │   │   ├── commands/              # Command handling
│   │   │   │   └── sub/               # Subcommand implementations
│   │   │   ├── config/                # Configuration management
│   │   │   │   ├── base/              # Base config classes
│   │   │   │   ├── general/           # General plugin config
│   │   │   │   └── message/           # Message/localization config
│   │   │   ├── listeners/             # Bukkit event listeners
│   │   │   ├── storage/               # Data persistence
│   │   │   ├── ComfyWhitelist.kt      # Main plugin class
│   │   │   └── LegacyUtils.kt         # Utility functions
│   │   └── resources/
│   │       ├── locales/               # Localized message files
│   │       ├── config.yml             # Default configuration
│   │       └── plugin.yml             # Plugin descriptor
│   └── test/
│       └── kotlin/com/cocahonka/comfywhitelist/
│           ├── commands/              # Command tests
│           │   └── sub/               # Subcommand tests
│           ├── config/                # Config tests
│           ├── listeners/             # Listener tests
│           └── storage/               # Storage tests
├── gradle/                            # Gradle wrapper
├── build.gradle.kts                   # Build configuration
├── settings.gradle.kts                # Project settings
├── gradle.properties                  # Gradle properties
└── README.MD                          # Project documentation
```

## Directory Purposes

**`src/main/kotlin/com/cocahonka/comfywhitelist/`:**
- Purpose: Main plugin source code
- Contains: All Kotlin source files
- Key files: `ComfyWhitelist.kt` (plugin entry point), `LegacyUtils.kt` (utilities)

**`src/main/kotlin/.../commands/`:**
- Purpose: Command handling infrastructure
- Contains: `CommandHandler.kt`, `CommandTabCompleter.kt`, `SubCommand.kt` interface
- Key files: `CommandHandler.kt` (main command executor)

**`src/main/kotlin/.../commands/sub/`:**
- Purpose: Individual subcommand implementations
- Contains: One file per subcommand
- Key files: `AddCommand.kt`, `RemoveCommand.kt`, `ListCommand.kt`, `EnableCommand.kt`, `DisableCommand.kt`, `ClearCommand.kt`, `ReloadCommand.kt`, `StatusCommand.kt`, `HelpCommand.kt`

**`src/main/kotlin/.../config/`:**
- Purpose: Configuration system root
- Contains: Subdirectories for different config types
- Key files: None directly (see subdirectories)

**`src/main/kotlin/.../config/base/`:**
- Purpose: Base configuration classes and enums
- Contains: Abstract config manager, locale enum
- Key files: `ConfigManager.kt` (template), `Locale.kt` (17 supported locales)

**`src/main/kotlin/.../config/general/`:**
- Purpose: General plugin configuration
- Contains: Main config handler
- Key files: `GeneralConfig.kt` (enabled state, locale, clear-command setting)

**`src/main/kotlin/.../config/message/`:**
- Purpose: Localized message handling
- Contains: Message definitions, formatting, config loader
- Key files: `Message.kt` (sealed class with all messages), `MessageConfig.kt` (loader), `MessageFormat.kt` (MiniMessage styling)

**`src/main/kotlin/.../listeners/`:**
- Purpose: Bukkit event listeners
- Contains: Event handler classes
- Key files: `PlayerPreLoginEvent.kt` (whitelist enforcement)

**`src/main/kotlin/.../storage/`:**
- Purpose: Data persistence layer
- Contains: Storage implementations
- Key files: `YamlStorage.kt` (YAML-based whitelist storage)

**`src/main/resources/`:**
- Purpose: Plugin resources bundled in JAR
- Contains: Configuration files, localization files
- Key files: `config.yml`, `plugin.yml`

**`src/main/resources/locales/`:**
- Purpose: Localized message files
- Contains: 17 language files (en, ru, de, es, fr, it, ja, ko, nl, pt, sv, tr, zh, uk, be, komi, lolcat)
- Key files: `messages_en.yml` (English, primary locale)

**`src/test/kotlin/com/cocahonka/comfywhitelist/`:**
- Purpose: Unit tests mirroring main source structure
- Contains: Test classes for each component
- Key files: Tests organized by feature area

## Key File Locations

**Entry Points:**
- `src/main/kotlin/com/cocahonka/comfywhitelist/ComfyWhitelist.kt`: Plugin main class
- `src/main/resources/plugin.yml`: Bukkit plugin descriptor

**Configuration:**
- `src/main/resources/config.yml`: Default configuration template
- `src/main/kotlin/com/cocahonka/comfywhitelist/config/general/GeneralConfig.kt`: Config loader

**Core Logic:**
- `src/main/kotlin/com/cocahonka/comfywhitelist/storage/YamlStorage.kt`: Whitelist data storage
- `src/main/kotlin/com/cocahonka/comfywhitelist/listeners/PlayerPreLoginEvent.kt`: Login enforcement

**Testing:**
- `src/test/kotlin/com/cocahonka/comfywhitelist/commands/CommandTestBase.kt`: Shared test base
- `src/test/kotlin/com/cocahonka/comfywhitelist/storage/StorageTestBase.kt`: Storage test base

**Build:**
- `build.gradle.kts`: Gradle build configuration
- `gradle.properties`: Version properties (paperApiVersion, mockBukkitVersion, etc.)

## Naming Conventions

**Files:**
- Kotlin classes: PascalCase matching class name (`CommandHandler.kt`, `YamlStorage.kt`)
- Test files: `{ClassName}Test.kt` (`AddCommandTest.kt`, `YamlStorageTest.kt`)
- Resource files: lowercase with underscores (`messages_en.yml`, `config.yml`)

**Directories:**
- Package structure: lowercase, matches reverse domain (`com/cocahonka/comfywhitelist`)
- Feature folders: lowercase singular (`commands`, `config`, `storage`, `listeners`)
- Subfolders: lowercase (`sub`, `base`, `general`, `message`)

**Classes:**
- Commands: `{Action}Command` (`AddCommand`, `RemoveCommand`, `EnableCommand`)
- Configs: `{Scope}Config` (`GeneralConfig`, `MessageConfig`)
- Listeners: `{EventName}` (`PlayerPreLoginEvent`)
- Interfaces: Noun describing contract (`SubCommand`, `Storage`)

## Where to Add New Code

**New Subcommand:**
- Implementation: `src/main/kotlin/com/cocahonka/comfywhitelist/commands/sub/{CommandName}Command.kt`
- Tests: `src/test/kotlin/com/cocahonka/comfywhitelist/commands/sub/{CommandName}CommandTest.kt`
- Registration: Add to `CommandHandler.init` commands list
- Permission: Add to `src/main/resources/plugin.yml` permissions section

**New Event Listener:**
- Implementation: `src/main/kotlin/com/cocahonka/comfywhitelist/listeners/{EventName}.kt`
- Tests: `src/test/kotlin/com/cocahonka/comfywhitelist/listeners/{EventName}Test.kt`
- Registration: Add `server.pluginManager.registerEvents()` in `ComfyWhitelist.registerEvents()`

**New Configuration Option:**
- Add key constant and property to `src/main/kotlin/com/cocahonka/comfywhitelist/config/general/GeneralConfig.kt`
- Add default value to `src/main/resources/config.yml`
- Update `updateProperties()` to load the value

**New Localized Message:**
- Add sealed class object to `src/main/kotlin/com/cocahonka/comfywhitelist/config/message/Message.kt`
- Add companion property to `src/main/kotlin/com/cocahonka/comfywhitelist/config/message/MessageConfig.kt`
- Update `MessageConfig.updateProperties()` to load it
- Add entries to all locale files in `src/main/resources/locales/`

**New Storage Implementation:**
- Implementation: `src/main/kotlin/com/cocahonka/comfywhitelist/storage/{Name}Storage.kt`
- Must implement `Storage` interface from `comfy-whitelist-api`
- Tests: `src/test/kotlin/com/cocahonka/comfywhitelist/storage/{Name}StorageTest.kt`

**Utilities:**
- Shared helpers: Add to `src/main/kotlin/com/cocahonka/comfywhitelist/LegacyUtils.kt` or create new utility object

## Special Directories

**`gradle/wrapper/`:**
- Purpose: Gradle wrapper JAR and properties
- Generated: Yes (by `gradle wrapper`)
- Committed: Yes

**`build/`:**
- Purpose: Build output (compiled classes, JAR files)
- Generated: Yes (by `./gradlew build`)
- Committed: No (in `.gitignore`)

**`src/main/resources/locales/`:**
- Purpose: Localization files copied to plugin data folder
- Generated: No (manually maintained)
- Committed: Yes
- Note: All 17 locale files are bundled and extracted on first run

---

*Structure analysis: 2026-01-30*
