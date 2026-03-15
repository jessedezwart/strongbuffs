---
name: new-condition
description: Add a new approved condition type to the Strong Buffs plugin. Use this when the user wants a new local-player trigger such as prayer active, item in inventory, or zone checks. Do not use for build/run-only requests.
---

The user will describe the condition they want. Follow these steps in order.

## Step 1 - Legal check
Read `docs/runelite-wiki/rejected-features.md` and, if needed, `docs/jagex-guidelines.md`.

Reject the request if it is not explicitly allowed or if it touches banned areas such as:
- Opponent freeze timers
- Prayer switching helpers in combat
- Boss rotations, phases, or timing helpers
- PvP information about other players
- AFK assistance

All conditions must track the local player only.

## Step 2 - Identify the trigger and API
Read the relevant docs in `docs/runelite-wiki/`, especially:
- `vars.md`
- `events-reference.md`
- `developer-guide.md`

Determine:
- which RuneLite event should trigger re-evaluation
- which approved API/VarBit/VarPlayer/client method provides the state
- whether cached runtime state is needed
- whether the condition belongs in var, stat, item-container, ground-item, or location-triggered routing

Do not default to polling. Use event-driven updates unless the condition is explicitly location-based.

## Step 3 - Check existing architecture
Read the current implementation before changing anything. The relevant areas are:
- `src/main/java/nl/jessedezwart/strongbuffs/model/condition/`
- `src/main/java/nl/jessedezwart/strongbuffs/model/condition/impl/`
- `src/main/java/nl/jessedezwart/strongbuffs/model/registry/DefinitionRegistry.java`
- `src/main/java/nl/jessedezwart/strongbuffs/panel/editor/ConditionEditorRegistry.java`
- `src/main/java/nl/jessedezwart/strongbuffs/panel/state/RulePanelController.java`
- runtime/compiler/engine classes if they exist for the condition being added

Important constraints:
- persisted model classes must stay pure data plus editor metadata/validation
- no direct RuneLite API calls in persisted model classes
- no reflection or service-loader registration
- register new types explicitly in `DefinitionRegistry`

## Step 4 - Add the persisted model
Create the new condition definition class under `src/main/java/nl/jessedezwart/strongbuffs/model/condition/impl/`.

Follow the current pattern:
- extend the correct base class, usually `NumericConditionDefinition`
- implement `getTypeId()`
- implement editor label/unit/min/max as needed
- if the condition has custom inputs, define them in the model via `getEditorFields()`
- implement `copy()`

Do not put Swing component creation in the model. The model defines editor fields; the panel layer renders them.

## Step 5 - Register the condition explicitly
Update `src/main/java/nl/jessedezwart/strongbuffs/model/registry/DefinitionRegistry.java`.

Add:
- the condition class
- its metadata instance
- its factory supplier

This project does not use reflection or `META-INF/services` for condition discovery.

## Step 6 - Wire runtime evaluation
Update the runtime/compiler path so the condition actually works:
- compiler mapping from persisted definition to runtime evaluator
- trigger routing/indexing for the relevant event source
- any runtime state caching needed for evaluation

Fail closed if required data is unavailable or invalid.

## Step 7 - Ensure panel support
If the condition follows the numeric-condition pattern, the generic editor should pick it up automatically once it is registered.

If the condition needs non-standard inputs:
- define editor fields in the model
- extend the generic editor field support only if required
- keep the rendering logic in the panel/editor layer, not the model

## Step 8 - Add tests
At minimum, add or update tests for:
- definition registry coverage
- serialization/deserialization in `RuleDefinitionStore`
- editor registry exposure if relevant
- runtime evaluation/trigger routing

Prefer updating existing registry/store tests when possible instead of creating redundant coverage.

## Step 9 - Update docs
If the condition is added, update the condition reference table in `AGENTS.md` if needed so the project guide stays accurate.

## Implementation notes
- Java only
- tabs, Allman braces
- use `apply_patch` for edits
- prefer explicit factories and registrations over magic discovery
- keep the change vertical: model, registry, runtime, tests
