# AI MUD Island – Tri-Agent Console Demo (DM + Ghost + Monkeys + Player)

Use this as a repeatable, talk-sized path you can drive by hand (human mode) or let the AI player run (AI-only mode). No engine tweaks needed.

## Setup

1) Env:
```
export OPENAI_API_KEY=...        # if using Spring AI DM/Ghost/Monkey/Player
```

2) Run (default human mode):
```
mvn exec:java \
  -Dexec.mainClass=com.demo.island.console.Application \
  -Dplayer.ai=false
```
or AI-only:
```
mvn exec:java \
  -Dexec.mainClass=com.demo.island.console.Application \
  -Dplayer.ai=true \
  -Dsim.turnCap=50
```

> Notes:
> - `player.ai=true` uses the Player Brain prompt to generate commands each turn.
> - Ghost/Monkey agents always run via Spring AI if configured.
> - A safety cap stops the sim at maxTurns or `sim.turnCap`.

## Human-mode script (you type)

World: Camp (start, LIGHT) → Bamboo (skeleton + machete) → Vines (monkeys + vines)

Sequence and beats:

1) At Camp (LIGHT)
```
LOOK
```
- DM: camp description.
- Status: Turn 0, LIGHT.

2) Move to Bamboo Grove
```
GO N
LOOK
```
- DM: bamboo description; skeleton present.
- Status: Turn 1, LIGHT.

3) Disturb skeleton, get machete, awaken ghost
```
MOVE SKELETON
TAKE MACHETE
```
- DM: RUN_ITEM_HOOK + SET_FLAG ghostAwakened, machete drops.
- Ghost: awakened; still idle in LIGHT, but watch later phases.
- Status: ghostAwakened should be true.

4) Return to Camp
```
GO S
```
- DM: back to camp.

5) Head to Vine Forest
```
GO E
LOOK
```
- DM: vines + monkeys; vine bundle present.
- Monkeys: ACTIVE mode; may emit follow/steal/poo intents if bananas appear.

6) Nudge time toward DUSK/DARK
```
LOOK
LOOK
```
(or small moves between tiles)
- Watch status line: timePhase shifts to DUSK then DARK as turns advance.
- Ghost: HEARING checks at DUSK/DARK → targetTileId toward player on success.
- Monkeys: SETTLING → SLEEPING; intents quiet down at night.

Optional banana beat:
- If you seed a banana in inventory or tile, monkeys may try STEAL_BANANA_FROM_PLAYER.

End condition:
- Stop when you reach DUSK/DARK and have shown ghost/monkey intent logs, or when maxTurns/turnCap hits. No need to finish a raft in this demo.

## AI-only variant

Run with:
```
mvn exec:java \
  -Dexec.mainClass=com.demo.island.console.Application \
  -Dplayer.ai=true \
  -Dsim.turnCap=50
```
- Let the AI player drive; observe logs:
  - Per turn: turn/timePhase, AI command, DM narration, ghost/monkey hints.
- Stop when RESULT=... prints (Midnight or safety stop).

## What to call out during the demo

- Verb hygiene: DM uses MOVE_PLAYER/TRANSFER_ITEM/SET_FLAG/RUN_ITEM_HOOK/CHECK; Ghost/Monkeys stick to intents + CHECK.
- Hook: Skeleton MOVE reveals machete and sets ghostAwakened.
- Checks: Ghost HEARING at DUSK/DARK; Monkeys GENERIC/INTIMIDATION (when present).
- Phase behavior: Ghost gets more reactive at night; Monkeys settle/sleep unless poked.
- Monkey effects: banana steal transfers ownership; poo sets POOED flag; return-home sets sleeping flag/target.
