# Actor / Thing / Round Model (Design Note)

This is a north star for evolving the engine toward a clean Actor/Thing/Round model. It documents abstractions, current actors/things, verb vocabulary, and a future round flow. No code changes.

## 1) Core abstractions

### Thing
- Identity: id.
- TextFace: name / short / detail / history.
- Stats: numeric attributes.
- Inventory: list of child Things (items, traces, statuses).
- Log/history: events involving this Thing (traceable later via SEARCH/INSPECT).
- Capabilities: which verbs it sensibly supports (search, climb, open, harvest, etc.).

### Actor (extends Thing)
- Extra attributes: STR/AGI/CHA/AWR/CON, etc.
- Inventory of Items.
- Control mode: HUMAN vs AI/Agent.
- Issues intents/commands each round (verb + targets).

### Item (extends Thing)
- Size/weight/fuel/durability; tags (tool, resource, raft_critical, bait, hazard, etc.).
- Used/thrown/consumed by Actors.
- May have action hooks (e.g., skeleton drop, key/lock, chain, etc.).

## 2) Current actors and things

Actors:
- PlayerCharacter.
- Ghost (Smoke Walker).
- MonkeyTroop.
- (Future) AI survivor / rival.

Things:
- Places: Camp, BambooGrove, VineForest, Cave*, Shipwreck*, BarrelsShore, Mountain*, QuicksandTile, etc. (* future additions).
- Items: machete, torch, bananas, vines, barrels, bamboo stands/poles, silk, raft parts, key, chain, etc.
- Status/trace Things: poo, broken branches, ghost traces, monkey evidence.

## 3) Verb vocabulary

Generic verbs the engine/tools should support conceptually:
- move_to(actor, tile)
- inspect/search(actor, thing)
- take/drop(actor, item)
- use(actor, item, [target]) (cut, unlock, light, etc.)
- throw(actor, item, target)
- harvest(actor, thing) (bamboo stand, vines, silk)
- craft(actor, recipe, items[])
- climb/dive(actor, thing)
- intimidate(actor, targetActor)
- shout/listen(actor[, target])
- launch(actor, raftThing)

Current engine verb mapping:
- MOVE_PLAYER ← move_to
- TRANSFER_ITEM ← take/drop/use/steal/throw outcomes
- SET_FLAG ← statuses, modes, simple booleans (ghostAwakened, sleeping, POOED)
- RUN_ITEM_HOOK ← special item/thing scripts (skeleton, locks/chains, etc.)
- CHECK ← generic checks (PERCEPTION, HEARING, INTIMIDATION, GENERIC, etc.)

## 4) Round model

Current v1 flow (per turn):
1) Player command → DM decision/actions → apply.
2) Ghost turn.
3) Monkey turn.
4) Recent checks flow into next DM input.

Future v2 concept (for multi-actor/zero-player sims):
- Round N:
  - All Actors (human + AI) choose intents (verb + targets).
  - Engine runs necessary CHECKs.
  - Engine resolves actions in deterministic order (initiative/priority).
  - Engine updates Things and Actors.
  - DM narrates based on resulting state.
- Goal: support multi-human, many Actors per step, and zero-player “war game” sims without changing the verb/CHECK substrate.

## 5) Logs & evidence

For each Thing, log notable events:
- “MonkeyTroop threw poo here on turn 7.”
- “Ghost stole barrel here turn 10.”

DM/agents can answer queries like “Is there evidence monkeys have been here?” via:
- SEARCH/INSPECT verb → CHECK(PERCEPTION) → use logs + check result to decide what the player learns.

## 6) Usage

- Use this doc as reference when evolving core toward Actor/Thing/Round v2.
- Keep v1 contracts (DM/Ghost/Monkey/Player DTOs, verbs, CHECKs) stable while iterating.
- When adding new features (more AI players, richer world), anchor them to these abstractions and the verb/CHECK layer.
