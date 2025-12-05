package com.demo.island.game.monkey;

import com.demo.island.game.AgentMood;
import com.demo.island.game.PlayerTool;
import com.demo.island.game.PlayerToolRequest;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.openai.OpenAiChatOptions;

/**
 * MonkeyAgent backed by Spring AI tool-calling (LOOK, MOVE, SEARCH).
 */
public final class SpringAiMonkeyAgent implements MonkeyAgent {
    private static final Logger LOG = LogManager.getLogger(SpringAiMonkeyAgent.class);

    private final ChatClient chatClient;
    private final String systemPrompt;
    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public SpringAiMonkeyAgent(ChatClient chatClient, String systemPrompt) {
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt;
    }

    @Override
    public MonkeyDecision decide(MonkeyState state) {
        if (systemPrompt == null || systemPrompt.isBlank()) {
            return null;
        }
        try {
            String user = formatState(state);
            ChatResponse response = chatClient.prompt()
                    .system(systemPrompt)
                    .tools(MonkeyToolsDescriptor.class)
                    .options(OpenAiChatOptions.builder()
                            .toolChoice("required")
                            .internalToolExecutionEnabled(false)
                            .build())
                    .user(user)
                    .call()
                    .chatResponse();
            if (response == null || response.getResult() == null) {
                return null;
            }
            AssistantMessage.ToolCall call = response.getResult().getOutput().getToolCalls().stream().findFirst().orElse(null);
            if (call == null) {
                return null;
            }
            MonkeyToolPayload payload = new MonkeyToolPayload();
            try {
                if (call.arguments() != null && !call.arguments().isBlank()) {
                    payload = mapper.readValue(call.arguments(), MonkeyToolPayload.class);
                }
            } catch (Exception ex) {
                LOG.debug("MonkeyAgent: failed to parse tool args: {}", ex.getMessage());
            }
            String name = call.name();
            return mapDecision(name, payload);
        } catch (Exception ex) {
            LOG.debug("MonkeyAgent: LLM call failed; skipping turn. err={}", ex.getMessage());
            return null;
        }
    }

    private MonkeyDecision mapDecision(String name, MonkeyToolPayload payload) {
        if (name == null) return null;
        String arg0 = payload.arg0 == null ? "" : payload.arg0;
        String arg1 = payload.arg1 == null ? "" : payload.arg1;
        String arg2 = payload.arg2 == null ? "" : payload.arg2;
        String arg3 = payload.arg3 == null ? "" : payload.arg3;
        PlayerToolRequest req;
        switch (name.toUpperCase()) {
            case "MOVE" -> {
                try {
                    req = PlayerToolRequest.move(com.demo.island.world.Direction8.valueOf(arg0.toUpperCase()));
                } catch (Exception ex) {
                    req = PlayerToolRequest.look();
                }
            }
            case "SEARCH" -> req = PlayerToolRequest.search();
            case "LOOK" -> req = PlayerToolRequest.look();
            default -> req = PlayerToolRequest.look();
        }
        AgentMood mood;
        try {
            mood = AgentMood.valueOf(arg2.toUpperCase());
        } catch (Exception ex) {
            mood = AgentMood.CURIOUS;
        }
        return new MonkeyDecision(req, arg0, arg1, mood);
    }

    private String formatState(MonkeyState state) {
        StringBuilder sb = new StringBuilder();
        sb.append("MONKEY_STATE\n\n");
        sb.append("Turn: ").append(state.turnNumber()).append("\n");
        sb.append("Actor: ").append(state.actorId()).append("\n");
        sb.append("Location: ").append(state.plotId()).append("\n");
        sb.append("Description: ").append(state.description()).append("\n");
        sb.append("Exits: ").append(state.exits().keySet()).append("\n");
        sb.append("PlayerHere: ").append(state.playerHere()).append("\n");
        sb.append("Notes: Use tools LOOK, MOVE, SEARCH only. arg0=direction for MOVE, empty otherwise. arg1=reason, arg2=mood, arg3=note.\n");
        return sb.toString();
    }

    public static class MonkeyToolsDescriptor {
        @org.springframework.ai.tool.annotation.Tool(name = "LOOK", description = "Observe current location. args: reason, mood, note.")
        public void look(String reason, String mood, String note) {}

        @org.springframework.ai.tool.annotation.Tool(name = "MOVE", description = "Move to adjacent tile. arg0=direction (N,NE,E,SE,S,SW,W,NW), arg1=reason, arg2=mood, arg3=note.")
        public void move(String direction, String reason, String mood, String note) {}

        @org.springframework.ai.tool.annotation.Tool(name = "SEARCH", description = "Search current location. args: reason, mood, note.")
        public void search(String reason, String mood, String note) {}
    }

    public static final class MonkeyToolPayload {
        public String arg0;
        public String arg1;
        public String arg2;
        public String arg3;
    }
}
