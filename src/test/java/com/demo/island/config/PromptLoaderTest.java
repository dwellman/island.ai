package com.demo.island.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

class PromptLoaderTest {

    private static final Logger LOG = LogManager.getLogger(PromptLoaderTest.class);

    @Test
    void usesDefaultWhenNoOverride() throws Exception {
        Path defaultFile = Files.createTempFile("prompt-default", ".md");
        Files.writeString(defaultFile, "DEFAULT PROMPT");

        String prompt = PromptLoader.loadOrFallback(defaultFile.toString(), null, "FALLBACK", LOG, "TEST");

        assertThat(prompt).isEqualTo("DEFAULT PROMPT");
    }

    @Test
    void usesOverrideWhenPresent() throws Exception {
        Path defaultFile = Files.createTempFile("prompt-default", ".md");
        Files.writeString(defaultFile, "DEFAULT PROMPT");
        Path overrideFile = Files.createTempFile("prompt-override", ".md");
        Files.writeString(overrideFile, "OVERRIDE PROMPT");

        String prompt = PromptLoader.loadOrFallback(defaultFile.toString(), overrideFile.toString(), "FALLBACK", LOG, "TEST");

        assertThat(prompt).isEqualTo("OVERRIDE PROMPT");
    }

    @Test
    void fallsBackWhenBothMissing() {
        String prompt = PromptLoader.loadOrFallback("missing-default.md", "missing-override.md", "FALLBACK", LOG, "TEST");

        assertThat(prompt).isEqualTo("FALLBACK");
    }
}
