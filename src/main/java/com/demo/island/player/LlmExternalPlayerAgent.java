package com.demo.island.player;

import com.demo.island.game.AgentDecision;
import com.demo.island.game.AgentMood;
import com.demo.island.game.ExternalPlayerAgent;
import com.demo.island.game.GameItemType;
import com.demo.island.game.GameSession;
import com.demo.island.game.PlayerTool;
import com.demo.island.game.PlayerToolRequest;
import com.demo.island.game.PlayerToolResult;
import com.demo.island.world.Direction8;
import com.demo.island.game.memory.MemorySummary;
import com.demo.island.game.memory.MemorySummaryBuilder;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.openai.OpenAiChatOptions;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * LLM PlayerAgent using Spring AI tools. One @Tool per PlayerTool; the model calls a tool and
 * Spring AI returns the payload. No ad-hoc parsing.
 */
public class LlmExternalPlayerAgent implements ExternalPlayerAgent {

    private static final Logger LOG = LogManager.getLogger(LlmExternalPlayerAgent.class);

    private final ChatClient chatClient;
    private final String systemPrompt;
    private final Map<PlayerTool, ToolPrompt> toolPrompts;
    private final DecisionTools decisionTools = new DecisionTools();
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private AgentDecision lastDecisionForPrompt;
    private String lastDecisionToolRaw = "";
    private String lastDecisionTargetRaw = "";
    private int turnCounter = 1;
    private String lastFailureLocationId = "";
    private String lastFailureTool = "";
    private String lastFailureTarget = "";
    private String lastFailureText = "";
    private int lastFailureCount = 0;

    public LlmExternalPlayerAgent(ChatClient chatClient,
                                  String playerAgentSystemPrompt,
                                  Map<PlayerTool, ToolPrompt> playerToolPrompts) {
        this.chatClient = Objects.requireNonNull(chatClient);
        this.systemPrompt = Objects.requireNonNull(playerAgentSystemPrompt);
        this.toolPrompts = Objects.requireNonNull(playerToolPrompts);
    }

    @Override
    public AgentDecision decideNext(GameSession session, PlayerToolResult lastResult) {
        updateRecentFeedback(lastResult);
        String userPrompt = buildUserPrompt(session, lastResult);
        LOG.debug("LLM PlayerAgent prompt:\n{}", userPrompt);
        org.springframework.ai.chat.model.ChatResponse resp = executePrompt(userPrompt);
        AgentDecision decision;
        if (resp == null) {
            decision = fallback("Fallback: LLM call failed or timed out.");
        } else {
            var generation = resp.getResult();
            AssistantMessage output = generation == null ? null : (AssistantMessage) generation.getOutput();
            var toolCalls = output != null ? output.getToolCalls() : null;
            if (toolCalls != null && !toolCalls.isEmpty()) {
                var call = toolCalls.get(0);
                logToolCall(call);
                ToolMappingResult parsed = toDecisionWithError(call);
                if (parsed.decision() != null) {
                    lastDecisionToolRaw = parsed.decision().tool();
                    lastDecisionTargetRaw = parsed.decision().args();
                    MappingOutcome mapped = mapDecision(parsed.decision());
                    if (mapped.decision() != null) {
                        decision = mapped.decision();
                    } else {
                        logMappingFailure(call, firstNonBlank(mapped.error(), "unknown mapping error"));
                        decision = fallback("Fallback: tool payload could not be mapped.");
                    }
                } else {
                    logMappingFailure(call, firstNonBlank(parsed.error(), "tool payload could not be mapped"));
                    decision = fallback("Fallback: tool payload could not be mapped.");
                }
            } else {
                logNoToolCall(snippetFromResponse(resp));
                decision = fallback("Fallback: no tool call returned.");
            }
        }
        decision = applyLoopGuard(decision, lastResult, session, lastDecisionTargetRaw);
        lastDecisionForPrompt = decision;
        if (decision == null) {
            lastDecisionToolRaw = "";
            lastDecisionTargetRaw = "";
        }
        turnCounter++;
        return decision;
    }

    private ToolMappingResult toDecisionWithError(AssistantMessage.ToolCall call) {
        try {
            ToolCallPayload payload = mapper.readValue(call.arguments(), ToolCallPayload.class);
            String toolName = call.name();
            String target = payload.arg0 == null ? "" : payload.arg0;
            String reason = payload.arg1 == null ? "" : payload.arg1;
            String mood = payload.arg2 == null ? "" : payload.arg2;
            String note = payload.arg3 == null ? "" : payload.arg3;
            return new ToolMappingResult(new ToolDecision(toolName, target, reason, mood, note), null);
        } catch (Exception e) {
            return new ToolMappingResult(null, "json parse failed: " + e.getClass().getSimpleName());
        }
    }

    private String buildFullSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append(systemPrompt.trim()).append("\n\nTools:\n");
        for (var entry : toolPrompts.entrySet()) {
            PlayerTool tool = entry.getKey();
            ToolPrompt tp = entry.getValue();
            sb.append("- ").append(tool.name()).append(": ")
                    .append(tp.intent()).append(" ")
                    .append(tp.args()).append(" ")
                    .append(tp.costHint()).append("\n");
        }
        sb.append("\nInvoke exactly one of the tools below.\n");
        return sb.toString();
    }

    private String buildUserPrompt(GameSession session, PlayerToolResult lastResult) {
        String time = lastResult != null && lastResult.getState() != null && lastResult.getState().time != null
                ? lastResult.getState().time
                : session.getClock().formatRemainingBracketed();
        String phase = lastResult != null && lastResult.getState() != null && lastResult.getState().phase != null
                ? lastResult.getState().phase
                : session.getClock().getPhase().name();

        String locationId = lastResult != null && lastResult.getState() != null ? lastResult.getState().locationId : "Unknown";
        String locationSummary = lastResult != null && lastResult.getState() != null ? lastResult.getState().locationSummary : "Unknown location.";

        String exits = "None";
        if (lastResult != null && lastResult.getState() != null && lastResult.getState().visibleExits != null && !lastResult.getState().visibleExits.isEmpty()) {
            exits = String.join(", ", lastResult.getState().visibleExits.keySet());
        }

        String visibleItems = "None";
        if (lastResult != null && lastResult.getState() != null && lastResult.getState().visibleItems != null && !lastResult.getState().visibleItems.isEmpty()) {
            visibleItems = String.join(", ", lastResult.getState().visibleItems);
        }

        String inventory = "None";
        if (lastResult != null && lastResult.getState() != null && lastResult.getState().inventory != null && !lastResult.getState().inventory.isEmpty()) {
            inventory = String.join("; ", lastResult.getState().inventory);
        }

        String lastToolName = lastDecisionForPrompt != null ? lastDecisionForPrompt.getRequest().getTool().name() : "None";
        String lastToolTarget = lastDecisionForPrompt != null ? targetFor(lastDecisionForPrompt) : "";
        String lastResultText = lastResult != null && lastResult.getText() != null ? lastResult.getText() : "";
        String feedback = recentFeedbackSummary();

        StringBuilder sb = new StringBuilder();
        sb.append("GAME STATE\n\n");
        sb.append("Turn: ").append(turnCounter).append("\n");
        sb.append("Time: ").append(time).append(" (Phase: ").append(phase).append(")\n\n");
        sb.append("Location:\n");
        sb.append("  Name: ").append(locationId).append("\n");
        sb.append("  Description: ").append(locationSummary).append("\n\n");
        sb.append("Exits:\n");
        sb.append("  ").append(exits).append("\n\n");
        sb.append("Visible items:\n");
        sb.append("  ").append(visibleItems).append("\n\n");
        sb.append("Inventory:\n");
        sb.append("  ").append(inventory).append("\n\n");
        sb.append("Last action:\n");
        sb.append("  Tool: ").append(lastToolName).append("\n");
        sb.append("  Target: ").append(lastToolTarget).append("\n");
        sb.append("  Result: ").append(lastResultText).append("\n\n");
        sb.append("Recent feedback at this location:\n");
        sb.append("  ").append(feedback).append("\n\n");
        sb.append("Notes:\n");
        sb.append("  - Visible items list above is complete. If it says \"None\", there are no items here.\n");
        sb.append("  - Inventory list above is complete. Only listed items are currently carried.\n");
        sb.append("\nMemory:\n");
        sb.append(formatMemorySection(session));
        return sb.toString();
    }

    private MappingOutcome mapDecision(ToolDecision td) {
        if (td == null || td.tool() == null) return new MappingOutcome(null, "tool missing");
        String toolName = td.tool().trim().toUpperCase(Locale.ROOT);
        String args = td.args() == null ? "" : td.args();
        String reason = td.reason() == null || td.reason().isBlank()
                ? "Fallback: missing reason."
                : td.reason();
        AgentMood mood = parseMood(td.mood());
        String note = td.note() == null ? "" : td.note();
        try {
            PlayerTool tool = PlayerTool.valueOf(toolName);
            return switch (tool) {
                case LOOK -> new MappingOutcome(new AgentDecision(PlayerToolRequest.look(), reason, mood, note), null);
                case SEARCH -> new MappingOutcome(new AgentDecision(PlayerToolRequest.search(), reason, mood, note), null);
                case RAFT_WORK -> new MappingOutcome(new AgentDecision(PlayerToolRequest.raftWork(), reason, mood, note), null);
                case STATUS -> new MappingOutcome(new AgentDecision(PlayerToolRequest.status(), reason, mood, note), null);
                case MOVE -> {
                    if (args.isBlank()) {
                        yield new MappingOutcome(null, "missing direction");
                    }
                    Direction8 dir = parseDirection(args);
                    yield dir != null
                            ? new MappingOutcome(new AgentDecision(PlayerToolRequest.move(dir), reason, mood, note), null)
                            : new MappingOutcome(null, "invalid direction");
                }
                case TAKE -> {
                    if (args.isBlank()) {
                        yield new MappingOutcome(null, "missing item");
                    }
                    GameItemType item = parseItem(args);
                    yield item != null
                            ? new MappingOutcome(new AgentDecision(PlayerToolRequest.take(item), reason, mood, note), null)
                            : new MappingOutcome(null, "invalid item");
                }
                case DROP -> {
                    if (args.isBlank()) {
                        yield new MappingOutcome(null, "missing item");
                    }
                    GameItemType item = parseItem(args);
                    yield item != null
                            ? new MappingOutcome(new AgentDecision(PlayerToolRequest.drop(item), reason, mood, note), null)
                            : new MappingOutcome(null, "invalid item");
                }
            };
        } catch (IllegalArgumentException ex) {
            return new MappingOutcome(null, "unknown tool");
        }
    }

    private Direction8 parseDirection(String token) {
        if (token == null || token.isEmpty()) return null;
        try {
            return Direction8.valueOf(token.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private GameItemType parseItem(String token) {
        if (token == null || token.isEmpty()) return null;
        try {
            String norm = token.trim().toUpperCase(Locale.ROOT);
            norm = norm.replace('-', ' ');
            if (norm.contains("HATCHET")) {
                return GameItemType.HATCHET;
            }
            if (norm.contains("WOOD") && norm.contains("LOG")) {
                return GameItemType.WOOD_LOG;
            }
            if (norm.contains("VINE") && norm.contains("ROPE")) {
                return GameItemType.VINE_ROPE;
            }
            if (norm.contains("METAL") && norm.contains("SCRAP")) {
                return GameItemType.METAL_SCRAP;
            }
            String enumish = norm.replaceAll("\\s+", "_");
            return GameItemType.valueOf(enumish);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private AgentMood parseMood(String raw) {
        if (raw == null || raw.isEmpty()) return AgentMood.CURIOUS;
        try {
            return AgentMood.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return AgentMood.CURIOUS;
        }
    }

    private AgentDecision fallback(String reason) {
        AgentDecision decision = new AgentDecision(PlayerToolRequest.look(), reason, AgentMood.CURIOUS, "");
        lastDecisionForPrompt = decision;
        lastDecisionToolRaw = PlayerTool.LOOK.name();
        lastDecisionTargetRaw = "";
        return decision;
    }

    private org.springframework.ai.chat.model.ChatResponse executePrompt(String userPrompt) {
        String system = buildFullSystemPrompt();
        try {
            return CompletableFuture.supplyAsync(() -> chatClient.prompt()
                            .system(system)
                            .tools(decisionTools)
                            .options(OpenAiChatOptions.builder()
                                    .toolChoice("required") // force the model to pick a tool
                                    .internalToolExecutionEnabled(false) // let the agent consume tool calls manually
                                    .build())
                            .user(userPrompt)
                            .call()
                            .chatResponse())
                    .get(30, TimeUnit.SECONDS);
        } catch (TimeoutException te) {
            LOG.warn("LLM PlayerAgent: chat call timed out; using fallback.");
        } catch (Exception ex) {
            LOG.warn("LLM PlayerAgent: chat call failed; using fallback. err={}", ex.getMessage());
        }
        return null;
    }

    private void logNoToolCall(String snippet) {
        String msg = "LLM PlayerAgent: no tool call in response; using fallback LOOK.";
        if (snippet != null && !snippet.isBlank()) {
            msg += " contentSnippet=\"" + snippet + "\"";
        }
        LOG.info(msg);
    }

    private String snippetFromResponse(org.springframework.ai.chat.model.ChatResponse resp) {
        if (resp == null) {
            return "";
        }
        try {
            var generation = resp.getResult();
            AssistantMessage output = generation == null ? null : (AssistantMessage) generation.getOutput();
            if (output == null || output.getText() == null) {
                return "";
            }
            String snippet = output.getText();
            snippet = snippet.replaceAll("\\s+", " ");
            if (snippet.length() > 120) {
                snippet = snippet.substring(0, 120) + "...";
            }
            return snippet;
        } catch (Exception ex) {
            return "";
        }
    }

    private void logToolCall(AssistantMessage.ToolCall call) {
        LOG.debug("LLM PlayerAgent tool call: name={} args={}", call.name(), truncateArgs(call.arguments()));
    }

    private void logMappingFailure(AssistantMessage.ToolCall call, String error) {
        LOG.debug("LLM PlayerAgent mapping failure: name={} args={}{}",
                call.name(),
                truncateArgs(call.arguments()),
                (error == null || error.isBlank() ? "" : " error=" + error));
    }

    private void updateRecentFeedback(PlayerToolResult lastResult) {
        if (lastResult == null || lastResult.getState() == null) {
            resetFailure();
            return;
        }
        if (lastDecisionForPrompt == null || lastDecisionForPrompt.getRequest() == null) {
            resetFailure();
            return;
        }
        String lastToolResult = lastResult.getState().lastToolResult == null ? "" : lastResult.getState().lastToolResult;
        String lastText = lastResult.getText() == null ? "" : lastResult.getText();
        boolean isFailure = "blocked".equalsIgnoreCase(lastToolResult)
                || lastText.toLowerCase(Locale.ROOT).contains("haven't found any items here");
        if (!isFailure) {
            String loc = lastResult.getState().locationId == null ? "" : lastResult.getState().locationId;
            if (!loc.equals(lastFailureLocationId)) {
                resetFailure();
            }
            return; // keep any prior loop info until a new failure or reset condition.
        }
        String loc = lastResult.getState().locationId == null ? "" : lastResult.getState().locationId;
        String tool = lastDecisionToolRaw == null || lastDecisionToolRaw.isBlank()
                ? lastDecisionForPrompt.getRequest().getTool().name()
                : lastDecisionToolRaw;
        String target = targetFor(lastDecisionForPrompt);
        String text = normalizeFeedbackText(lastResult.getText());
        if (loc.equals(lastFailureLocationId) && tool.equals(lastFailureTool)
                && target.equals(lastFailureTarget) && text.equals(lastFailureText)) {
            lastFailureCount += 1;
        } else {
            lastFailureLocationId = loc;
            lastFailureTool = tool;
            lastFailureTarget = target;
            lastFailureText = text;
            lastFailureCount = 1;
        }
    }

    private void resetFailure() {
        lastFailureLocationId = "";
        lastFailureTool = "";
        lastFailureTarget = "";
        lastFailureText = "";
        lastFailureCount = 0;
    }

    private String recentFeedbackSummary() {
        if (lastFailureCount >= 2 && !lastFailureText.isBlank() && !lastFailureLocationId.isBlank()
                && !lastFailureTool.isBlank()) {
            String target = lastFailureTarget == null ? "" : lastFailureTarget;
            String targetPhrase = target.isBlank() ? "" : " \"" + target + "\"";
            return "You have attempted " + lastFailureTool + targetPhrase + " at this location "
                    + lastFailureCount + " times. Each attempt returned: \"" + lastFailureText + "\".";
        }
        return "None";
    }

    private String targetFor(AgentDecision decision) {
        if (decision == null || decision.getRequest() == null) return "";
        PlayerToolRequest req = decision.getRequest();
        if (lastDecisionToolRaw != null && !lastDecisionToolRaw.isBlank()
                && lastDecisionTargetRaw != null) {
            // Use raw target when available to preserve original item/direction strings.
            return lastDecisionTargetRaw;
        }
        return switch (req.getTool()) {
            case MOVE -> req.getDirection() != null ? req.getDirection().name() : "";
            case TAKE, DROP -> req.getItemType() != null ? req.getItemType().name() : "";
            default -> "";
        };
    }

    private String normalizeFeedbackText(String text) {
        if (text == null) return "";
        String normalized = text.trim();
        // Strip leading time prefix like "[23:59] "
        if (normalized.startsWith("[")) {
            int idx = normalized.indexOf("]");
            if (idx >= 0 && idx + 1 < normalized.length()) {
                normalized = normalized.substring(idx + 1).trim();
            }
        }
        return normalized;
    }

    private AgentDecision applyLoopGuard(AgentDecision decision,
                                         PlayerToolResult lastResult,
                                         GameSession session,
                                         String currentRawTarget) {
        if (decision == null || decision.getRequest() == null) return decision;
        if (decision.getRequest().getTool() != PlayerTool.TAKE) return decision;
        if (lastResult == null || lastResult.getState() == null) return decision;

        boolean noVisibleItems = lastResult.getState().visibleItems == null
                || lastResult.getState().visibleItems.isEmpty();
        if (!noVisibleItems) return decision;

        String loc = session.getLocation() != null ? session.getLocation().getTileId() : "";
        if (loc == null || loc.isBlank()) {
            loc = lastResult.getState().locationId == null ? "" : lastResult.getState().locationId;
        }
        if (loc.isBlank()) return decision;

        boolean sameLocation = loc.equals(lastFailureLocationId);
        boolean sameTarget = currentRawTarget != null && !currentRawTarget.isBlank()
                && currentRawTarget.equalsIgnoreCase(lastFailureTarget);
        boolean lastWasTake = "TAKE".equalsIgnoreCase(lastFailureTool);
        boolean failureKnown = lastFailureCount >= 1
                && lastFailureText != null && !lastFailureText.isBlank()
                && (lastFailureText.toLowerCase(Locale.ROOT).contains("haven't found any items here")
                || "blocked".equalsIgnoreCase(lastResult.getState().lastToolResult));

        if (sameLocation && sameTarget && lastWasTake && failureKnown) {
            String guardMsg = "LoopGuard: suppressed TAKE \"" + currentRawTarget + "\" at location="
                    + loc + " (visibleItems=None, lastResult=\"" + lastFailureText + "\"); auto-switching to LOOK.";
            LOG.info(guardMsg);
            // Ensure last-action reporting reflects the executed tool.
            lastDecisionToolRaw = PlayerTool.LOOK.name();
            lastDecisionTargetRaw = "";
            AgentMood mood = AgentMood.CONFUSED;
            String reason = "LoopGuard: TAKE blocked here; looking around instead.";
            return new AgentDecision(PlayerToolRequest.look(), reason, mood, "");
        }
        return decision;
    }

    private String truncateArgs(String args) {
        if (args == null) return "";
        String compact = args.replaceAll("\\s+", " ");
        int limit = 200;
        if (compact.length() > limit) {
            return compact.substring(0, limit) + "...";
        }
        return compact;
    }

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        return b;
    }

    public String explainMemory(GameSession session, String question) {
        MemorySummary summary = MemorySummaryBuilder.build(session);
        String userQuestion = (question == null || question.isBlank())
                ? "Explain, in detail, how your memory works for you and the plots of land on this island."
                : question;
        String userPrompt = userQuestion.trim() + "\n\nMEMORY SUMMARY\n" + summary.render();
        String system = "You are the player describing how your own memory works. "
                + "Answer in plain language using the memory summary provided. Do not call tools.";
        try {
            org.springframework.ai.chat.model.ChatResponse response = chatClient.prompt()
                    .system(system)
                    .user(userPrompt)
                    .options(OpenAiChatOptions.builder().toolChoice("none").build())
                    .call()
                    .chatResponse();
            return extractText(response, summary.render());
        } catch (Exception ex) {
            LOG.warn("LLM PlayerAgent: memory explanation fallback used. err={}", ex.getMessage());
            return "Memory summary (fallback):\n" + summary.render();
        }
    }

    private String extractText(org.springframework.ai.chat.model.ChatResponse resp, String fallback) {
        if (resp == null) {
            return fallback;
        }
        try {
            var generation = resp.getResult();
            AssistantMessage output = generation == null ? null : (AssistantMessage) generation.getOutput();
            if (output != null && output.getText() != null && !output.getText().isBlank()) {
                return output.getText().trim();
            }
        } catch (Exception ex) {
            // ignore and fall back
        }
        return fallback;
    }

    private String formatMemorySection(GameSession session) {
        MemorySummary summary = MemorySummaryBuilder.build(session);
        String rendered = summary.render();
        String indented = "  " + rendered.replace("\n", "\n  ");
        return indented + "\n";
    }

    private record ToolMappingResult(ToolDecision decision, String error) { }

    private record MappingOutcome(AgentDecision decision, String error) { }

    /**
     * One @Tool per PlayerTool; Spring AI uses the signatures to expose the contract to the model.
     */
    public static class DecisionTools {
        @Tool(name = "LOOK", description = "Inspect the current location. Positional args: arg0 is empty, arg1=reason, arg2=mood, arg3=note.")
        public ToolDecision look(String reason, String mood, String note) { return null; }

        @Tool(name = "MOVE", description = "Walk to an adjacent location. First arg: direction (N,NE,E,SE,S,SW,W,NW). Next args: reason, mood, note.")
        public ToolDecision move(String direction, String reason, String mood, String note) { return null; }

        @Tool(name = "SEARCH", description = "Carefully inspect the current location. First meaningful arg is reason, then mood, then note.")
        public ToolDecision search(String reason, String mood, String note) { return null; }

        @Tool(name = "TAKE", description = "Pick up a visible item. First arg: item name exactly as shown. Next args: reason, mood, note.")
        public ToolDecision take(String item, String reason, String mood, String note) { return null; }

        @Tool(name = "DROP", description = "Drop an item from inventory. First arg: item name exactly as shown. Next args: reason, mood, note.")
        public ToolDecision drop(String item, String reason, String mood, String note) { return null; }

        @Tool(name = "RAFT_WORK", description = "Attempt construction/escape work here. First meaningful arg is reason, then mood, then note.")
        public ToolDecision raftWork(String reason, String mood, String note) { return null; }

        @Tool(name = "STATUS", description = "Get a concise summary without changing the world. First meaningful arg is reason, then mood, then note.")
        public ToolDecision status(String reason, String mood, String note) { return null; }
    }

    private static final class ToolCallPayload {
        public String arg0;
        public String arg1;
        public String arg2;
        public String arg3;
    }
}
