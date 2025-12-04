package com.demo.island.ai;

import com.demo.island.player.PlayerAgent;
import com.demo.island.player.PlayerInput;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;

import java.util.logging.Logger;

/**
 * Player agent backed by Spring AI. Returns a single command string.
 */
public final class SpringAiPlayerAgent implements PlayerAgent {

    private static final Logger LOG = Logger.getLogger(SpringAiPlayerAgent.class.getName());

    private final ChatClient chatClient;
    private final String systemPrompt;
    private final ObjectMapper mapper;

    public SpringAiPlayerAgent(ChatClient chatClient) {
        this(chatClient, PromptLoader.load("player.prompt.md"));
    }

    public SpringAiPlayerAgent(ChatClient chatClient, String systemPrompt) {
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt == null ? "" : systemPrompt;
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public String decide(PlayerInput input) {
        String jsonInput;
        try {
            jsonInput = mapper.writeValueAsString(input.toDto());
        } catch (Exception e) {
            LOG.warning("Failed to serialize Player input: " + e.getMessage());
            return "LOOK";
        }

        String response;
        try {
            response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(jsonInput)
                    .call()
                    .content();
        } catch (Exception e) {
            LOG.warning("Player agent call failed: " + e.getMessage());
            return "LOOK";
        }

        // Expect plain command text; trim and default to LOOK on empty.
        String command = response == null ? "" : response.trim();
        if (command.isEmpty()) {
            return "LOOK";
        }
        // If model responds with quotes or JSON, strip simple quotes/brackets heuristically.
        if (command.startsWith("\"") && command.endsWith("\"") && command.length() > 1) {
            command = command.substring(1, command.length() - 1).trim();
        }
        return command.isEmpty() ? "LOOK" : command;
    }
}
