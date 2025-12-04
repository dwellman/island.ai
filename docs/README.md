# Island Engine – Creation Myth & Player Harness

This doc captures the current architecture in four slices: world build, gameplay loop, tool harness, and where the LLM lives. It also shows how to run the existing sims.

---

## 1) World Build (“Creation Myth”)

Pipeline steps (in order):
1. Define world geometry and bounds
2. Define prime (anchor) plots with IDs, coordinates, and roles
3. Wire anchor topology (8-way neighbors between anchor plots)
4. Garden the full island band into plots using neighbor influence
5. Attach plot context (base/current description + history) to each plot
6. Assign terrain difficulty and safety to each plot
7. Add terrain features and smooth difficulty across neighboring plots
8. Seed flora (plant families and density per zone)
9. Run deterministic Gardener full-coverage pass + CreationReport

Key invariants:
- All prime plots are reachable from spawn.
- All walkable plots are visited by the Gardener (full coverage).
- No DEAD tiles remain after world build.
- Difficulty smoothing holds (no insane spikes).
- `readyForCosmos == true` gates the world as “playable”.

The CreationReport is emitted after step 9 and reflects these invariants.

---

## 2) Gameplay Loop & Cosmos (80s mode)

GameSession:
- Player starts at Wreck Beach with clock shown as `[24:00]`.
- CosmosClock is 24h (1440 pips) with phases: PRE_DAWN → MORNING → AFTERNOON → DUSK → NIGHT.

Actions (one per turn):
- MOVE_WALK / MOVE_RUN / LOOK / SEARCH / TAKE / DROP / RAFT_WORK / LAUNCH
- JUMP uses ChallengeResolver (DEX/ACROBATICS, DC 12).
- Monkey poo uses ChallengeResolver (DEX save, DC 12) as a reactive event in monkey territory.

Narration:
- DefaultDmAdapter is rule-based and deterministic.
- GameEngine prefixes every message with the clock `[HH:MM]`.
- DM is not LLM-backed.

---

## 3) PlayerTool & ToolEpisode Harness (external agents)

PlayerToolEngine:
- Tools: LOOK, MOVE, SEARCH, TAKE, DROP, RAFT_WORK, STATUS.
- Maps tools → GameEngine actions (or STATUS peek).
- Returns `text` (DM line with `[HH:MM]`) and `state` (PlayerToolState: time/phase, location summary, exits, visible items, inventory, raft progress, last tool/result).

ToolEpisodeRunner:
- Spins up a fresh world + session per episode.
- Bounded by `maxTurns` (default 10 for sandbox).
- Loop:
  - Ask ExternalPlayerAgent for AgentDecision (PlayerToolRequest + one-line reason).
  - Call PlayerToolEngine.invoke(request).
  - Log `[Agent] Reason: …` then the `[HH:MM] …` game line.
- Returns EpisodeSummary + decision log for analysis.

This is the single surface any external agent (heuristic or LLM) uses.

---

## 4) PlayerAgent & Spring AI Boundary (LLM only here)

ExternalPlayerAgent returns AgentDecision (tool + rationale). Implementations:
- SmartAiTestAgent: heuristic, no LLM.
- LlmExternalPlayerAgent: Spring AI ChatClient + PlayerAgent prompts.

Prompts:
- PlayerAgentPromptConfig holds the system prompt (external 2025 player, 10-turn sandbox) and the finalized tool prompts (intent/args/cost per tool).
- Prompts are **not** shared with the DM. DM remains rule-based.

LLM containment:
- Only LlmExternalPlayerAgent talks to ChatClient.
- No Spring AI in GameEngine, DM, world build, Gardener, or tests.
- If parsing fails, the LLM agent falls back to LOOK with a safe reason.

Opt-in:
- `unna.player.llm.enabled=true` activates LlmExternalPlayerAgent (with ChatClient present).
- Otherwise the heuristic SmartAiTestAgent is used.

---

## 5) How to Run

Heuristic AI episode (no LLM):
```
mvn -q exec:java -Dexec.mainClass=com.demo.island.sim.AiTestGameRunner
```

LLM player episode (Spring AI enabled):
```
mvn -q exec:java \
  -Dunna.player.llm.enabled=true \
  -Dexec.mainClass=com.demo.island.sim.AiTestGameRunner
```

Gardener/world build sanity pass (optional):
```
mvn -q exec:java -Dexec.mainClass=com.demo.island.sim.GardenerRunner -Dexec.args="200 42"
```

The logs show world-build steps, the creation report, per-turn agent reasons, and `[HH:MM]` game lines.
