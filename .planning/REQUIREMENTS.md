# Requirements: Expiring Whitelist

**Defined:** 2026-01-30
**Core Value:** Players are automatically removed from the whitelist when their time expires

## v1 Requirements

### Storage

- [ ] **STOR-01**: Store player whitelist entries with expiry timestamps
- [ ] **STOR-02**: Load whitelist from YAML on plugin startup
- [ ] **STOR-03**: Save whitelist to YAML after modifications

### Commands

- [ ] **CMD-01**: `/wl add <player>` adds player with 31-day expiry
- [ ] **CMD-02**: `/wl add <player>` on existing player extends expiry by 31 days
- [ ] **CMD-03**: `/wl remove <player>` removes player from whitelist
- [ ] **CMD-04**: `/wl list` shows all players with days remaining
- [ ] **CMD-05**: `/wl check <player>` shows specific player's remaining time

### Access Control

- [ ] **ACC-01**: Block non-whitelisted players from joining server
- [ ] **ACC-02**: Allow whitelisted players with valid (non-expired) entries

### Cleanup

- [ ] **CLN-01**: Background task runs periodically to remove expired entries
- [ ] **CLN-02**: Expired entries removed from storage file

### Simplification

- [ ] **SIM-01**: Remove locale system (English only)
- [ ] **SIM-02**: Remove unused commands (enable, disable, status, help, clear, reload)
- [ ] **SIM-03**: Simplify message formatting (no MiniMessage tags)

## v2 Requirements

(None planned)

## Out of Scope

| Feature | Reason |
|---------|--------|
| Multiple locales | English-only use case, reduces maintenance |
| UUID-based identity | Offline/cracked server support requires usernames |
| Enable/disable toggle | Whitelist always active |
| Database storage | YAML sufficient for expected scale |
| External API | No third-party plugin integration needed |
| Configurable expiry duration | 31 days hardcoded, simplicity |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| STOR-01 | Phase 1 | Pending |
| STOR-02 | Phase 1 | Pending |
| STOR-03 | Phase 1 | Pending |
| CMD-01 | Phase 2 | Pending |
| CMD-02 | Phase 2 | Pending |
| CMD-03 | Phase 2 | Pending |
| CMD-04 | Phase 2 | Pending |
| CMD-05 | Phase 2 | Pending |
| ACC-01 | Phase 3 | Pending |
| ACC-02 | Phase 3 | Pending |
| CLN-01 | Phase 3 | Pending |
| CLN-02 | Phase 3 | Pending |
| SIM-01 | Phase 1 | Pending |
| SIM-02 | Phase 2 | Pending |
| SIM-03 | Phase 2 | Pending |

**Coverage:**
- v1 requirements: 15 total
- Mapped to phases: 15
- Unmapped: 0 ✓

---
*Requirements defined: 2026-01-30*
*Last updated: 2026-01-30 after initial definition*
