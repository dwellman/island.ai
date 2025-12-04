package com.demo.island.ai;

import com.demo.island.engine.check.CheckRequest;
import com.demo.island.engine.check.CheckType;
import com.demo.island.ghost.GhostAgent;
import com.demo.island.ghost.GhostDecision;
import com.demo.island.ghost.GhostDecisionDto;
import com.demo.island.ghost.GhostInput;
import com.demo.island.ghost.GhostIntent;
import com.demo.island.ghost.GhostIntentDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import com.demo.island.tools.GhostTools;

import java.util.logging.Logger;

/**
 * Ghost brain backed by Spring AI ChatClient. Uses ghost.prompt.md as system message and exchanges
 * JSON via GhostInputDto/GhostDecisionDto.
 */
public final class SpringAiGhostAgent implements GhostAgent {

    private static final Logger LOG = Logger.getLogger(SpringAiGhostAgent.class.getName());

    private final ChatClient chatClient;
    private final GhostTools ghostTools;
    private final String systemPrompt;
    private final ObjectMapper mapper;

    public SpringAiGhostAgent(ChatClient chatClient, GhostTools ghostTools) {
        this(chatClient, ghostTools, PromptLoader.load("ghost.prompt.md"));
    }

    public SpringAiGhostAgent(ChatClient chatClient, GhostTools ghostTools, String systemPrompt) {
        this.chatClient = chatClient;
        this.ghostTools = ghostTools;
        this.systemPrompt = systemPrompt == null ? "" : systemPrompt;
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public GhostDecision decide(GhostInput input) {
        String jsonInput;
        try {
            jsonInput = mapper.writeValueAsString(input.toDto());
        } catch (Exception e) {
            LOG.warning("Failed to serialize Ghost input: " + e.getMessage());
            GhostDecision fallback = new GhostDecision("Ghost is silent.", true);
            fallback.addError("Serialization error: " + e.getMessage());
            return fallback;
        }

        String response;
        try {
            response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(jsonInput)
                    .call()
                    .content();
        } catch (Exception e) {
            LOG.warning("Ghost agent call failed: " + e.getMessage());
            GhostDecision fallback = new GhostDecision("Ghost hesitates.", true);
            fallback.addError("Ghost agent call failed: " + e.getMessage());
            return fallback;
        }

        try {
            GhostDecisionDto dto = mapper.readValue(response, GhostDecisionDto.class);
            return map(dto);
        } catch (Exception e) {
            LOG.warning("Failed to parse Ghost response: " + e.getMessage());
            GhostDecision fallback = new GhostDecision("Ghost mutters aimlessly.", true);
            fallback.addError("Ghost response parse failed: " + e.getMessage());
            return fallback;
        }
    }

    private GhostDecision map(GhostDecisionDto dto) {
        GhostDecision decision = new GhostDecision(dto.getNarration(), dto.isTurnConsumesTime());
        dto.getHints().forEach(decision::addHint);
        dto.getErrors().forEach(decision::addError);
        dto.getActions().forEach(action -> decision.addAction(mapIntent(action)));
        return decision;
    }

    private GhostIntent mapIntent(GhostIntentDto dto) {
        GhostIntent.Verb verb;
        try {
            verb = GhostIntent.Verb.valueOf(dto.getVerb());
        } catch (IllegalArgumentException e) {
            return new GhostIntent(GhostIntent.Verb.SET_TARGET_TILE, dto.getCreatureId(), dto.getTargetTileId(),
                    null, null, null, null, null, null, null);
        }

        if (verb == GhostIntent.Verb.CHECK) {
            CheckRequest req = ghostTools.hearingCheck(
                    dto.getCheckSubjectId() != null ? dto.getCheckSubjectId() : dto.getCreatureId(),
                    dto.getDifficulty() != null ? dto.getDifficulty() : 10
            );
            return ghostTools.checkIntent(req);
        }
        if (verb == GhostIntent.Verb.SET_FLAG) {
            return GhostIntent.setFlag(
                    dto.getFlagTarget(),
                    dto.getFlagName(),
                    dto.getFlagValue() != null && dto.getFlagValue()
            );
        }
        return ghostTools.setTargetIntent(dto.getCreatureId(), dto.getTargetTileId());
    }
}
