package com.demo.island.game;

import java.util.Map;

final class DmMessageMapper {

    private static final Map<ReasonCode, String> REASON_TO_MSG = Map.of(
            ReasonCode.NO_VISIBLE_ITEMS_HERE, "You haven't found any items here.",
            ReasonCode.ALREADY_CARRYING_ITEM, "You are already carrying that.",
            ReasonCode.NO_EXIT_IN_DIRECTION, "You can't go that way.",
            ReasonCode.NEEDS_DIRECTION, "You need a direction.",
            ReasonCode.NEEDS_ITEM, "You need to specify an item.",
            ReasonCode.NOT_CARRYING_ITEM, "You don't have that."
    );

    private DmMessageMapper() {
    }

    static String messageFor(ToolOutcome outcome, String timePrefix) {
        String body = bodyFor(outcome);
        return messageForBody(body, timePrefix);
    }

    static String bodyFor(ToolOutcome outcome) {
        if (outcome == null) return "You can't do that.";
        if (outcome.getOutcomeType() == OutcomeType.BLOCKED) {
            String mapped = REASON_TO_MSG.get(outcome.getReasonCode());
            if (mapped != null && !mapped.isBlank()) {
                return mapped;
            }
            if (outcome.getDmText() != null && !outcome.getDmText().isBlank()) {
                return stripPrefix(outcome.getDmText());
            }
            return "You can't do that.";
        }
        if (outcome.getDmText() != null && !outcome.getDmText().isBlank()) {
            return outcome.getDmText();
        }
        return "You can't do that.";
    }

    static String messageForBody(String body, String timePrefix) {
        String resolved = (body == null || body.isBlank()) ? "You can't do that." : body;
        if (resolved.startsWith("[")) {
            return resolved;
        }
        String prefix = timePrefix == null ? "" : timePrefix;
        return prefix.isBlank() ? resolved : (prefix + " " + resolved);
    }

    private static String stripPrefix(String text) {
        String t = text.trim();
        if (t.startsWith("[")) {
            int idx = t.indexOf("]");
            if (idx >= 0 && idx + 1 < t.length()) {
                return t.substring(idx + 1).trim();
            }
        }
        return t;
    }
}
