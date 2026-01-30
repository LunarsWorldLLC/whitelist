# External Integrations

**Analysis Date:** 2026-01-30

## APIs & External Services

**Minecraft Server:**
- Paper API - Server plugin integration
  - Package: `com.destroystokyo.paper:paper-api`
  - No auth required (compile-only dependency)

**Public Plugin API:**
- ComfyWhitelist API - Exposed for third-party plugins
  - Package: `com.github.cocahonka:comfy-whitelist-api`
  - Registration: Bukkit Services Manager at `ServicePriority.Normal`
  - Access: `server.servicesManager.getRegistration(ComfyWhitelistAPI::class.java)`
  - Interfaces exposed: `Storage`, `WhitelistManager`
  - Implementation: `src/main/kotlin/com/cocahonka/comfywhitelist/ComfyWhitelist.kt` (lines 102-108)

## Data Storage

**Databases:**
- None (file-based storage only)

**File Storage:**
- YAML-based whitelist storage
  - File: `{plugin-data-folder}/whitelist.yml`
  - Implementation: `src/main/kotlin/com/cocahonka/comfywhitelist/storage/YamlStorage.kt`
  - Format: List of player usernames under `players` key
  - Library: Bukkit `YamlConfiguration`

**Configuration Files:**
- `config.yml` - General plugin settings (enabled state, locale, clear command toggle)
- `locales/messages_{locale}.yml` - Localized message strings (17 languages)

**Caching:**
- In-memory `MutableSet<String>` for whitelisted players
- Loaded from YAML on startup and reload
- Persisted to YAML on each modification

## Authentication & Identity

**Auth Provider:**
- Nickname-based (not UUID-based)
  - Design choice for offline/cracked server compatibility
  - Player identification: Username string matching (case-sensitive)

**Permission System:**
- Bukkit permission nodes (defined in `plugin.yml`)
- Root permission: `comfywhitelist.*`
- Per-command permissions: `comfywhitelist.{add|remove|clear|list|on|off|reload|help|status}`

## Monitoring & Observability

**Error Tracking:**
- Bukkit logger (`Bukkit.getLogger()`)
- Stack traces logged on storage I/O failures

**Logs:**
- Server console via Bukkit logging
- Warning level for locale fallback and I/O errors

## CI/CD & Deployment

**Hosting:**
- Manual deployment to Minecraft server plugins folder
- GitHub Releases for distribution

**CI Pipeline:**
- Not detected in repository

## Environment Configuration

**Required env vars:**
- None

**Secrets location:**
- Not applicable (no secrets required)

## Webhooks & Callbacks

**Incoming:**
- None

**Outgoing:**
- None

## Event Listeners

**Bukkit Events:**
- `AsyncPlayerPreLoginEvent` - Whitelist check on player join
  - Priority: `LOWEST`
  - Implementation: `src/main/kotlin/com/cocahonka/comfywhitelist/listeners/PlayerPreLoginEvent.kt`
  - Behavior: Kicks non-whitelisted players with `KICK_WHITELIST` result

## Command Registration

**Commands:**
- `/comfywhitelist` (alias: `/comfywl`)
  - Handler: `src/main/kotlin/com/cocahonka/comfywhitelist/commands/CommandHandler.kt`
  - Tab completion: `src/main/kotlin/com/cocahonka/comfywhitelist/commands/CommandTabCompleter.kt`

## Third-Party Libraries

**Kyori Adventure:**
- MiniMessage parsing for rich text formatting
- Custom style tags: `<warning>`, `<success>`, `<remove>`, `<off>`, `<comfy>`
- Legacy serialization for Bukkit 1.13 compatibility
- Implementation: `src/main/kotlin/com/cocahonka/comfywhitelist/config/message/MessageFormat.kt`

**Soft Dependencies:**
- `kotlin-stdlib` - Optional runtime dependency (for lightweight JAR)
  - Declared in `plugin.yml` as `softdepend`

---

*Integration audit: 2026-01-30*
