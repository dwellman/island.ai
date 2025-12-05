package com.demo.island.config;

import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.Logger;

public final class PromptLoader {

    private PromptLoader() {
    }

    public static String loadOrFallback(String defaultPath, String overridePath, String fallbackContent, Logger log, String label) {
        String resolvedDefault = defaultPath;
        String resolvedOverride = (overridePath == null || overridePath.isBlank()) ? null : overridePath;
        try {
            if (resolvedOverride != null) {
                String overrideContent = read(resolvedOverride);
                if (overrideContent != null) {
                    log.info("{} prompt: loaded from {} (override)", label, resolvedOverride);
                    return overrideContent;
                } else {
                    log.warn("{} prompt: override path {} could not be loaded; attempting default {}.", label, resolvedOverride, resolvedDefault);
                }
            }
            String content = read(resolvedDefault);
            if (content != null) {
                log.info("{} prompt: loaded from {}", label, resolvedDefault);
                return content;
            }
        } catch (Exception ex) {
            // fall through to fallback
        }
        if (resolvedOverride != null) {
            log.warn("{} prompt: could not be loaded from {} or override {}; using fallback.", label, resolvedDefault, resolvedOverride);
        } else {
            log.warn("{} prompt: could not be loaded from {}; using fallback.", label, resolvedDefault);
        }
        return fallbackContent;
    }

    private static String read(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (Exception ex) {
            return null;
        }
    }
}
