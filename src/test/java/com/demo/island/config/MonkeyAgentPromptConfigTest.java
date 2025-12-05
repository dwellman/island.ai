package com.demo.island.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MonkeyAgentPromptConfigTest {

    @AfterEach
    void clear() {
        System.clearProperty("monkey1.prompt.path");
    }

    @Test
    void loadsMonkeyPrompt() {
        System.setProperty("monkey1.prompt.path", "src/test/resources/prompts/test-monkey1.md");
        MonkeyAgentPromptConfig cfg = new MonkeyAgentPromptConfig();

        String prompt = cfg.monkeyAgentSystemPrompt();

        assertThat(prompt).contains("TEST MONKEY1 PROMPT");
    }

    @Test
    void missingMonkeyPromptReturnsBlank() {
        System.setProperty("monkey1.prompt.path", "src/test/resources/prompts/missing-monkey1.md");
        MonkeyAgentPromptConfig cfg = new MonkeyAgentPromptConfig();

        String prompt = cfg.monkeyAgentSystemPrompt();

        assertThat(prompt).contains("Monkey1 – Island Monkey Agent System Prompt");
    }
}
