# Coding Conventions

**Analysis Date:** 2026-01-30

## Naming Patterns

**Files:**
- PascalCase for class files: `ComfyWhitelist.kt`, `CommandHandler.kt`, `YamlStorage.kt`
- Subcommand files named after the command: `AddCommand.kt`, `RemoveCommand.kt`
- Test files append `Test` suffix: `AddCommandTest.kt`, `YamlStorageTest.kt`

**Classes:**
- PascalCase: `ComfyWhitelist`, `MessageConfig`, `YamlStorage`
- Interface implementations often match interface name: `Storage` interface implemented by `YamlStorage`
- Sealed class members as objects: `Message.NoPermission`, `Message.PlayerAdded`

**Functions:**
- camelCase: `loadConfig()`, `addPlayer()`, `isPlayerWhitelisted()`
- Event handlers prefixed with `on`: `onCommand()`, `onPlayerPreLoginEvent()`
- Boolean getters use `is` prefix: `isPlayerWhitelisted()`, `isWhitelistEnabled()`

**Variables:**
- camelCase for local variables: `playerName`, `subCommandName`, `rawMessageFromConfig`
- Private properties use `private` modifier, not underscore prefix
- Constants use SCREAMING_SNAKE_CASE: `DISPLAY_NAME`, `WHITELISTED_PLAYERS_KEY`

**Types:**
- PascalCase: `Locale`, `MessageFormat`, `SubCommand`
- Companion object constants grouped with class

## Code Style

**Formatting:**
- Kotlin official code style (via `kotlin.code.style=official` in `gradle.properties`)
- 4-space indentation
- Braces on same line as declaration
- No trailing commas

**Linting:**
- No explicit linter configured
- Relies on Kotlin compiler checks and IDE

**Suppression Annotations:**
- `@Suppress("removal")` for deprecated API usage
- `@Suppress("unused")` for plugin entry point
- `@file:Suppress("removal")` at file level when needed

## Import Organization

**Order:**
1. Kotlin stdlib imports
2. Third-party imports (net.kyori.adventure, be.seeseemelk.mockbukkit)
3. Java stdlib imports
4. Project imports (com.cocahonka.comfywhitelist)

**Style:**
- Explicit imports only, no wildcard imports for production code
- Wildcard imports allowed in tests: `import org.junit.jupiter.api.Assertions.*`

## Error Handling

**Patterns:**
- Return boolean for success/failure: `fun save(): Boolean`, `fun load(): Boolean`
- try-catch with logging for IO operations:
```kotlin
return try {
    config.save(storageFile)
    true
} catch (e: Exception) {
    getLogger().warning(e.stackTraceToString())
    false
}
```
- Early returns for validation failures
- No custom exception types - uses standard Kotlin/Java exceptions

**Validation:**
- Use `require()` for precondition checks: `require(dataFolder.isDirectory)`
- Regex for input validation: `SubCommand.playerNameRegex`
- Null checks with safe calls and elvis operator

## Logging

**Framework:** Bukkit logger via `org.bukkit.Bukkit.getLogger()`

**Patterns:**
- Warning level for errors: `getLogger().warning(e.stackTraceToString())`
- Warning for configuration issues: `getLogger().warning("locale '$value' does not exist!")`
- No info or debug level logging

## Comments

**When to Comment:**
- KDoc for public interfaces and classes
- KDoc for interface methods explaining behavior
- Comments for non-obvious code (`// KEKW` for fun locales)

**KDoc Style:**
```kotlin
/**
 * Represents a subcommand within the plugin.
 *
 * Implement this interface to create a new subcommand that can be executed by a [CommandSender]
 * and has its own [identifier], [permission], and [usage] information.
 */
interface SubCommand {
```

**Property Documentation:**
```kotlin
/**
 * @property storage The [Storage] instance to interact with whitelist data.
 */
class AddCommand(private val storage: Storage) : SubCommand {
```

## Function Design

**Size:**
- Functions generally small (5-20 lines)
- Largest functions are `when` expressions for locale mapping

**Parameters:**
- Use named parameters in Kotlin DSL contexts: `filter { it.startsWith(prefix, ignoreCase = true) }`
- Constructor injection for dependencies: `AddCommand(storage: Storage)`
- Use data classes or interfaces for complex parameters

**Return Values:**
- Boolean for success/failure of operations
- `Set<String>` for player lists (immutable copy returned)
- `Component` for formatted messages
- Extension functions for transformations: `Component.toLegacyText()`

## Module Design

**Exports:**
- Public API through interfaces: `Storage`, `WhitelistManager`
- Internal setters with `internal set` or `private set`
- Companion objects for static state and constants

**Package Structure:**
- `commands/` - Command handling
- `commands/sub/` - Individual subcommand implementations
- `config/` - Configuration management
- `config/base/` - Base configuration classes
- `config/general/` - Plugin settings
- `config/message/` - Localized messages
- `listeners/` - Event listeners
- `storage/` - Data persistence
- `api/` - Public API interfaces (external dependency)

## Dependency Injection

**Pattern:** Constructor injection for all dependencies

**Example:**
```kotlin
class CommandHandler(
    storage: Storage,
    generalConfig: GeneralConfig,
    plugin: ComfyWhitelist,
) : CommandExecutor {
```

## Interface Design

**Pattern:** Interfaces define contracts, implementations handle details

**Example:**
```kotlin
interface SubCommand {
    val identifier: String
    val permission: String
    val usage: String
    fun execute(sender: CommandSender, args: Array<String>): Boolean
}
```

## Static State

**Pattern:** Companion objects with delegated properties

```kotlin
companion object {
    var whitelistEnabled: Boolean by Delegates.notNull()
        private set
    lateinit var locale: Locale
        private set
}
```

**Message Configuration Pattern:**
```kotlin
companion object {
    lateinit var noPermission: Component internal set
    lateinit var playerAdded: Component private set
}
```

## Extension Functions

**Pattern:** Utility extensions in dedicated files

```kotlin
// LegacyUtils.kt
fun CommandSender.sendMessage(component: Component) {
    sendMessage(legacySection.serialize(component))
}

fun Component.toLegacyText() = legacySection.serialize(this)
```

## Sealed Classes

**Pattern:** Use for enumeration with associated data

```kotlin
sealed class Message(val key: String) {
    abstract fun getDefault(locale: Locale): Component

    object NoPermission : Message("no-permission") {
        override fun getDefault(locale: Locale): Component = ...
    }
}
```

## Builder Patterns

**Pattern:** Lambda builders for configuration objects

```kotlin
val nameReplacementConfigBuilder = { playerName: String ->
    TextReplacementConfig.builder()
        .match("<name>")
        .replacement(playerName)
        .build()
}
```

---

*Convention analysis: 2026-01-30*
