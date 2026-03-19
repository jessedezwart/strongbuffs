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

DefinitionCatalog       <- source of truth for approved condition/action definitions
RuleDefinitionStore     <- serializes versioned RuleDefinition list to/from RuneLite config
ConditionEditorRegistry <- builds condition editor metadata/components from the catalog
ActionEditorRegistry    <- builds action editor metadata/components from the catalog
RuleRepository          <- persisted rule access plus runtime synchronization
RuleDraftSession        <- current edit draft and selection
RuleDraftValidator      <- validates draft rules before save
RulePanelController     <- facade over repository, draft session, validation, and unsaved-change flow
RuntimeConditionRegistry <- maps condition classes to runtime matchers, requirements, and value formatters
RuntimeConditionRequirementCollector <- derives selective watchlists from enabled rules
RuntimeConditionTracker <- RuneLite event subscriber that maintains cached runtime state
RuleCompiler            <- compiles rules to trigger-aware runtime rules
RuleEngine              <- applies activation mode and cooldown semantics
ActionDispatcher        <- dispatches compiled actions to runtime action handlers
RuleRuntimeController   <- wires compiler, tracker, engine, and action dispatcher together
StrongBuffsPlugin       <- wires toolbar + panel + runtime controller
```

### Current Repo State

- The repo now has a full vertical slice: persistence, editor UI, model validation, selective RuneLite-backed runtime state tracking, compiled rules, trigger indexing, activation/cooldown handling, overlay rendering, screen flash handling, and sound dispatch.
- `ConditionDefinition` and `ActionDefinition` implementations are persisted data plus editor/validation metadata only. They must not access RuneLite directly.
- Runtime behavior is centralized in registries and updaters. If a condition needs in-game data, the mapping belongs in runtime services, not in the persisted model class.

### Runtime Model

Persisted definitions are pure data. They are safe to serialize, diff, validate, and migrate.

Current runtime evaluation is split into clearly separated steps:

1. `DefinitionCatalog`
   - maps approved type IDs to condition/action classes
   - provides metadata instances for the editor
   - creates default instances for new rows in the panel
2. `ConditionRuntimeRegistry`
   - maps each condition class to a matcher
   - declares which runtime requirements that condition needs
   - formats live values for actions such as overlay text
3. `RuntimeConditionRequirementCollector`
   - walks enabled rule trees and builds a selective `RuntimeConditionRequirements` watchlist
4. `RuntimeConditionTracker`
   - subscribes to RuneLite events
   - refreshes only the runtime slices required by the active rules
   - stores cached data in `RuntimeState`
5. `ConditionChecker`
   - evaluates persisted condition trees against cached `RuntimeState`
6. `RuntimeRequirementPlanner`
   - maps a `RuntimeConditionRequirements` watchlist to the RuneLite subscriptions and runtime triggers it implies
   - is the single source of truth for tracker listener wiring and engine invalidation policy
7. `RuleCompiler`
   - turns validated rules into `CompiledRule` objects
   - precomputes each rule's requirements and trigger plan through `RuntimeRequirementPlanner`
   - builds a `RuleTriggerIndex` so the engine only reevaluates affected rules
8. `RuleEngine`
   - handles `WHILE_ACTIVE`, `ON_ENTER`, and `ON_EXIT`
   - enforces cooldowns
   - activates, updates, and clears actions through `ActionDispatcher`
9. `ActionDispatcher` and `RuntimeActionHandlerRegistry`
   - route each action type to its runtime handler
   - own overlay/screen-flash/sound lifecycle

`RuntimeState` is the cached snapshot used for evaluation. It is split into skill, var, inventory, ground-item, and location slices.

### Condition To Game-State Mapping

Conditions are mapped to in-game values in `ConditionRuntimeRegistry`, not in the condition classes themselves.

- Numeric conditions such as HP, prayer points, spec, run energy, skill level, and item count are mapped by reading the relevant value from `RuntimeState` and comparing it with the condition operator and threshold.
- Boolean or structured conditions such as prayer active, poison state, slayer task state, XP gain, item presence, zone membership, and instance state each have dedicated runtime matchers in the registry.
- The same registry also declares which runtime slices those conditions need. That drives the watchlist used by `RuntimeConditionTracker`.

This means adding a condition is usually a two-part change:
- add a persisted condition definition for editor/persistence concerns
- add runtime registration and, if needed, new tracked state for evaluation concerns

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

`RuleDefinitionStore` owns all serialize/deserialize logic. It uses the `DefinitionCatalog` to resolve condition and action type IDs through polymorphic Gson adapters.

Persistence rules:

- Every saved rule includes `schemaVersion`
- `RuleDefinitionStore` is responsible for migrations from older schema versions
- Unknown or invalid nodes must fail closed and be ignored, never partially executed
- User-entered names may be accepted in the UI, but persisted data should prefer canonical internal identifiers where practical

On `startUp`: load and deserialize from config. On any save: serialize and write back.

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

The panel edits a `RuleDraft`, not persisted runtime state. `RulePanelController` is a facade over:

- `RuleRepository` for persisted rule access and runtime synchronization
- `RuleDraftSession` for the current draft and selected rule
- `RuleDraftValidator` for save-time validation
- `UnsavedChangesGuard` for deferred navigation/create/delete flows

Do not mutate live runtime objects directly from Swing components.

## Runtime Check Work

Current runtime implementation:

- `RuntimeState` is an aggregate root around split state slices:
  `SkillRuntimeState`, `VarRuntimeState`, `InventoryRuntimeState`, `GroundItemRuntimeState`, and `LocationRuntimeState`
- `RuntimeConditionTracker` owns event subscriptions and delegates state mutation to focused updater classes
- `ConditionRuntimeRegistry` is the runtime mapping layer for condition evaluation, requirement planning, and live-value formatting
- `ConditionChecker` recursively evaluates `ConditionGroup` trees against cached `RuntimeState`
- `RuntimeRequirementPlanner` is the shared mapping from requirement watchlists to RuneLite subscriptions and runtime triggers
- `RuleCompiler` and `RuleTriggerIndex` reduce reevaluation work to rules affected by the incoming runtime triggers
- `RuleEngine` applies activation mode and cooldown semantics, then calls `ActionDispatcher`

Current RuneLite wiring:

- `StatChanged` for HP, prayer points, real skill levels, and XP gain
- `VarbitChanged` for tracked prayers, spec, poison, and slayer task state
- `ItemContainerChanged` for inventory and equipment conditions
- `ItemSpawned` / `ItemDespawned` for ground-item conditions
- `GameTick` only when run energy, XP gain tick state, player location, or instance checks are required
- `GameStateChanged` to refresh or clear tracked state on login transitions

## New Condition Workflow (`/new-condition`)

1. Confirm the condition is in the approved list in `docs/runelite-wiki/rejected-features.md`
2. Identify the triggering event and VarBit/API from `docs/runelite-wiki/vars.md` and `events-reference.md`
3. Add a persisted condition definition class under `model/condition/impl/`
4. Keep that class limited to:
   - persisted fields
   - editor label/description/fields
   - validation
   - copying
   - `getTypeId()`
5. Register the new condition in `DefaultDefinitionCatalog`
   - add metadata instance
   - add factory supplier
6. Register the runtime behavior in `ConditionRuntimeRegistry`
   - matcher
   - requirement contributor
   - live-value formatter if the action UI needs one
7. If the condition only uses existing runtime slices, stop there
8. If it needs new data:
   - extend `RuntimeConditionRequirements`
   - extend the relevant `RuntimeState` slice or add a new slice
   - update the relevant updater class, or add a new updater if the concern does not fit an existing one
   - make sure `RuntimeConditionTracker` refreshes and emits the correct runtime triggers
   - update `RuntimeRequirementPlanner` if the new requirement needs new runtime triggers or listener subscriptions
9. Add tests for:
   - serialization/deserialization through `RuleDefinitionStore`
   - requirement collection
   - condition evaluation through `ConditionChecker`
   - updater/tracker behavior if new runtime state was introduced
   - rule-engine behavior if the condition has edge cases around transitions or cooldowns
10. Update this guide if the architecture or workflow changed materially

### Adding Actions

Action work follows the same split:

1. Add the persisted `ActionDefinition` implementation
2. Register it in `DefaultDefinitionCatalog`
3. Add editor metadata support if needed
4. Implement a `RuntimeActionHandler<T>`
5. Register that handler in `RuntimeActionHandlerRegistry`
6. Add tests for persistence, handler behavior, and lifecycle

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
- Java only; tabs; Allman braces; Lombok `@Slf4j` for logging
- Do not modify `docs/runelite-wiki/rejected-features.md` - it is the immutable legal whitelist
- Prefer deterministic versions in Gradle and avoid `latest.release` once the project moves beyond scaffolding
