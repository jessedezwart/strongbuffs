# Strong Buffs

A [RuneLite](https://runelite.net) Plugin Hub plugin that brings WeakAuras-style conditional overlays to Old School RuneScape.

Define **Auras** — visual and audio indicators that activate when configurable in-game conditions are met. Instead of hardcoded overlays that always show, Auras only fire when the player decides they should.

**Examples:**
- Flash the screen red when HP drops below 30%
- Show a warning when prayer points fall under 10
- Play a sound when special attack energy hits 100%
- Alert when entering the wilderness with a certain item equipped


## Legality

This plugin is built for and submitted to the [RuneLite Plugin Hub](https://runelite.net/plugin-hub). It follows a pretty strict approach to feature development:

- All features comply with the [Jagex Third-Party Client Guidelines](https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1)
- All features comply with RuneLite's Plugin Hub submission policy
- The approved feature list is documented in [`docs/runelite-wiki/rejected-features.md`](docs/runelite-wiki/rejected-features.md)
- **If a feature is not on the approved list, it is not implemented — no exceptions**
- Always think about how a feature may be abused. If it has potential for abuse, it is not implemented.

Hard bans (non-negotiable, will never be added):
- Prayer switching indicators in combat
- Boss attack timing, rotation, or phase helpers
- Opponent freeze timers
- Any information about other players for PvP purposes

All conditions track **your own player's state only**.

## Using AI in This Project

AI tools (Claude, Copilot, etc.) are permitted for development — with one non-negotiable rule:

> **You must be able to explain every line you commit. If you cannot explain it, do not merge it.**

AI-generated code carries the same responsibility as hand-written code. "The AI wrote it" is not a defence for a bug.

**Before merging any AI-generated code:**
- Read it line by line and understand what it does
- Verify it does not violate the legal whitelist in `docs/runelite-wiki/rejected-features.md`
- Confirm it follows the code conventions in `docs/runelite-wiki/code-conventions.md`

AI is a productivity tool, not a substitute for review.

This project is not affiliated with Jagex Ltd. or RuneLite.
