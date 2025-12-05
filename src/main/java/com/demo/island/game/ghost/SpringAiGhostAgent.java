package com.demo.island.game.ghost;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatOptions;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * GhostAgent backed by Spring AI that returns a manifestation mode/text based on Ghost1 prompt.
 */
public final class SpringAiGhostAgent implements GhostAgent {
    private static final Logger LOG = LogManager.getLogger(SpringAiGhostAgent.class);
    private final ChatClient chatClient;
    private final String systemPrompt;
    private final ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public SpringAiGhostAgent(ChatClient chatClient, String systemPrompt) {
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt;
    }

    @Override
    public GhostManifestation manifest(GhostState state) {
        if (systemPrompt == null || systemPrompt.isBlank() || state == null) {
            return GhostManifestation.silent();
        }
        String user = state.format();
        String content = "";
        try {
            ChatResponse response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(user)
                    .options(OpenAiChatOptions.builder().internalToolExecutionEnabled(false).build())
                    .call()
                    .chatResponse();
            if (response != null && response.getResult() != null) {
                var output = response.getResult().getOutput();
                if (output instanceof org.springframework.ai.chat.messages.AssistantMessage) {
                    var msg = (org.springframework.ai.chat.messages.AssistantMessage) output;
                    if (msg.getText() != null) {
                        content = msg.getText();
                    }
                }
            }
        } catch (Exception ex) {
            LOG.info("GhostAgent: call failed; silent. err={}", ex.getMessage());
            return GhostManifestation.silent();
        }
        if (content == null || content.isBlank()) {
            return GhostManifestation.silent();
        }
        try {
            ManifestationPayload payload = mapper.readValue(content, ManifestationPayload.class);
            GhostMode mode = parseMode(payload.mode);
            String text = payload.text == null ? "" : payload.text;
            if (mode == null) {
                return GhostManifestation.silent();
            }
            return new GhostManifestation(mode, text);
        } catch (Exception ex) {
            LOG.info("GhostAgent: parse failed; silent. err={}", ex.getMessage());
            return GhostManifestation.silent();
        }
    }

    private GhostMode parseMode(String mode) {
        if (mode == null || mode.isBlank()) return null;
        try {
            return GhostMode.valueOf(mode.trim().toUpperCase());
        } catch (Exception ex) {
            return null;
        }
    }

    private static final class ManifestationPayload {
        public String mode;
        public String text;
    }
}
