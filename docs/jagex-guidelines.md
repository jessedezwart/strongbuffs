# Jagex Third-Party Client Guidelines

**Source:** https://secure.runescape.com/m=news/third-party-client-guidelines?oldschool=1
**Published:** 01 June 2022

Only two third-party clients are officially approved: **RuneLite** and **HDOS**.

> Using any unapproved client feature risks permanent account action.
> Jagex acknowledges it is impossible to list every potential feature; general principles apply alongside specific examples.

---

## Prohibited: Boss Fight Features

Applies to Raids sub-bosses, Slayer bosses, Demi-bosses, wave-based minigames, Fight Caves, and the Inferno.

- Next attack prediction (timing or attack style)
- Projectile targets, target locations, or impact locations
- **Prayer switching indicators**
- Attack counters
- Automatic stand/don't-stand indicators
  - *(Manually placed tile markers remain permitted)*

## Prohibited: Menu Changes

- Any addition of new menu entries which cause actions to be sent to the server
  - *(Max Cape and Achievement diary capes are excepted)*
- Menu option changes for Construction, Blackjacking, and Attack (or similar PvP) options

## Prohibited: Interface Changes

- Unhiding interface components (e.g. special attack bar or minimap in Barrows)
- Moving or resizing click zones for 3D components
- Moving or resizing click zones under: combat options, inventory, worn equipment, spellbook
- Resizing click zones under the prayer book

---

## Full Prohibited Features Table (from Jagex)

| Feature | Category |
|---------|----------|
| Indicates where projectiles will land | Combat |
| Indicates the timing of a boss mechanic's start or end | Combat |
| Adds visual or audio boss mechanic indicators (except manually-configured external helpers) | Combat |
| Indicates what prayers to use and in what order (e.g. Cerberus) | Combat |
| Indicates opposing clan players in PvP | Combat |
| Helps timing a flinch on an opponent | Combat |
| **Indicates how long an opponent is frozen for** | Combat |
| Auto-indicates where or not to stand in boss fights (manual tile markers exempt) | Combat |
| Easier 3D spell-targeting by removing options | Combat |
| Indicates which player an NPC is focused on | Combat |
| Shows other players what items or loot will drop in PvP | Combat |
| Indicates whom your opponent's opponent is in PvP | Combat |
| **Indicates which prayer to use in any combat situation** | Combat |
| Resizes Spellbook interface components | Combat |
| Resizes Prayer interface components | Combat |
| Provides extra information about others for PvP scouting | Combat |
| Gives summary information about a group of players (e.g. how many are attackable) | Combat |
| Removes or deprioritises attack or cast options from PvP minimenu | Combat |
| Reveals the maze layout in Sotetseg (Theatre of Blood) | Combat |
| Adds menu entries causing server actions (Max Cape / Achievement diary capes excepted) | Menus |
| Modifies menu options for blackjacking (Pickpocket, Knock-out) | Menus |
| Reorders or removes player-based options such as 'Trade with' | Menus |
| Offers world interaction in any detached camera mode | Misc |

> **This table is not exhaustive.** Features that act similarly to those described can also be considered unacceptable. Jagex reserves the right to add prohibited features.

---

## Additional Restrictions (RuneLite Policy, beyond Jagex rules)

These are RuneLite's own rules for Plugin Hub submissions:

| Feature | Reason |
|---------|--------|
| Opponent freeze timers | Prohibited by Jagex guidelines |
| Spellbook resizing | Forbidden per 2024-04-17 Jagex update |
| Highlighting offline friends | Privacy / harassment |
| Mouse Keys plugin | Only OS default mouse keys allowed |
| Dynamic Puro-Puro spawns | Too powerful |
| AFK agility training aids | AFK abuse susceptibility |
| Level-based PvP player indicators | Targeting attackable players |
| New high-end PvM boss plugins | Trivializes content |
| Crowdsourcing player data (locations, gear, names) | Privacy |
| Programmatic chat insertion | Considered autotyping |
| Outgoing chat message modification | Same |
| Plugins exposing player info over HTTP | Privacy/security |
| Plugins simulating game content | e.g. Leviathan simulation in Quest Helper |
| Credential manager plugins | Account security risk |
| ID-based plugins with user-provided IDs | Moderation issues |
| Touchscreen/Controller plugins | May trigger macro detection |
| Adult/sexual content | No |
| Kotlin, Scala, or other JVM languages | Reviewer burden; build tooling |
| Java reflection | Cannot review all executed code |
| JNI (Java Native Interface) | Same |
| Execution of external processes | Same |
| Downloading or running code at runtime | Same |
