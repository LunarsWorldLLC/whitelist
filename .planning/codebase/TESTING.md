# Testing Patterns

**Analysis Date:** 2026-01-30

## Test Framework

**Runner:**
- JUnit 5 (JUnit Platform)
- Config: `build.gradle.kts` with `useJUnitPlatform()`

**Mocking Framework:**
- MockBukkit v2.29.0 (Minecraft server mock)

**Assertion Library:**
- JUnit Jupiter Assertions

**Run Commands:**
```bash
./gradlew test              # Run all tests
./gradlew test --info       # Run with verbose output
```

## Test File Organization

**Location:**
- Separate test directory mirroring source: `src/test/kotlin/`
- Tests mirror package structure of production code

**Naming:**
- Test classes append `Test` suffix: `AddCommandTest`, `YamlStorageTest`
- Test methods use backtick syntax with descriptive sentences

**Structure:**
```
src/test/kotlin/
└── com/cocahonka/comfywhitelist/
    ├── commands/
    │   ├── CommandTestBase.kt          # Base class for command tests
    │   └── sub/
    │       ├── AddCommandTest.kt
    │       ├── ClearCommandTest.kt
    │       ├── DisableCommandTest.kt
    │       ├── EnableCommandTest.kt
    │       ├── HelpCommandTest.kt
    │       ├── ListCommandTest.kt
    │       ├── ReloadCommandTest.kt
    │       └── RemoveCommandTest.kt
    ├── config/
    │   ├── base/
    │   │   └── LocaleTest.kt
    │   ├── general/
    │   │   └── GeneralConfigTest.kt
    │   └── message/
    │       ├── MessageConfigTest.kt
    │       └── MessageTest.kt
    ├── listeners/
    │   └── PlayerPreLoginEventTest.kt
    └── storage/
        ├── StorageTestBase.kt          # Base class for storage tests
        └── YamlStorageTest.kt
```

## Test Structure

**Suite Organization:**
```kotlin
class AddCommandTest : CommandTestBase() {

    private lateinit var addCommand: AddCommand
    private lateinit var label: String
    private lateinit var addedPlayer: PlayerMock

    @BeforeEach
    override fun setUp() {
        super.setUp()
        addCommand = AddCommand(storage)
        label = addCommand.identifier
        addedPlayer = server.addPlayer()
        playerWithPermission.addAttachment(plugin, addCommand.permission, true)
    }

    @Test
    fun `when console is sender`() {
        val result = handler.onCommand(
            sender = console,
            command = command,
            label = label,
            args = arrayOf(addCommand.identifier, addedPlayer.name),
        )

        assertTrue(result)
        assertWhitelisted(addedPlayer)
        assertOnlyPlayerAddedMessage(console, addedPlayer)
    }
}
```

**Patterns:**
- Base classes for common setup: `CommandTestBase`, `StorageTestBase`
- `@BeforeEach` for test initialization with MockBukkit
- `@AfterEach` for MockBukkit cleanup (always call `MockBukkit.unmock()`)
- Descriptive test names in backticks: `` `when player is sender without permission` ``

## Mocking

**Framework:** MockBukkit (purpose-built for Bukkit plugin testing)

**Patterns:**
```kotlin
@BeforeEach
fun setUp() {
    server = MockBukkit.mock()
    plugin = MockBukkit.load(ComfyWhitelist::class.java)

    console = ConsoleCommandSenderMock()
    playerWithPermission = server.addPlayer()
    playerWithoutPermission = server.addPlayer()
}

@AfterEach
fun tearDown() {
    MockBukkit.unmock()
}
```

**What to Mock:**
- Bukkit server: `MockBukkit.mock()`
- Plugin instances: `MockBukkit.load(ComfyWhitelist::class.java)`
- Mock plugins for config tests: `MockBukkit.createMockPlugin()`
- Players: `server.addPlayer()`
- Console: `ConsoleCommandSenderMock()`
- Permissions: `player.addAttachment(plugin, permission, true)`

**What NOT to Mock:**
- Storage implementations (use real `YamlStorage` with mock plugin's `dataFolder`)
- Configuration managers (use real implementations)
- Message formatting (use real `MessageFormat`)

## Fixtures and Factories

**Test Data:**
```kotlin
// Players created via MockBukkit
val addedPlayer = server.addPlayer()
val invalidPlayer = server.addPlayer("мотузок")  // Invalid name with Cyrillic

// Configuration data accessed via reflection
val filePath = GeneralConfig::class.java
    .getDeclaredField("filePath")
    .apply { isAccessible = true }
    .get(GeneralConfig.Companion::class.java) as String
```

**Location:**
- No dedicated fixtures directory
- Test data created inline in test methods
- Reflection used to access private fields for testing

## Coverage

**Requirements:** None explicitly enforced

**Patterns:**
- Command tests cover: console sender, player with permission, player without permission, invalid usage
- Storage tests cover: add, remove, clear, list, save/load
- Config tests cover: default values, custom values from file

## Test Types

**Unit Tests:**
- Command execution tests
- Configuration loading tests
- Storage operation tests
- Locale parsing tests

**Integration Tests:**
- Event listener tests (async player pre-login)
- Full command handler with storage

**E2E Tests:**
- Not used (MockBukkit provides sufficient integration testing)

## Common Patterns

**Async Testing:**
```kotlin
private lateinit var eventCallerThread: Thread
private lateinit var latch: CountDownLatch

private val timeout = 2L
private val timeUnit = TimeUnit.SECONDS

@BeforeEach
fun setUp() {
    latch = CountDownLatch(1)
    eventCallerThread = Thread {
        val inetAddress = InetAddress.getLocalHost()
        event = AsyncPlayerPreLoginEvent(joiningPlayer.name, inetAddress, joiningPlayer.uniqueId)
        server.pluginManager.callEvent(event)
        latch.countDown()
    }
}

private fun executeEvent() {
    eventCallerThread.start()
    latch.await(timeout, timeUnit)
}
```

**Error Testing:**
```kotlin
@Test
fun `when player name is invalid`() {
    val invalidPlayer = server.addPlayer("мотузок")
    val result = handler.onCommand(
        sender = playerWithPermission,
        command = command,
        label = label,
        args = arrayOf(addCommand.identifier, invalidPlayer.name),
    )

    assertFalse(result)
    assertNotWhitelisted(invalidPlayer)
    assertOnlyInvalidPlayerNameMessage(playerWithPermission)
}
```

**Permission Testing:**
```kotlin
@Test
fun `when player is sender without permission`() {
    val result = handler.onCommand(
        sender = playerWithoutPermission,
        command = command,
        label = label,
        args = arrayOf(addCommand.identifier, addedPlayer.name),
    )

    assertFalse(result)
    assertNotWhitelisted(addedPlayer)
    assertOnlyNoPermissionMessage(playerWithoutPermission)
}
```

**Message Assertion Pattern:**
```kotlin
protected fun assertOnlyNoPermissionMessage(sender: MessageTarget) {
    assertEquals(
        sender.nextMessage(),
        legacySection.serialize(Message.NoPermission.getDefault(locale))
    )
    sender.assertNoMoreSaid()
}
```

## Base Test Classes

**CommandTestBase (`src/test/kotlin/.../commands/CommandTestBase.kt`):**
- Sets up MockBukkit server and plugin
- Creates console and player mocks
- Initializes storage, configs, and command handler
- Provides assertion helpers:
  - `assertWhitelisted(player)` / `assertNotWhitelisted(player)`
  - `assertWhitelistEnabled()` / `assertWhitelistDisabled()`
  - `assertStorageEmpty()`
  - `assertOnlyNoPermissionMessage(sender)`
  - `assertOnlyInvalidPlayerNameMessage(sender)`
  - `assertOnlyInvalidUsageMessage(sender, usage)`

**StorageTestBase (`src/test/kotlin/.../storage/StorageTestBase.kt`):**
- Abstract class requiring `createStorage()` implementation
- Provides common storage tests:
  - Add player to whitelist
  - Remove player from whitelist
  - Get whitelisted players
  - Clear whitelist
  - Save and load operations

## Reflection Usage in Tests

**Pattern:** Access private fields for testing internal state
```kotlin
val clearCommandKey = GeneralConfig::class.java
    .getDeclaredField("clearCommandKey")
    .apply { isAccessible = true }
    .get(GeneralConfig.Companion::class.java) as String
```

## Test Environment

**JVM Target:**
- Tests compile with JVM 17 (production targets JVM 8)
- Configured in `build.gradle.kts`:
```kotlin
tasks.withType<JavaCompile> {
    if (name == "compileTestJava") {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = if (name.contains("Test")) "17" else "1.8"
    }
}
```

---

*Testing analysis: 2026-01-30*
