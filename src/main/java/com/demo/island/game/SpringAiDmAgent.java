package com.demo.island.game;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;

/**
 * DM Agent backed by Spring AI ChatClient using the dm1.md prompt.
 */
public final class SpringAiDmAgent implements DmAgent {

    private final ChatClient chatClient;
    private final String systemPrompt;

    public SpringAiDmAgent(ChatClient chatClient, String systemPrompt) {
        this.chatClient = chatClient;
        this.systemPrompt = systemPrompt;
    }

    @Override
    public String rewrite(DmAgentContext context) {
        if (systemPrompt == null || systemPrompt.isBlank()) {
            return null;
        }
        String user = DmAgentContextFormatter.format(context);
        ChatResponse response = chatClient.prompt()
                .system(systemPrompt)
                .user(user)
                .call()
                .chatResponse();
        if (response == null || response.getResult() == null || response.getResult().getOutput() == null) {
            return null;
        }
        var output = response.getResult().getOutput();
        String text = output.getText();
        if (text == null) {
            return null;
        }
        String firstLine = text.strip();
        int idx = firstLine.indexOf('\n');
        if (idx >= 0) {
            firstLine = firstLine.substring(0, idx).trim();
        }
        return firstLine.isBlank() ? null : firstLine;
    }
}
