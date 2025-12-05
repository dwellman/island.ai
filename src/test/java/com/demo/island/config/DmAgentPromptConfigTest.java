package com.demo.island.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DmAgentPromptConfigTest {

    @AfterEach
    void clear() {
        System.clearProperty("dm1.prompt.path");
    }

    @Test
    void loadsDmPromptFromFile() {
        System.setProperty("dm1.prompt.path", "src/test/resources/prompts/test-dm1.md");
        DmAgentPromptConfig cfg = new DmAgentPromptConfig();

        String prompt = cfg.dmAgentSystemPrompt();

        assertThat(prompt).contains("TEST DM1 PROMPT");
    }

    @Test
    void defaultPromptUsedWhenNoOverride() {
        DmAgentPromptConfig cfg = new DmAgentPromptConfig();

        String prompt = cfg.dmAgentSystemPrompt();

        assertThat(prompt).contains("DM1 – Island DM Agent System Prompt");
    }

    @Test
    void missingOverrideFallsBackToDefault() {
        System.setProperty("dm1.prompt.path", "src/test/resources/prompts/missing-dm1.md");
        DmAgentPromptConfig cfg = new DmAgentPromptConfig();

        String prompt = cfg.dmAgentSystemPrompt();

        assertThat(prompt).contains("DM1 – Island DM Agent System Prompt");
    }
}
