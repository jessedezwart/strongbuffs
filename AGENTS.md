# Strong Buffs Agent Notes

## Read First

- `README.md`

If you are adding or changing a condition, also read:

- `skills/new-condition/SKILL.md`

## Source Of Truth

- Product and architecture overview: `README.md`
- Legal constraints and project whitelist: `docs/jagex-guidelines.md` and `docs/rejected-features.md`
- Task-specific workflows: `skills/*/SKILL.md`

## Help
Read relevant docs:
https://github.com/runelite/runelite/wiki/Developer-Guide
https://github.com/runelite/runelite/wiki/Creating-plugin-config-panels
https://static.runelite.net/runelite-api/apidocs/
https://static.runelite.net/runelite-client/apidocs/
https://github.com/runelite/runelite/wiki/Code-Conventions
https://github.com/runelite/runelite/wiki/Working-with-client-scripts
https://github.com/runelite/runelite/wiki/VarPlayers%2C-VarBits%2C-and-VarClients

## Hard Rules

- Never modify `docs/rejected-features.md`.
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
