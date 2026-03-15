# Strong Buffs - Agent Guide

## What This Project Is

**Strong Buffs** is a RuneLite plugin (Plugin Hub) that brings WeakRules-style conditional overlays to Old School RuneScape.

**WeakRules concept:** users define "Rules" - visual or audio indicators that activate when configurable game-state conditions are met. The goal is a flexible, user-driven overlay system that surfaces real-time information the player defines. This replaces hardcoded "show X always" overlays with "show X when Y is true."

Examples of intended functionality:
- Flash a warning when HP drops below 30%
- Show an icon when a prayer is active
- Play a sound when special attack energy hits 100%
- Highlight an inventory item when a buff ends
- Alert when entering the wilderness

## Legal Constraints (Jagex Rules)

This plugin uses a **strict whitelist** approach. If a feature is not on the approved list, it is not implemented.

- Official Jagex rules: `docs/jagex-guidelines.md`
- Approved condition/display types: `docs/runelite-wiki/rejected-features.md`

**When uncertain about any feature: do not implement it.** There are no grey areas. If it is not explicitly on the approved list in `rejected-features.md`, it is out of scope.

Hard bans (from Jagex, non-negotiable):
- Opponent freeze timers
- Prayer switching indicators in combat
- Boss attack timing, rotation, or phase helpers
- Any information about other players for PvP purposes
- AFK training assistance

All conditions track the **local player's own state only**. No other player data.

## Architecture

### Design Principles

- Persist **definitions**, not live runtime objects
- Keep RuneLite API access in runtime services only
- Evaluate rules from **event-driven cached state**, not direct ad hoc reads everywhere
- Support both **steady-state** and **transition-based** activations
- Keep rendering concerns separated by display type and lifecycle
- Fail closed when data is invalid, unknown, or out of policy

### Overview

```
RuleDefinition          <- one user-created rule (persisted as JSON)
  |- rootGroup          <- tree of ConditionGroup / ConditionDefinition nodes
  |- display            <- typed DisplayDefinition
  |- activationMode     <- WHILE_ACTIVE / ON_ENTER / ON_EXIT
  `- cooldownTicks      <- optional activation suppression

RuleDefinitionStore     <- serializes versioned RuleDefinition list to/from RuneLite config
RuleCompiler            <- converts persisted definitions into compiled runtime evaluators
RuntimeState            <- cached local-player state updated from RuneLite events only
TriggerIndex            <- maps event sources to affected compiled rules
RuleEngine              <- evaluates rules, tracks active state, handles transitions/cooldowns
DisplayController(s)    <- overlay / infobox / inventory / sound outputs driven by RuleEngine
StrongBuffsPanel        <- PluginPanel sidebar: list/create/edit/delete rules
StrongBuffsPlugin       <- wires everything together
StrongBuffsConfig       <- global settings (enable toggle, global volume, etc.)
```

### Package Structure

```
nl.jessedezwart.strongbuffs/
  StrongBuffsPlugin.java
  StrongBuffsConfig.java
  RuleCompiler.java
  RuleEngine.java
  RuleDefinitionStore.java
  runtime/
    RuntimeState.java              <- local-player snapshot/cache, updated on the client thread
    TriggerIndex.java              <- maps varbits/skills/containers/ticks to affected rules
    ActiveRuleState.java           <- active/inactive, enter/exit, cooldown tracking
  model/
    RuleDefinition.java
    ActivationMode.java            <- WHILE_ACTIVE, ON_ENTER, ON_EXIT
    ConditionNode.java             <- marker interface for persisted condition definitions
    ConditionGroup.java            <- AND/OR of List<ConditionNode> children
    ConditionLogic.java            <- enum: AND, OR
    condition/
      ConditionDefinition.java     <- abstract persisted leaf node
      HpCondition.java             <- #9
      PrayerPointsCondition.java   <- #1
      PrayerActiveCondition.java   <- #2
      SpecCondition.java           <- #3
      RunEnergyCondition.java      <- #4
      PoisonCondition.java         <- #5
      WildernessCondition.java     <- #6
      SlayerTaskCondition.java     <- #7
      QuestProgressCondition.java  <- #8
      SkillLevelCondition.java     <- #10 (real level)
      BoostedSkillCondition.java   <- #11
      XpGainCondition.java         <- #12
      ItemInInventoryCondition.java<- #13
      ItemCountCondition.java      <- #14
      ItemEquippedCondition.java   <- #15
      GroundItemCondition.java     <- #16
      PlayerInZoneCondition.java   <- #17
      PlayerInInstanceCondition.java <- #18
    display/
      DisplayDefinition.java
      OverlayTextDisplay.java
      ColoredIconDisplay.java
      ProgressBarDisplay.java
      InfoBoxDisplay.java
      ScreenFlashDisplay.java
      InventoryHighlightDisplay.java
      SoundAlertDisplay.java
  overlay/
    RuleOverlay.java
    InventoryHighlightOverlay.java
  infobox/
    RuleInfoBoxManager.java
  sound/
    RuleSoundController.java
  panel/
    StrongBuffsPanel.java
    RuleListPanel.java
    RuleEditPanel.java
    ConditionGroupPanel.java
    ConditionRowPanel.java
```

### Data Model

```java
// One user-defined rule
class RuleDefinition
{
    int schemaVersion;
    String id;                  // UUID, generated on creation
    String name;
    boolean enabled;
    ConditionGroup rootGroup;
    ActivationMode activationMode;
    int cooldownTicks;          // optional; 0 = none
    DisplayDefinition display;
}

// Persisted condition tree node - AND/OR group
class ConditionGroup implements ConditionNode
{
    ConditionLogic logic;          // AND or OR
    List<ConditionNode> children;  // Condition leaves or nested ConditionGroups
}

// Persisted leaf node - no direct RuneLite client access here
abstract class ConditionDefinition implements ConditionNode
{
}

// What to render when the rule fires
abstract class DisplayDefinition
{
}

// Runtime-only compiled rule
class CompiledRule
{
    RuleDefinition definition;
    RuntimeConditionEvaluator evaluator;
    DisplayController displayController;
}
```

**Condition logic example:**
```
rootGroup (AND)
  |- PrayerPointsCondition (< 10)
  `- nestedGroup (OR)
     |- WildernessCondition (any level)
     `- PlayerInZoneCondition (Edgeville area)
```

### Runtime Model

Persisted definitions are pure data. They are safe to serialize, diff, validate, and migrate.

Runtime evaluation is separate:

- `RuntimeState` caches only the local player's approved state
- `RuleCompiler` converts `RuleDefinition` into `CompiledRule`
- `CompiledRule` contains pre-resolved evaluators, trigger metadata, and renderer bindings
- No persisted model class may directly call RuneLite APIs

This separation keeps client-thread concerns and RuneLite API coupling out of the saved config format.

### Activation Semantics

Not every rule is a steady-state boolean. The engine must support transitions explicitly.

- `WHILE_ACTIVE`: render for as long as the condition tree evaluates true
- `ON_ENTER`: fire once when the condition tree changes from false to true
- `ON_EXIT`: fire once when the condition tree changes from true to false

Optional cooldown:

- `cooldownTicks`: suppress repeated firings for a short period after activation

This is required for use cases like "prayer just deactivated", "buff ended", and one-shot sound alerts.

### Persistence

All rules are serialized as a JSON array using Gson (bundled with RuneLite) and stored in a single RuneLite config key:

- Config group: `strongbuffs`
- Config key: `rules`
- Format: `[{ "id": "...", "name": "...", ... }, ...]`

`RuleDefinitionStore` owns all serialize/deserialize logic. Polymorphic Gson type adapters handle `ConditionNode` and `DisplayDefinition` subtypes.

Persistence rules:

- Every saved rule includes `schemaVersion`
- `RuleDefinitionStore` is responsible for migrations from older schema versions
- Unknown or invalid nodes must fail closed and be ignored, never partially executed
- User-entered names may be accepted in the UI, but persisted data should prefer canonical internal identifiers where practical

On `startUp`: load and deserialize from config. On any save: serialize and write back.

## Condition Reference

All 18 conditions. All track the local player only.

### `VarbitChanged` / `VarPlayerChanged`

| # | Class | What it checks | API |
|---|-------|---------------|-----|
| 1 | `PrayerPointsCondition` | Prayer pts below/above X | `VarPlayer.PRAYER_POINTS` |
| 2 | `PrayerActiveCondition` | Specific prayer active/inactive | `Varbits.PRAYER_*` |
| 3 | `SpecCondition` | Spec energy below/above X% | `VarPlayer.SPECIAL_ATTACK_PERCENT` |
| 4 | `RunEnergyCondition` | Run energy below/above X% | `VarPlayer.RUN_ENERGY_OR_TUNA_PASTE_DOSES` |
| 5 | `PoisonCondition` | Poison or venom active | poison `VarPlayer` |
| 6 | `WildernessCondition` | Player in wilderness | wilderness `VarBit` |
| 7 | `SlayerTaskCondition` | Task active / kills remaining | slayer `VarBits` |
| 8 | `QuestProgressCondition` | Quest at stage X | quest `VarBits` |

### `StatChanged`

| # | Class | What it checks | API |
|---|-------|---------------|-----|
| 9 | `HpCondition` | HP points below/above X | `Skill.HITPOINTS` boosted level |
| 10 | `SkillLevelCondition` | Real skill level above/below X | `event.getLevel()` |
| 11 | `BoostedSkillCondition` | Boosted skill level above/below X | `event.getBoostedLevel()` |
| 12 | `XpGainCondition` | XP gained in a specific skill | any `StatChanged` for that skill |

### `ItemContainerChanged`

| # | Class | What it checks | Container |
|---|-------|---------------|-----------|
| 13 | `ItemInInventoryCondition` | Item present in inventory (by name) | `INVENTORY` |
| 14 | `ItemCountCondition` | Item count above/below X | `INVENTORY` |
| 15 | `ItemEquippedCondition` | Item equipped (by name) | `EQUIPMENT` |

### `ItemSpawned` / `ItemDespawned`

| # | Class | What it checks | API |
|---|-------|---------------|-----|
| 16 | `GroundItemCondition` | Ground item nearby (by name) | `ItemManager.search(name)` |

### `GameTick` - location only

| # | Class | What it checks | API |
|---|-------|---------------|-----|
| 17 | `PlayerInZoneCondition` | Player inside a defined tile rectangle | `WorldPoint.isInZone(sw, ne, check)` |
| 18 | `PlayerInInstanceCondition` | Player is in a private instance | `client.getTopLevelWorldView().isInstance()` |

## Display Types

| Enum | What it renders |
|------|----------------|
| `OVERLAY_TEXT` | Text label + optional value drawn on screen |
| `COLORED_ICON` | Colored icon or image |
| `PROGRESS_BAR` | Horizontal/vertical progress bar |
| `INFOBOX` | Small counter/timer in the corner (RuneLite `InfoBox`) |
| `SCREEN_FLASH` | Colored border flash on the game viewport |
| `INVENTORY_HIGHLIGHT` | Colored highlight drawn on an inventory item |
| `SOUND_ALERT` | Audio alert (uses RuneLite `SoundEffectPlayer`) |

Implementation notes:

- These should not share one nullable-field data class
- Each display type should have its own typed config object and its own renderer/controller
- Overlay rendering, infobox management, and sound playback have different lifecycles and should stay separated

## Panel UI

The sidebar panel (`StrongBuffsPanel`) is added to the client toolbar on `startUp` and removed on `shutDown`.

```
[ + New Rule ]
----------------------------------
> Low HP Warning      [Edit] [X]
  Spec at 100%        [Edit] [X]
  Wilderness Alert    [Edit] [X]

- Edit view (RuleEditPanel) -

Name: [Low HP Warning__________]

Conditions:
[ AND v ]
  HP  [ below v ] [ 30 ] pts   [X]
  [ OR v ]
    Wilderness [ any level ]   [X]
  [ + add condition ]
[ + add group ]

Activation:
Mode: [ While active v ]
Cooldown ticks: [ 0 ]

Display:
Type:  [ Screen flash v ]
Color: [########]

[ Save ]  [ Cancel ]
```

`ConditionGroupPanel` renders recursively. A `ConditionGroup` may contain nested `ConditionGroup` children, each rendered as an indented sub-panel with its own AND/OR toggle.

The panel should edit a draft model and only persist validated data on save. Do not mutate live runtime objects directly from Swing components.

## Event Handling

`RuleEngine` subscribes to RuneLite events. It updates `RuntimeState`, asks `TriggerIndex` which compiled rules are affected, and only re-evaluates those rules.

| Event | Triggers re-eval of |
|-------|-------------------|
| `VarbitChanged` | #1-8 (filtered by relevant varbits/varps) |
| `StatChanged` | #9-12 (filtered by relevant skills) |
| `ItemContainerChanged` | #13-15 (filtered by inventory/equipment container) |
| `ItemSpawned` / `ItemDespawned` | #16 (ground item conditions) |
| `GameTick` | #17-18 (location conditions only) |

Additional rules:

- `VarbitChanged` handling should be filtered by relevant varbits/varps, not "all var-based rules"
- `StatChanged` handling should be filtered by skill where possible
- Ground-item and XP-based conditions may require retained runtime state, not just immediate event inspection
- `RuleEngine` maintains per-rule `ActiveRuleState`, not just a flat `Set<String>`

`ActiveRuleState` tracks:

- current truth value
- entered/exited this tick
- last activation tick
- cooldown suppression
- display-specific state if required

## Build Order

Build in this sequence:

1. **Core model** - `RuleDefinition`, `ActivationMode`, `ConditionNode`, `ConditionGroup`, `ConditionLogic`, typed `DisplayDefinition`
2. **Versioned storage** - `RuleDefinitionStore` with subtype adapters and schema migrations
3. **Runtime core** - `RuntimeState`, `RuleCompiler`, `TriggerIndex`, `ActiveRuleState`, `RuleEngine`
4. **Vertical slice** - implement 3 conditions (`HpCondition`, `PrayerPointsCondition`, `SpecCondition`) and 3 displays (`OVERLAY_TEXT`, `SCREEN_FLASH`, `SOUND_ALERT`)
5. **Plugin wiring** - `StrongBuffsPlugin` startUp/shutDown, overlay + panel registration
6. **Minimal panel** - create/edit/delete rules for the vertical slice only
7. **Testing** - serialization tests, evaluator tests, trigger-index tests, activation-transition tests
8. **Expand safely** - add remaining approved conditions and display types incrementally

Do not implement all 18 conditions before the first usable end-to-end slice. That would lock in the wrong abstractions too early.

## New Condition Workflow (`/new-condition`)

1. Confirm the condition is in the approved list in `docs/runelite-wiki/rejected-features.md`
2. Identify the triggering event and VarBit/API from `docs/runelite-wiki/vars.md` and `events-reference.md`
3. Add a persisted definition class in `model/condition/`
4. Add or extend the runtime evaluator/compiler mapping
5. Register trigger metadata in `TriggerIndex` / `RuleEngine`
6. Add tests for serialization, evaluation, and trigger routing
7. Add a row to the condition reference table in this file

## Key RuneLite APIs

See `docs/runelite-wiki/` for detailed documentation.

```java
// HP
client.getBoostedSkillLevel(Skill.HITPOINTS)

// Vars
client.getVarpValue(VarPlayer.PRAYER_POINTS)
client.getVarpValue(VarPlayer.SPECIAL_ATTACK_PERCENT)  // 0-1000 (div by 10 = %)
client.getVarpValue(VarPlayer.RUN_ENERGY_OR_TUNA_PASTE_DOSES)
client.getVarbitValue(Varbits.PRAYER_PROTECT_FROM_MAGIC)

// Skills
client.getRealSkillLevel(Skill.ATTACK)
client.getBoostedSkillLevel(Skill.PRAYER)

// Inventory
client.getItemContainer(InventoryID.INVENTORY)
client.getItemContainer(InventoryID.EQUIPMENT)

// Location
client.getLocalPlayer().getWorldLocation()
WorldPoint.isInZone(sw, ne, check)
client.getTopLevelWorldView().isInstance()

// Ground items
event.getItem().getId()
```

## Code Conventions

From `docs/runelite-wiki/code-conventions.md`:

- **Java only** (no Kotlin, Scala, etc.)
- **Tabs** for indentation, not spaces
- **Allman-style braces** - opening brace on its own line
- `else`, `catch`, `finally` on new lines
- Lombok `@Slf4j` for logging
- `@Subscribe` methods for events
- No reflection, JNI, subprocess execution, or runtime code loading
- Plugin package: `nl.jessedezwart.strongbuffs`

## Commands

| Command | Description |
|---------|-------------|
| `./gradlew run` | Start the dev client with `--developer-mode --debug` and the plugin loaded |
| `./gradlew build` | Compile and run all checks |
| `./gradlew test` | Run unit tests only |
| `./gradlew shadowJar` | Build the distributable fat JAR (output: `build/libs/*-all.jar`) |

**Claude Code skills:** `/run`, `/build`, `/test`, `/new-condition` (defined in `.claude/commands/`)
**Codex skills:** defined in `skills/` with `SKILL.md` files (auto-matched by description)

## Development Workflow

1. **Run dev client:** `./gradlew run`
   - Passes `--developer-mode --debug` automatically
   - Opens RuneLite with the plugin loaded
2. **DevTools panel** - enable via the developer mode panel; use Var Inspector to find VarBit IDs
3. **Logging** - `log.debug(...)` for dev info, only shows with `--debug`; production uses `log.info(...)` sparingly
4. **Build vertically** - get one end-to-end rule working before expanding condition count

## Key External References

- RuneLite Javadoc: https://static.runelite.net/api/client/ (api module) and https://static.runelite.net/api/runelite-client/ (client module)
- OSRS Wiki (item/NPC/object IDs): enable "Advanced data" in infoboxes
- Decompiled client scripts: https://github.com/runelite/cs2-scripts
- Cache data / IDs: https://chisel.weirdgloop.org/
- RuneLite Discord `#development` channel for questions

## Notes for AI Agents

**Legal:**
- This plugin uses a strict whitelist. Check `docs/runelite-wiki/rejected-features.md` before writing any new feature. If the condition or display type is not in the approved list, do not implement it.
- `docs/jagex-guidelines.md` is the primary source of truth (official Jagex rules). `rejected-features.md` distills them into a whitelist for this project.
- `docs/runelite-wiki/rejected-features.md` is **immutable** - never modify it.
- All conditions track `client.getLocalPlayer()` only. Never read other players' state.
- User input: item names and NPC names are allowed in the UI. Raw numeric IDs as free-form user input are not allowed.

**RuneLite API:**
- All overlays must be registered in `startUp()` and unregistered in `shutDown()` - not optional
- The plugin panel must be added to `ClientToolbar` in `startUp()` and removed in `shutDown()`
- Game state may **only** be accessed from the client thread; schedule work with `clientThread.invokeLater()` if needed outside event handlers
- Prefer event-driven re-evaluation over polling; `GameTick` is used only for location conditions (#17-18)
- Do not put direct RuneLite API calls into persisted model classes; keep them in runtime evaluators/controllers only

**Code:**
- Plugin package: `nl.jessedezwart.strongbuffs`; main class: `StrongBuffsPlugin`; config: `StrongBuffsConfig`
- Java only; tabs; Allman braces; Lombok `@Slf4j` for logging
- Do not modify `docs/runelite-wiki/rejected-features.md` - it is the immutable legal whitelist
- Prefer deterministic versions in Gradle and avoid `latest.release` once the project moves beyond scaffolding
