# Story: Cave Torch & Spider Threat

Goal: make the cave corridor a small, dangerous side quest teaching that light is limited, items can be combined, the cave is a hoard/boundary (not a home), and danger is real.

## Requirements

### 1) Old torch behavior
- The old torch in C2 is almost burned out and one-time only.
- Player can pick it up, soak it with a small amount of kerosene from the can in C2, and ignite it with flint from C2.
- When lit: strong light for only a few turns; clearly say it is nearly spent (e.g., “The torch catches, flaring bright and thin. It is already nearly burned through.”).
- Once it burns out, it is gone and cannot be relit. No explicit turn count shown.

### 2) Torch-limited reveal in C3 (map + tally marks)
- Default (no lit torch): only vague “carved lines” text; no readable detail.
- With lit torch: show the carved island map (marks for shipwreck, camp, cave) and many five-bar tally groups.
- After torch burns out: revert to vague text on future visits without light. Map/tallies are carved into stone, cannot be taken; the torch only reveals briefly.

### 3) Spider swarm threat (burn webs → two-turn escape)
- C4 has webs/spiders only; no treasure.
- If player burns webs (with the old torch or any burning action):
  - Immediately: “You touch the torch to the web. The silk catches. Spiders start to swarm toward you.”
  - Next turn in any cave tile: short warning (“Spiders are climbing over your boots.”).
  - Following turn in any cave tile: last warning (“Spiders are on you. Get out.”).
  - If still in any cave tile after that: death (“You are swarmed by spiders. You are dead. To try again, you must start over.”).
- Escaping the cave before death step: survive; spiders stop at the mouth (“You stumble out of the cave. The spiders stop at the entrance and cling to the stone.”).
- Not burning webs: no swarm and no new content. Burning outside C4 does not trigger swarm.

### Acceptance criteria
- Old torch works as a one-shot light: pick up, soak, light; lights C3/C4; burns out after a few turns and cannot be relit.
- C3 description gated on light: vague without torch; map + tally marks with lit torch; reverts after burnout without light.
- Spider swarm triggers on burning webs in C4, gives two turns to escape, and kills if the player stays in the cave; no effect otherwise.
- Messages are short and repeatable; no new lore beyond signaling state/danger.

### Tests
- Cover: “Torch lit → C3 shows map, then torch out.”; “Burn webs then escape → live.”; “Burn webs and linger → die.”
