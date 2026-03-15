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
- Evaluate rules from **cached runtime state**, not direct ad hoc reads from persisted model classes
- Support both **steady-state** and **transition-based** activations
- Keep editor, persistence, runtime checks, and rendering concerns separated
- Fail closed when data is invalid, unknown, or out of policy

### Overview

```
RuleDefinition          <- one user-created rule (persisted as JSON)
  |- rootGroup          <- tree of ConditionGroup / ConditionDefinition nodes
  |- action             <- typed ActionDefinition
  |- activationMode     <- WHILE_ACTIVE / ON_ENTER / ON_EXIT
  `- cooldownTicks      <- optional activation suppression

RuleDefinitionStore     <- serializes versioned RuleDefinition list to/from RuneLite config
DefinitionRegistry      <- approved condition/action model catalog
ConditionEditorRegistry <- builds condition editor metadata/components
ActionEditorRegistry    <- builds action editor metadata/components
RulePanelController     <- owns persisted rules, draft state, and validation
StrongBuffsPanel        <- PluginPanel sidebar: list/create/edit/delete rules
RuntimeConditionRequirements <- selective watchlist derived from enabled rules
RuntimeConditionTracker <- RuneLite event subscriber that maintains runtime state
RuntimeState            <- aggregate cached local-player state for runtime evaluation
ConditionChecker        <- evaluates condition trees against RuntimeState
StrongBuffsPlugin       <- wires toolbar + panel
StrongBuffsConfig       <- plugin config stub
```

### Current Repo State

- The repo currently has persistence, editor UI, model validation, runtime condition evaluation, and selective RuneLite-backed runtime state tracking.
- It does **not** yet have a full rule engine, trigger index, compiled-rule layer, overlays, or sound playback controllers.
- Current runtime work should build on `runtime/RuntimeConditionTracker`, `runtime/RuntimeState`, and `runtime/ConditionChecker`.

### Package Structure

```
nl.jessedezwart.strongbuffs/
  StrongBuffsPlugin.java
  StrongBuffsConfig.java
  RuleDefinitionStore.java
  model/
    action/
      ActionDefinition.java
      impl/
        OverlayTextAction.java
        ScreenFlashAction.java
        SoundAlertAction.java
    condition/
      ComparisonOperator.java
      ConditionDefinition.java
      ConditionEditorOptions.java
      NumericConditionDefinition.java
      impl/
        HpCondition.java
        PrayerPointsCondition.java
        PrayerActiveCondition.java
        SpecialAttackCondition.java
        RunEnergyCondition.java
        PoisonCondition.java
        SlayerTaskCondition.java
        SkillLevelCondition.java
        XpGainCondition.java
        ItemInInventoryCondition.java
        ItemCountCondition.java
        ItemEquippedCondition.java
        GroundItemCondition.java
        PlayerInZoneCondition.java
        PlayerInInstanceCondition.java
      tree/
        ConditionNode.java
        ConditionGroup.java
        ConditionLogic.java
    editor/
      EditorField.java
    registry/
      DefinitionRegistry.java
    rule/
      RuleDefinition.java
      ActivationMode.java
  panel/
    editor/
      ActionEditorRegistry.java
      ActionEditorSupport.java
      ConditionEditorRegistry.java
      EditorFieldComponentFactory.java
    state/
      RuleDraft.java
      RulePanelController.java
      RuleValidationResult.java
      RuleDescriptions.java
      RuleControllerActionResult.java
      UnsavedResolution.java
    view/
      StrongBuffsPanel.java
      RuleListPanel.java
      RuleEditPanel.java
      ConditionGroupPanel.java
      ConditionRowPanel.java
  runtime/
    RuntimeConditionRequirements.java
    RuntimeConditionTracker.java
    RuntimeState.java
    ConditionChecker.java
    state/
      SkillRuntimeState.java
      VarRuntimeState.java
      InventoryRuntimeState.java
      GroundItemRuntimeState.java
      LocationRuntimeState.java
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
    ActionDefinition action;
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

// What to do when the rule fires
abstract class ActionDefinition
{
}

// Runtime snapshot used by condition evaluation
class RuntimeState
{
    SkillRuntimeState skills;
    VarRuntimeState vars;
    InventoryRuntimeState inventory;
    GroundItemRuntimeState groundItems;
    LocationRuntimeState location;
}
```

**Condition logic example:**
```
rootGroup (AND)
  |- PrayerPointsCondition (< 10)
  `- nestedGroup (OR)
     |- PrayerActiveCondition (Protect from Magic active)
     `- PlayerInZoneCondition (Edgeville area)
```

### Runtime Model

Persisted definitions are pure data. They are safe to serialize, diff, validate, and migrate.

Current runtime evaluation is split into:

- `RuntimeState`: aggregate root for the split runtime slices
- `ConditionChecker`: recursive tree walker for `ConditionGroup`
- `ConditionDefinition.matches(RuntimeState)`: leaf condition evaluation lives on the condition implementations
- `ConditionDefinition.contributeRequirements(...)`: leaf conditions declare the runtime data they require
- `RuntimeConditionRequirements`: immutable watchlist derived from enabled persisted rules
- `RuntimeConditionTracker`: subscribes to RuneLite events and populates only the state slices required by the current rule set

The full activation/cooldown rule engine described earlier in this file is still planned work, not current repo state.

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

`RuleDefinitionStore` owns all serialize/deserialize logic. Polymorphic Gson type adapters handle `ConditionNode` and `ActionDefinition` subtypes.

Persistence rules:

- Every saved rule includes `schemaVersion`
- `RuleDefinitionStore` is responsible for migrations from older schema versions
- Unknown or invalid nodes must fail closed and be ignored, never partially executed
- User-entered names may be accepted in the UI, but persisted data should prefer canonical internal identifiers where practical

On `startUp`: load and deserialize from config. On any save: serialize and write back.

## Condition Reference

Current repo condition set: 15 conditions. All track the local player only.

| # | Class | What it checks | Intended runtime source |
|---|-------|---------------|-------------------------|
| 1 | `HpCondition` | HP points below/above X | boosted HP level |
| 2 | `PrayerPointsCondition` | Prayer points below/above X | prayer points |
| 3 | `PrayerActiveCondition` | Specific prayer active/inactive | prayer varbits |
| 4 | `SpecialAttackCondition` | Special attack energy below/above X% | spec varp |
| 5 | `RunEnergyCondition` | Run energy below/above X% | run energy varp |
| 6 | `PoisonCondition` | Poison or venom active | poison/venom state |
| 7 | `SlayerTaskCondition` | Task active or kills remaining check | slayer vars |
| 8 | `SkillLevelCondition` | Real skill level above/below X | real skill level |
| 9 | `XpGainCondition` | XP gained in a specific skill | `StatChanged` |
| 10 | `ItemInInventoryCondition` | Item present in inventory by name | inventory container |
| 11 | `ItemCountCondition` | Inventory item count above/below X | inventory container |
| 12 | `ItemEquippedCondition` | Item equipped by name | equipment container |
| 13 | `GroundItemCondition` | Ground item nearby by name | ground item cache |
| 14 | `PlayerInZoneCondition` | Player inside a defined tile rectangle | world location |
| 15 | `PlayerInInstanceCondition` | Player is in a private instance | world view instance flag |

Removed from the repo condition catalog:

- `WildernessCondition`
- `QuestProgressCondition`
- `BoostedSkillCondition`

Do not assume removed conditions are still part of the current implementation unless the user explicitly asks to add them back.

## Action Types

Current repo action set: 3 actions.

| Class | What it represents |
|------|---------------------|
| `OverlayTextAction` | Overlay text label with optional live value |
| `ScreenFlashAction` | Screen flash configuration |
| `SoundAlertAction` | Sound preset + volume |

## Panel UI

The sidebar panel (`StrongBuffsPanel`) is added to the client toolbar on `startUp` and removed on `shutDown`.

```
[ + New Rule ]
----------------------------------
> Low HP Warning      [Edit] [X]
  Spec at 100%        [Edit] [X]
  Prayer Active       [Edit] [X]

- Edit view (RuleEditPanel) -

Name: [Low HP Warning__________]

Conditions:
[ AND v ]
  HP  [ below v ] [ 30 ] pts   [X]
  [ OR v ]
    Prayer active [ Thick Skin ] [x]
  [ + add condition ]
[ + add group ]

Activation:
Mode: [ While active v ]
Cooldown ticks: [ 0 ]

Action:
Type:  [ Screen flash v ]
Color: [########]

[ Save ]  [ Cancel ]
```

`ConditionGroupPanel` renders recursively. A `ConditionGroup` may contain nested `ConditionGroup` children, each rendered as an indented sub-panel with its own AND/OR toggle.

The panel should edit a draft model and only persist validated data on save. Do not mutate live runtime objects directly from Swing components.

## Runtime Check Work

Current runtime implementation:

- `RuntimeState` is an aggregate root around split state slices:
  `SkillRuntimeState`, `VarRuntimeState`, `InventoryRuntimeState`, `GroundItemRuntimeState`, and `LocationRuntimeState`
- `ConditionChecker` evaluates either a single `ConditionDefinition` or a recursive `ConditionGroup`
- Each condition implementation owns both:
  - `matches(RuntimeState)` for evaluation
  - `contributeRequirements(RuntimeConditionRequirements.Builder)` for declaring runtime dependencies
- `RuntimeConditionTracker` derives a selective watchlist from enabled rules and only tracks the needed data sources

Current RuneLite wiring:

- `StatChanged` for HP, prayer points, real skill levels, and XP gain
- `VarbitChanged` for tracked prayers, spec, poison, and slayer task state
- `ItemContainerChanged` for inventory and equipment conditions
- `ItemSpawned` / `ItemDespawned` for ground-item conditions
- `GameTick` only when run energy, XP gain tick state, player location, or instance checks are required
- `GameStateChanged` to refresh or clear tracked state on login transitions

Still missing:

- A trigger index
- A rule engine that handles transitions and cooldowns
- Action execution/rendering driven by runtime evaluation

## Build Order

Build in this sequence:

1. **Core model** - rule, condition tree, and action definitions
2. **Versioned storage** - `RuleDefinitionStore` with subtype adapters and schema validation
3. **Editor UI** - registries, panel state, recursive condition editing
4. **Runtime checks** - `RuntimeState` plus `ConditionChecker`
5. **Event-driven runtime** - populate `RuntimeState` from RuneLite events
6. **Rule engine** - transitions, cooldowns, and rule activation state
7. **Action execution** - overlays, flashes, sounds, and lifecycle management
8. **Testing** - serialization, editor, evaluator, runtime routing, activation transitions

## New Condition Workflow (`/new-condition`)

1. Confirm the condition is in the approved list in `docs/runelite-wiki/rejected-features.md`
2. Identify the triggering event and VarBit/API from `docs/runelite-wiki/vars.md` and `events-reference.md`
3. Add a persisted definition class in `model/condition/`
4. Register it in `DefinitionRegistry`
5. Implement `matches(RuntimeState)` on the condition
6. Implement `contributeRequirements(RuntimeConditionRequirements.Builder)` on the condition
7. If it needs a new runtime source, extend `RuntimeConditionTracker` and the relevant `runtime/state/*` slice
8. Add tests for serialization, evaluation, and requirement collection
9. Add a row to the condition reference table in this file

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
4. **Build vertically** - keep runtime, actions, and event wiring incremental and testable

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
- Prefer event-driven state updates over polling; `RuntimeConditionTracker` should only subscribe/update what enabled rules require
- Do not put direct RuneLite API calls into persisted model classes; keep them in runtime evaluators/controllers only

**Code:**
- Plugin package: `nl.jessedezwart.strongbuffs`; main class: `StrongBuffsPlugin`; config: `StrongBuffsConfig`
- Java only; tabs; Allman braces; Lombok `@Slf4j` for logging
- Do not modify `docs/runelite-wiki/rejected-features.md` - it is the immutable legal whitelist
- Prefer deterministic versions in Gradle and avoid `latest.release` once the project moves beyond scaffolding
