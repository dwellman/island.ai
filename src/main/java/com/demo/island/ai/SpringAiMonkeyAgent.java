package com.demo.island.ai;

import com.demo.island.engine.check.CheckRequest;
import com.demo.island.engine.check.CheckSubjectKind;
import com.demo.island.engine.check.CheckType;
import com.demo.island.monkey.MonkeyAgent;
import com.demo.island.monkey.MonkeyDecision;
import com.demo.island.monkey.MonkeyDecisionDto;
import com.demo.island.monkey.MonkeyInput;
import com.demo.island.monkey.MonkeyIntent;
import com.demo.island.monkey.MonkeyIntentDto;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import com.demo.island.tools.MonkeyTools;

import java.util.logging.Logger;

/**
 * Monkey brain backed by Spring AI ChatClient. Uses monkey.brain.prompt.md as system message and exchanges
 * JSON via MonkeyInputDto/MonkeyDecisionDto.
 */
public final class SpringAiMonkeyAgent implements MonkeyAgent {

    private static final Logger LOG = Logger.getLogger(SpringAiMonkeyAgent.class.getName());

    private final ChatClient chatClient;
    private final MonkeyTools monkeyTools;
    private final String systemPrompt;
    private final ObjectMapper mapper;

    public SpringAiMonkeyAgent(ChatClient chatClient, MonkeyTools monkeyTools) {
        this(chatClient, monkeyTools, PromptLoader.load("monkey.brain.prompt.md"));
    }

    public SpringAiMonkeyAgent(ChatClient chatClient, MonkeyTools monkeyTools, String systemPrompt) {
        this.chatClient = chatClient;
        this.monkeyTools = monkeyTools;
        this.systemPrompt = systemPrompt == null ? "" : systemPrompt;
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public MonkeyDecision decide(MonkeyInput input) {
        String jsonInput;
        try {
            jsonInput = mapper.writeValueAsString(input.toDto(
                    input.getWorldState().getCreatures().values().stream()
                            .filter(c -> c.getKind() == com.demo.island.core.Creature.CreatureKind.MONKEY_TROOP)
                            .map(com.demo.island.core.Creature::getCreatureId)
                            .findFirst()
                            .orElse(""),
                    input.getWorldState().getTiles().containsKey("T_VINES") ? "T_VINES" : ""
            ));
        } catch (Exception e) {
            LOG.warning("Failed to serialize Monkey input: " + e.getMessage());
            MonkeyDecision fallback = new MonkeyDecision("Monkeys hesitate.", true);
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
            LOG.warning("Monkey agent call failed: " + e.getMessage());
            MonkeyDecision fallback = new MonkeyDecision("Monkeys idle.", true);
            fallback.addError("Monkey agent call failed: " + e.getMessage());
            return fallback;
        }

        try {
            MonkeyDecisionDto dto = mapper.readValue(response, MonkeyDecisionDto.class);
            return map(dto);
        } catch (Exception e) {
            LOG.warning("Failed to parse Monkey response: " + e.getMessage());
            MonkeyDecision fallback = new MonkeyDecision("Monkeys chatter incoherently.", true);
            fallback.addError("Monkey response parse failed: " + e.getMessage());
            return fallback;
        }
    }

    private MonkeyDecision map(MonkeyDecisionDto dto) {
        MonkeyDecision decision = new MonkeyDecision(dto.getNarration(), dto.isTurnConsumesTime());
        decision.setDailyPhase(dto.getDailyPhase());
        decision.setTargetTileId(dto.getTargetTileId());
        dto.getMonkeyIntents().forEach(intentDto -> decision.addIntent(mapIntent(intentDto)));
        dto.getCheckRequests().forEach(reqDto -> {
            if (reqDto.difficulty() != null) {
                CheckType type = CheckType.valueOf(reqDto.checkType() != null ? reqDto.checkType() : CheckType.GENERIC.name());
                CheckSubjectKind subjectKind = reqDto.subjectKind() != null ? CheckSubjectKind.valueOf(reqDto.subjectKind()) : CheckSubjectKind.CREATURE;
                CheckRequest req = monkeyTools.genericCheck(
                        reqDto.subjectId(),
                        reqDto.difficulty()
                );
                // override type/subjectKind if provided
                req = new CheckRequest(type, subjectKind, reqDto.subjectId(), reqDto.difficulty());
                decision.addCheckRequest(req);
            }
        });
        dto.getHints().forEach(decision::addHint);
        dto.getErrors().forEach(decision::addError);
        return decision;
    }

    private MonkeyIntent mapIntent(MonkeyIntentDto dto) {
        MonkeyIntent.IntentKind kind;
        try {
            kind = MonkeyIntent.IntentKind.valueOf(dto.getIntent());
        } catch (IllegalArgumentException e) {
            kind = MonkeyIntent.IntentKind.IGNORE_PLAYERS;
        }
        if (dto.getPlayerId() != null) {
            return switch (kind) {
                case STEAL_BANANA_FROM_PLAYER -> monkeyTools.stealBanana(dto.getPlayerId());
                case FOLLOW_BANANA_CARRIER -> monkeyTools.followBanana(dto.getPlayerId());
                case THROW_POO_AT_PLAYER -> monkeyTools.throwPoo(dto.getPlayerId());
                default -> MonkeyIntent.targeting(kind, dto.getPlayerId());
            };
        }
        return MonkeyIntent.of(kind);
    }
}
