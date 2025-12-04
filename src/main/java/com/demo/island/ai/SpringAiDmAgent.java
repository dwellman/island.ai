package com.demo.island.ai;

import com.demo.island.dto.DmDecisionDto;
import com.demo.island.engine.DmAgent;
import com.demo.island.engine.DmDecision;
import com.demo.island.engine.DmInput;
import com.demo.island.engine.check.CheckService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.client.ChatClient;
import com.demo.island.tools.DmTools;

import java.util.logging.Logger;

/**
 * DM agent backed by Spring AI ChatClient. Loads system/dm prompts from classpath and
 * exchanges JSON using the frozen DmInputDto/DmDecisionDto contract.
 */
public final class SpringAiDmAgent implements DmAgent {

    private static final Logger LOG = Logger.getLogger(SpringAiDmAgent.class.getName());

    private final ChatClient chatClient;
    private final CheckService checkService;
    private final DmTools dmTools;
    private final String systemPrompt;
    private final String dmPrompt;
    private final ObjectMapper mapper;

    public SpringAiDmAgent(ChatClient chatClient, CheckService checkService, DmTools dmTools) {
        this(chatClient, checkService, dmTools,
                PromptLoader.load("system.md"),
                PromptLoader.load("dm.prompt.md"));
    }

    public SpringAiDmAgent(ChatClient chatClient, CheckService checkService, DmTools dmTools,
                           String systemPrompt, String dmPrompt) {
        this.chatClient = chatClient;
        this.checkService = checkService;
        this.dmTools = dmTools;
        this.systemPrompt = systemPrompt == null ? "" : systemPrompt;
        this.dmPrompt = dmPrompt == null ? "" : dmPrompt;
        this.mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    @Override
    public DmDecision decide(DmInput input) {
        String jsonInput;
        try {
            jsonInput = mapper.writeValueAsString(input.toDto());
        } catch (JsonProcessingException e) {
            LOG.warning("Failed to serialize DM input: " + e.getMessage());
            DmDecision fallback = new DmDecision("The DM falters, unable to parse the world.", false);
            fallback.addError("Serialization error: " + e.getMessage());
            return fallback;
        }

        String response;
        try {
            response = chatClient.prompt()
                    .system(systemPrompt + "\n\n" + dmPrompt)
                    .user(jsonInput)
                    .call()
                    .content();
        } catch (Exception e) {
            LOG.warning("DM agent call failed: " + e.getMessage());
            DmDecision fallback = new DmDecision("The DM is silent for a moment.", false);
            fallback.addError("DM agent call failed: " + e.getMessage());
            return fallback;
        }

        try {
            DmDecisionDto dto = mapper.readValue(response, DmDecisionDto.class);
            DmDecision decision = dto.toDecision(input.getWorldState(), input.getCommand().getPlayerId(), checkService);
            return enforceTurnConsumptionDefault(decision, input.getCommand().getRawText());
        } catch (Exception e) {
            LOG.warning("Failed to parse DM response: " + e.getMessage());
            DmDecision fallback = new DmDecision("The DM mutters uncertainly.", false);
            fallback.addError("DM response parse failed: " + e.getMessage());
            return fallback;
        }
    }

    /**
     * Safety guard: if the DM does not explicitly mark a non-meta command as non-consuming,
     * assume it burns a turn. This prevents the simulation from getting stuck on turn 0 when
     * the LLM omits turnConsumesTime.
     */
    private DmDecision enforceTurnConsumptionDefault(DmDecision decision, String rawCommand) {
        boolean isMeta = isMetaCommand(rawCommand);
        boolean consumes = decision.isTurnConsumesTime() || (!isMeta);
        if (consumes == decision.isTurnConsumesTime()) {
            return decision;
        }

        DmDecision adjusted = new DmDecision(decision.getNarration(), consumes);
        decision.getStateChanges().forEach(adjusted::addStateChange);
        decision.getCheckResults().forEach(adjusted::addCheckResult);
        decision.getHints().forEach(adjusted::addHint);
        decision.getErrors().forEach(adjusted::addError);
        return adjusted;
    }

    private boolean isMetaCommand(String rawCommand) {
        if (rawCommand == null || rawCommand.trim().isEmpty()) {
            return true;
        }
        String upper = rawCommand.trim().toUpperCase();
        return upper.equals("HELP") || upper.equals("?") || upper.equals("TIME") || upper.equals("STATUS");
    }
}
