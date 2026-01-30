# Technology Stack

**Analysis Date:** 2026-01-30

## Languages

**Primary:**
- Kotlin 1.8.10 - All application code (`src/main/kotlin/`, `src/test/kotlin/`)

**Secondary:**
- Kotlin DSL - Build configuration (`build.gradle.kts`, `settings.gradle.kts`)

## Runtime

**Environment:**
- JVM 8 (production) / JVM 17 (tests)
- Minecraft Server: Paper API 1.13.2+ (supports 1.13 - 1.20.x)

**Package Manager:**
- Gradle 8.1.1
- Lockfile: Not present (uses version catalog in `gradle.properties`)

## Frameworks

**Core:**
- Paper API 1.13.2-R0.1-SNAPSHOT - Minecraft server plugin framework
- Bukkit Configuration API - YAML configuration management

**Testing:**
- JUnit 5 (JUnit Platform) - Test framework
- MockBukkit v1.19 (2.29.0) - Bukkit server mocking

**Build/Dev:**
- Gradle Kotlin DSL - Build system
- Shadow Plugin 8.1.1 - Fat JAR packaging with dependency shading

## Key Dependencies

**Critical:**
- `net.kyori:adventure-text-minimessage:4.13.1` - MiniMessage text formatting with custom tags
- `net.kyori:adventure-text-serializer-legacy:4.13.1` - Legacy color code conversion
- `net.kyori:adventure-api:4.13.1` - Text component API

**Infrastructure:**
- `com.github.cocahonka:comfy-whitelist-api:1.0.1` - Public API for external plugin integration (from JitPack)

## Configuration

**Environment:**
- No environment variables required
- All configuration via YAML files in plugin data folder

**Build:**
- `build.gradle.kts` - Main build configuration
- `gradle.properties` - Version catalog for dependencies
- `settings.gradle.kts` - Project naming

**Key Build Artifacts:**
- `ComfyWhitelist-{version}-standalone.jar` - Includes Kotlin stdlib (run `./gradlew standalone`)
- `ComfyWhitelist-{version}-lightweight.jar` - Requires separate Kotlin runtime (run `./gradlew lightweight`)

## Platform Requirements

**Development:**
- JDK 17+ (tests require Java 17)
- Gradle 8.1.1+ (wrapper included)

**Production:**
- JRE 8+ (targets Java 8)
- Paper/Spigot server 1.13 - 1.20.x
- Optional: Kotlin stdlib plugin (for lightweight JAR variant)

## Repositories

**Maven:**
- Maven Central (default)
- Paper MC: `https://repo.papermc.io/repository/maven-public/`
- JitPack: `https://jitpack.io`

## Build Commands

```bash
./gradlew standalone    # Build fat JAR with Kotlin stdlib
./gradlew lightweight   # Build JAR without Kotlin stdlib
./gradlew test          # Run unit tests
```

---

*Stack analysis: 2026-01-30*
