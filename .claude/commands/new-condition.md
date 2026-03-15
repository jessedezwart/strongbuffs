Scaffold a new Rule condition type for the Strong Buffs plugin.

The user will provide the name and description of the condition. Use this to:

1. Read `AGENTS.md` to understand the project architecture and legal constraints
2. Read `docs/runelite-wiki/vars.md` to understand which vars/events are relevant
3. Read `docs/runelite-wiki/rejected-features.md` to check if this condition type is legal
4. Check existing source files in `src/main/java/com/example/` to understand current patterns
5. Scaffold the new condition class following RuneLite code conventions (tabs, Allman braces, Java only, BSD header)
6. Identify which RuneLite event(s) and VarBits to subscribe to
7. Write the implementation

Always:
- Verify legality against Jagex guidelines before writing any code
- Use `@Subscribe` on the appropriate event method
- Register the condition in the relevant manager class
- Follow the code conventions in `docs/runelite-wiki/code-conventions.md`
