package com.demo.island.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GhostAgentPromptConfigTest {

    @Test
    void loadsPromptFromPath() {
        System.setProperty("ghost1.prompt.path", "src/test/resources/prompts/test-ghost1.md");
        GhostAgentPromptConfig cfg = new GhostAgentPromptConfig();
        String prompt = cfg.ghostAgentSystemPrompt();
        assertThat(prompt).contains("Ghost1 test prompt");
        System.clearProperty("ghost1.prompt.path");
    }

    @Test
    void missingPromptFallsBack() {
        System.setProperty("ghost1.prompt.path", "src/test/resources/prompts/missing-ghost1.md");
        GhostAgentPromptConfig cfg = new GhostAgentPromptConfig();
        String prompt = cfg.ghostAgentSystemPrompt();
        assertThat(prompt).contains("Ghost1 – Island Ghost Agent System Prompt");
        System.clearProperty("ghost1.prompt.path");
    }
}
