# Strong Buffs Agent Notes

## Read These First

- `README.md`
- `docs/jagex-guidelines.md`
- `docs/rejected-features.md`

If you are adding or changing a condition, also read:

- `skills/new-condition/SKILL.md`

## Source Of Truth

- Product and architecture overview: `README.md`
- Legal constraints and project whitelist: `docs/jagex-guidelines.md` and `docs/rejected-features.md`
- Task-specific workflows: `skills/*/SKILL.md`

## Hard Rules

- If a feature is not clearly allowed by `docs/rejected-features.md`, do not implement it.
- Never modify `docs/runelite-wiki/rejected-features.md`.
- Track only the local player's own state. Never use other-player data.
- Keep persisted definitions free of direct RuneLite API access.
- Prefer event-driven tracking over polling.
- Register overlays and panel UI in plugin startup/shutdown correctly.

## Working Style

- Read the existing implementation before changing architecture.
- Keep changes narrow and consistent with the current codebase.
- Add or update tests when behavior changes.
- Test after every change to ensure no regressions.

## Commands

- `./gradlew run`
- `./gradlew build`
- `./gradlew test`

## Implementation notes
- Java 11
- tabs, Allman braces
