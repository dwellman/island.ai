package com.demo.island.player;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PlayerAgentPromptConfigTest {

    @AfterEach
    void clear() {
        System.clearProperty("player1.prompt.path");
    }

    @Test
    void loadsPromptFromFile() {
        System.setProperty("player1.prompt.path", "src/test/resources/prompts/test-player1.md");
        PlayerAgentPromptConfig cfg = new PlayerAgentPromptConfig();

        String prompt = cfg.playerAgentSystemPrompt();

        assertThat(prompt).contains("TEST PLAYER1 PROMPT");
    }

    @Test
    void fallsBackWhenMissing() {
        System.setProperty("player1.prompt.path", "src/test/resources/prompts/missing-player1.md");
        PlayerAgentPromptConfig cfg = new PlayerAgentPromptConfig();

        String prompt = cfg.playerAgentSystemPrompt();

        assertThat(prompt).contains("External PlayerAgent using Spring AI tool-calling").isNotBlank();
    }
}
