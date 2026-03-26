# Strong Buffs — Legal Feature Whitelist

This document defines what Strong Buffs is and is not allowed to implement.
It is derived from `docs/jagex-guidelines.md` (official Jagex rules) and RuneLite's Plugin Hub policy.

**If a feature is not on the approved list, it is not allowed.**
When in doubt, do not implement it.

---

## Approved Condition Types

These are the only condition types permitted. All track the **local player's own state only**.

| Condition | API | Notes |
|-----------|-----|-------|
| HP below/above X% | `client.getLocalPlayer().getHealthRatio()` / `getHealthScale()` | Own HP only |
| Prayer points below/above X | `client.getBoostedSkillLevel(Skill.PRAYER)` | |
| Specific prayer active/inactive | `client.getVarbitValue(Varbits.PRAYER_*)` | Own prayers only |
| Special attack energy at/above/below X% | `client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT)` | |
| Run energy below/above X% | `client.getVarpValue(VarPlayer.RUN_ENERGY_OR_TUNA_PASTE_DOSES)` | |
| Skill level (real) above/below X | `client.getRealSkillLevel(Skill.*)` | |
| Skill level (boosted) above/below X | `client.getBoostedSkillLevel(Skill.*)` | |
| Item in inventory (by name) | `ItemManager.search(name)` + `ItemContainer` | Own inventory only |
| Item equipped (by name) | Equipment `ItemContainer` | Own equipment only |
| Item count in inventory above/below X | `ItemContainer` | |
| NPC nearby (by name) | `client.getNpcs()` — name match only | Proximity alert; no attack/phase info |
| Slayer task active / remaining count | Slayer VarBits | |
| Quest progress at stage X | Quest VarBits | |
| Stat XP changed | `StatChanged` event | |
| Chat message received matching pattern | `ChatMessage` event | Player's own chat |
| Ground item nearby (by name) | `ItemSpawned` event + `ItemManager.search(name)` | |
| Animation playing on local player | `AnimationChanged` event | Own player only |
| Graphic/spotanim on local player | `GraphicChanged` event | Own player only |
| Poison/venom active | Poison VarPlayer | |
| Player in wilderness | Wilderness VarBit | |
| Game tick (time-based trigger) | `GameTick` event | |

## Approved Display Types

| Display | Notes |
|---------|-------|
| Overlay text (label + value) | |
| Colored icon or image | |
| Progress bar | |
| InfoBox (corner counter/timer) | |
| Screen flash / border color | |
| Inventory item highlight | Own inventory only |
| Sound alert | |
| Chat message to self | `ChatMessageType.GAMEMESSAGE` — no programmatic outgoing chat |

## Approved User Input

Users may configure rules using:

| Input type | Example | Allowed |
|-----------|---------|---------|
| Item name (text) | "Dragon scimitar" | ✅ Resolved via `ItemManager.search()` |
| NPC name (text) | "Abyssal demon" | ✅ Matched against `NPC.getName()` |
| Skill name (enum) | `Skill.ATTACK` | ✅ |
| Prayer name (enum) | `Varbits.PRAYER_PROTECT_FROM_MAGIC` | ✅ |
| Numeric thresholds | HP < 30%, spec >= 100% | ✅ |
| Color picker | Overlay color | ✅ |
| Sound file selection | Alert sound | ✅ |
| Player name | Target another player | ❌ Player targeting |
| Raw NPC/item/object IDs | Free-form numeric input | ❌ Moderation risk |

---

## Explicitly Not Allowed

These are hard bans. Do not implement regardless of how the user asks.

### From Jagex Guidelines (direct prohibitions)
- Any indicator of how long an opponent is frozen
- Any indicator of which prayer to activate in combat (prayer switching helper)
- Any indicator of boss attack type, timing, or rotation
- Any indicator of which player an NPC is targeting
- Any projectile landing location indicator
- Any attack counter for boss mechanics
- Any PvP scouting information about other players
- Sotetseg maze layout reveal
- Blackjacking menu modifications
- Spellbook or prayer book resizing
- Click zone modification for any interface
- Detached-camera world interaction

### From RuneLite Policy
- AFK training assistance (any skill)
- Opponent freeze timer
- Boss phase/rotation helpers (Zulrah, Cerberus, Theatre of Blood, Inferno, etc.)
- Highlighting players by combat level range (PvP attackability)
- PK/skull warnings
- Collecting or transmitting other players' data
- Programmatic chat insertion or modification

### Technical prohibitions
- No user-entered raw NPC/item/object/varbit IDs as free-form input
- No tracking of other players (only `client.getLocalPlayer()`)
- No reflection, JNI, subprocess execution, or runtime code loading
- Plugin written in Java only — no Kotlin, Scala, etc.

---

## Name Resolution is Safe

Accepting item/NPC **names** as user input is explicitly fine — this is the existing pattern in Ground Items, NPC Indicators, and Bank Tags. The plugin resolves names to IDs internally via RuneLite APIs; no raw IDs are exposed to users.

---

## Reference
Full Jagex guidelines: `docs/jagex-guidelines.md`
