# Expiring Whitelist

## What This Is

A Minecraft server whitelist plugin where player access expires after 31 days. Running the whitelist command again extends their time rather than resetting it. Designed for offline/cracked servers using username-based identification.

## Core Value

Players are automatically removed from the whitelist when their time expires, requiring server admins to actively maintain access.

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] Add player to whitelist with 31-day expiry
- [ ] Extend expiry time when player is re-added (increments, doesn't reset)
- [ ] Remove player from whitelist manually
- [ ] List all whitelisted players with their expiry dates
- [ ] Check specific player's remaining whitelist time
- [ ] Background task removes expired entries periodically
- [ ] Block non-whitelisted players from joining
- [ ] Username-based identification (not UUID)

### Out of Scope

- Multiple locales — English only, simplifies codebase
- Enable/disable toggle — whitelist always active
- Status command — redundant with list
- Help command — simple enough without it
- Clear command — dangerous, use remove individually
- Reload command — restart server or use plugin manager
- MiniMessage custom tags — plain messages sufficient
- External API exposure — no third-party plugin integration needed

## Context

**Existing Codebase:**
- Kotlin 1.8 + Paper API 1.13.2+ (supports 1.13-1.20.x)
- Gradle build with standalone/lightweight JAR variants
- MockBukkit test framework in place
- YAML-based storage already implemented

**Key Changes from Existing:**
- Storage must track expiry timestamps, not just player names
- Background scheduler needed for cleanup task
- Strip 17 locale files and complex message system
- Remove 5+ subcommands (enable, disable, status, help, clear, reload)

## Constraints

- **Runtime**: Paper/Spigot 1.13+ (Java 8 target for compatibility)
- **Identity**: Username-based only — no UUID lookups (offline server support)
- **Storage**: YAML file — no database dependencies
- **Language**: English only — no i18n complexity

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Username-based | Offline/cracked server support | — Pending |
| 31-day default | Balance between access and maintenance | — Pending |
| Increment on re-add | Rewards active engagement over punishment | — Pending |
| Background cleanup | Simpler than checking at every login | — Pending |
| Strip locales | Reduces maintenance, English-only use case | — Pending |

---
*Last updated: 2026-01-30 after initialization*
