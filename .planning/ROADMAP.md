# Roadmap: Expiring Whitelist

**Created:** 2026-01-30
**Depth:** Quick (3-5 phases)

## Overview

| Phase | Name | Goal | Requirements |
|-------|------|------|--------------|
| 1 | Storage Foundation | Expiry-aware storage system | STOR-01, STOR-02, STOR-03, SIM-01 |
| 2 | Commands | All whitelist management commands | CMD-01 to CMD-05, SIM-02, SIM-03 |
| 3 | Access & Cleanup | Login enforcement and auto-expiry | ACC-01, ACC-02, CLN-01, CLN-02 |

---

## Phase 1: Storage Foundation ✓

**Status:** Complete (2026-01-30)
**Goal:** Replace simple player list with expiry-aware storage system

**Requirements:**
- STOR-01: Store player whitelist entries with expiry timestamps
- STOR-02: Load whitelist from YAML on plugin startup
- STOR-03: Save whitelist to YAML after modifications
- SIM-01: Remove locale system (English only)

**Plans:** 2 plans

Plans:
- [x] 01-01-PLAN.md — Expiry-aware storage (WhitelistEntry + YamlStorage modifications)
- [x] 01-02-PLAN.md — Remove locale system (Messages object, delete locale files)

**Success Criteria:**
1. WhitelistEntry data class holds player name + expiry timestamp
2. YAML storage saves/loads entries with timestamps
3. Storage can add player with expiry, check if valid, extend time
4. All locale files and Message sealed class removed
5. Tests pass for storage operations

---

## Phase 2: Commands ✓

**Status:** Complete (2026-01-30)
**Goal:** Implement all whitelist management commands with expiry support

**Requirements:**
- CMD-01: `/wl add <player>` adds player with 31-day expiry
- CMD-02: `/wl add <player>` on existing player extends expiry by 31 days
- CMD-03: `/wl remove <player>` removes player from whitelist
- CMD-04: `/wl list` shows all players with days remaining
- CMD-05: `/wl check <player>` shows specific player's remaining time
- SIM-02: Remove unused commands (enable, disable, status, help, clear, reload)
- SIM-03: Simplify message formatting (no MiniMessage tags)

**Plans:** 2 plans

Plans:
- [x] 02-01-PLAN.md — Expiry-aware command logic (modify add/list, create check, add messages)
- [x] 02-02-PLAN.md — Command cleanup (delete 6 unused commands, update handler/tab completer/permissions)

**Success Criteria:**
1. Add command creates/extends entries correctly
2. Remove command deletes entries
3. List shows player names with "X days remaining" format
4. Check shows specific player's remaining time or "not found"
5. Old commands removed from codebase
6. Messages use simple string formatting
7. Tab completion works for all commands

---

## Phase 3: Access & Cleanup ✓

**Status:** Complete (2026-01-30)
**Goal:** Enforce whitelist on login and auto-remove expired entries

**Requirements:**
- ACC-01: Block non-whitelisted players from joining server
- ACC-02: Allow whitelisted players with valid (non-expired) entries
- CLN-01: Background task runs periodically to remove expired entries
- CLN-02: Expired entries removed from storage file

**Plans:** 1 plan

Plans:
- [x] 03-01-PLAN.md — Verify access control tests + implement cleanup task

**Success Criteria:**
1. Non-whitelisted players kicked with clear message
2. Expired players treated as non-whitelisted
3. Valid whitelisted players can join
4. Scheduler task runs every hour (configurable)
5. Expired entries automatically removed and saved
6. Plugin fully functional end-to-end

---

## Coverage

All 15 v1 requirements mapped:
- Phase 1: 4 requirements (storage + locale removal)
- Phase 2: 8 requirements (commands + simplification)
- Phase 3: 4 requirements (access control + cleanup)

Unmapped: 0

---
*Roadmap created: 2026-01-30*
