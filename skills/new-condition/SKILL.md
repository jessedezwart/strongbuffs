---
name: new-condition
description: Add a new approved condition type to the Strong Buffs plugin. Use this when the user wants a new local-player trigger such as prayer active, item in inventory, or zone checks. Do not use for build/run-only requests.
---

The user will describe the condition they want. Follow these steps in order.

## Step 1 - Legal check
Read `docs/runelite-wiki/rejected-features.md` and `docs/jagex-guidelines.md` and `README.md`.

Check if the condition is allowed. If not, decline to implement and explain to the user why.

## Step 2 - Identify the trigger and API
Determine:
- which RuneLite event should trigger re-evaluation, check README.md for the API resources.
- which approved API/VarBit/VarPlayer/client method provides the state
- whether cached runtime state is needed
- whether the condition belongs in var, stat, item-container, ground-item, or location-triggered routing

Do not default to polling. Use event-driven updates unless the condition is explicitly location-based.

## Step 3 - Check existing architecture
Read the current implementation before changing anything. Check what areas need to be changed. This will likely include:
- persisted model class for the condition definition
- registry entry for the condition
- runtime evaluator class for the condition

## Step 4 - Implement

Implement!

## Step 5 - Add tests
Add tests, if possible, for the new condition.
