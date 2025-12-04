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
import org.json.JSONObject;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.annotation.Tool;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class LlmExternalPlayerAgent implements ExternalPlayerAgent {

    private final ChatClient chatClient;
    private final String systemPrompt;
    private final Map<PlayerTool, ToolPrompt> toolPrompts;
    private final boolean jsonContract;

    public LlmExternalPlayerAgent(ChatClient chatClient,
                                  String playerAgentSystemPrompt,
                                  Map<PlayerTool, ToolPrompt> playerToolPrompts,
                                  boolean jsonContract) {
        this.chatClient = chatClient;
        this.systemPrompt = Objects.requireNonNull(playerAgentSystemPrompt);
        this.toolPrompts = Objects.requireNonNull(playerToolPrompts);
        this.jsonContract = jsonContract;
    }

    @Override
    public AgentDecision decideNext(GameSession session, PlayerToolResult lastResult) {
        String userPrompt = buildUserPrompt(session, lastResult);
        String response = chatClient.prompt()
                .system(buildFullSystemPrompt())
                .tools(new DecisionToolSchema())
                .user(userPrompt)
                .call()
                .content();
        return parseResponse(response);
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
        sb.append("\nRespond with labeled lines (text/markdown is fine, no JSON needed):\n");
        sb.append("TOOL: <NAME> [ARGS]\n");
        sb.append("REASON: <one sentence reason>\n");
        sb.append("MOOD: <PROGRESSING|CURIOUS|CAUTIOUS|CONFUSED|FRUSTRATED|STUCK>\n");
        sb.append("NOTE: <optional short note to designers>\n");
        return sb.toString();
    }

    private String buildUserPrompt(GameSession session, PlayerToolResult lastResult) {
        StringBuilder sb = new StringBuilder();
        if (lastResult != null) {
            sb.append("Last message: ").append(lastResult.getText()).append("\n");
            if (lastResult.getState() != null) {
                sb.append("State:\n");
                sb.append("- time: ").append(lastResult.getState().time).append(" phase: ").append(lastResult.getState().phase).append("\n");
                sb.append("- location: ").append(lastResult.getState().locationId).append(" => ").append(lastResult.getState().locationSummary).append("\n");
                sb.append("- exits: ").append(lastResult.getState().visibleExits).append("\n");
                sb.append("- items: ").append(lastResult.getState().visibleItems).append("\n");
                sb.append("- inventory: ").append(lastResult.getState().inventory).append("\n");
                sb.append("- progress: ").append(lastResult.getState().raftProgress).append(" ready=").append(lastResult.getState().raftReady).append("\n");
                sb.append("- lastTool: ").append(lastResult.getState().lastTool).append(" result=").append(lastResult.getState().lastToolResult).append("\n");
            }
        } else {
            sb.append("Start of episode. No prior actions.\n");
        }
        sb.append("Choose the next tool and give one concise reason.");
        return sb.toString();
    }

    private AgentDecision parseResponse(String response) {
        if (response == null) {
            return fallbackDecision("Fallback: null response");
        }
        if (jsonContract) {
            Optional<AgentDecision> parsed = parseJsonResponse(response);
            if (parsed.isPresent()) {
                return parsed.get();
            }
        }
        String[] lines = response.trim().split("\\r?\\n");
        String toolLine = "";
        String reasonLine = "";
        String moodLine = "";
        String noteLine = "";
        for (String line : lines) {
            String upper = line.toUpperCase(Locale.ROOT);
            if (upper.startsWith("TOOL:")) {
                toolLine = line.substring(line.indexOf(':') + 1).trim();
            } else if (upper.startsWith("REASON:")) {
                reasonLine = line.substring(line.indexOf(':') + 1).trim();
            } else if (upper.startsWith("MOOD:")) {
                moodLine = line.substring(line.indexOf(':') + 1).trim();
            } else if (upper.startsWith("NOTE:")) {
                noteLine = line.substring(line.indexOf(':') + 1).trim();
            }
        }
        PlayerToolRequest request = parseTool(toolLine, extractArgs(toolLine)).orElse(PlayerToolRequest.look());
        String reason = reasonLine.isEmpty() ? "Fallback: unable to interpret response; looking around." : reasonLine;
        AgentMood mood = parseMood(moodLine);
        return new AgentDecision(request, reason, mood, noteLine);
    }

    private Optional<PlayerToolRequest> parseTool(String toolText) {
        if (toolText == null || toolText.isEmpty()) return Optional.empty();
        String upper = toolText.toUpperCase(Locale.ROOT);
        String[] parts = upper.split("\\s+");
        if (parts.length == 0) return Optional.empty();
        String name = parts[0];
        try {
            PlayerTool tool = PlayerTool.valueOf(name);
            return switch (tool) {
                case LOOK -> Optional.of(PlayerToolRequest.look());
                case SEARCH -> Optional.of(PlayerToolRequest.search());
                case RAFT_WORK -> Optional.of(PlayerToolRequest.raftWork());
                case STATUS -> Optional.of(PlayerToolRequest.status());
                case MOVE -> {
                    Direction8 dir = parts.length > 1 ? parseDirection(parts[1]) : null;
                    yield dir != null ? Optional.of(PlayerToolRequest.move(dir)) : Optional.empty();
                }
                case TAKE, DROP -> {
                    GameItemType item = parts.length > 1 ? parseItem(parts[1]) : null;
                    if (item == null) yield Optional.empty();
                    yield tool == PlayerTool.TAKE
                            ? Optional.of(PlayerToolRequest.take(item))
                            : Optional.of(PlayerToolRequest.drop(item));
                }
            };
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }

    private String extractArgs(String toolLine) {
        if (toolLine == null) return null;
        String[] parts = toolLine.trim().split("\\s+", 2);
        if (parts.length < 2) return "";
        return parts[1].trim();
    }

    private Direction8 parseDirection(String token) {
        try {
            return Direction8.valueOf(token);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private GameItemType parseItem(String token) {
        try {
            return GameItemType.valueOf(token);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private AgentMood parseMood(String raw) {
        if (raw == null || raw.isEmpty()) return AgentMood.CURIOUS;
        try {
            return AgentMood.valueOf(raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return AgentMood.CURIOUS;
        }
    }

    private AgentDecision fallbackDecision(String reason) {
        return new AgentDecision(PlayerToolRequest.look(), reason, AgentMood.CURIOUS, "");
    }

    private Optional<AgentDecision> parseJsonResponse(String response) {
        try {
            int start = response.indexOf('{');
            int end = response.lastIndexOf('}');
            String jsonText = (start >= 0 && end > start) ? response.substring(start, end + 1) : response;
            JSONObject json = new JSONObject(jsonText);
            String tool = json.optString("tool", "");
            String args = json.optString("args", "");
            String reason = json.optString("reason", "").trim();
            String moodRaw = json.optString("mood", "CURIOUS").trim();
            String note = json.optString("note", "").trim();
            PlayerToolRequest request = parseTool(tool, args).orElse(PlayerToolRequest.look());
            AgentMood mood = parseMood(moodRaw);
            if (reason.isEmpty()) {
                reason = "Fallback: missing reason in JSON.";
            }
            return Optional.of(new AgentDecision(request, reason, mood, note));
        } catch (Exception ex) {
            return Optional.empty();
        }
    }

    /**
     * Tool schema for Spring AI tool registration (documentation only).
     */
    public static class DecisionToolSchema {
        @Tool(description = "Choose the next PlayerTool. Preferred reply uses labeled lines: TOOL:, REASON:, MOOD:, NOTE:.")
        public String decide(String tool, String args, String reason, String mood, String note) {
            return "";
        }
    }
}
