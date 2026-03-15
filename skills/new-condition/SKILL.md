---
name: new-condition
description: Scaffold a new Aura condition type for the Strong Buffs plugin. Use this when the user wants to add a new trigger — such as HP threshold, prayer active, special attack ready, item in inventory, or skill level change. Do not use for building or running the plugin.
---

The user will describe the condition they want. Follow these steps in order:

## Step 1 — Legal check
Read `docs/runelite-wiki/rejected-features.md`.
Verify the condition does not relate to:
- Opponent freeze timers
- Boss mechanics (rotation, phase, timing helpers)
- AFK assistance
- PvP player targeting by combat level

If the condition is illegal or a gray area, explain why and stop. Do not write code for illegal features.

## Step 2 — Identify the RuneLite API
Read `docs/runelite-wiki/vars.md` and `docs/runelite-wiki/developer-guide.md`.
Determine:
- Which event(s) to subscribe to (`VarbitChanged`, `StatChanged`, `GameTick`, `ItemContainerChanged`, etc.)
- Which VarBit/VarPlayer/client method provides the relevant game state
- Whether the condition needs to poll on `GameTick` or react to a specific event

## Step 3 — Check existing patterns
Read the current files in `src/main/java/` to understand the existing `Condition` interface/base class and `AuraManager` registration pattern before writing new code.

## Step 4 — Implement
Create the new `Condition` subclass in `src/main/java/` following:
- Java only (no Kotlin or other JVM languages)
- Tabs for indentation, Allman-style braces (opening brace on new line)
- Lombok `@Slf4j` if logging is needed
- BSD-2-Clause copyright header
- `@Subscribe` on event methods

## Step 5 — Wire up
- Register the condition in `AuraManager`
- Add a config entry in `StrongBuffsConfig` if the condition requires user-settable parameters (e.g. the HP threshold value)
