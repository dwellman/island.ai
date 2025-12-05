package com.demo.island.player;

/**
 * Payload returned by Spring AI tool calls.
 */
public record ToolDecision(String tool, String args, String reason, String mood, String note) {
}
