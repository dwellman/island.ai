package com.demo.island.dto;

import com.demo.island.core.ItemInstance;
import com.demo.island.core.WorldState;
import com.demo.island.engine.DmDecision;
import com.demo.island.engine.MovePlayerChange;
import com.demo.island.engine.FlagTarget;
import com.demo.island.engine.SetFlagChange;
import com.demo.island.engine.StateChange;
import com.demo.island.engine.TransferItemChange;
import com.demo.island.engine.actions.ItemActionHandler;
import com.demo.island.engine.actions.ItemActionRegistry;
import com.demo.island.engine.actions.ItemActionResult;
import com.demo.island.engine.check.CheckRequest;
import com.demo.island.engine.check.CheckResult;
import com.demo.island.engine.check.CheckService;
import com.demo.island.engine.check.CheckSubjectKind;
import com.demo.island.engine.check.CheckType;

import java.util.ArrayList;
import java.util.List;

public final class DmDecisionDto {

    private final String narration;
    private final boolean turnConsumesTime;
    private final List<StateChangeDto> actions;
    private final List<String> hints;
    private final List<String> errors;

    public DmDecisionDto(String narration, boolean turnConsumesTime, List<StateChangeDto> actions) {
        this(narration, turnConsumesTime, actions, List.of(), List.of());
    }

    public DmDecisionDto(String narration, boolean turnConsumesTime, List<StateChangeDto> actions,
                         List<String> hints, List<String> errors) {
        this.narration = narration;
        this.turnConsumesTime = turnConsumesTime;
        this.actions = actions == null ? List.of() : List.copyOf(actions);
        this.hints = hints == null ? List.of() : List.copyOf(hints);
        this.errors = errors == null ? List.of() : List.copyOf(errors);
    }

    public String getNarration() {
        return narration;
    }

    public boolean isTurnConsumesTime() {
        return turnConsumesTime;
    }

    public List<StateChangeDto> getStateChanges() {
        return actions;
    }

    public List<StateChangeDto> getActions() {
        return actions;
    }

    public List<String> getHints() {
        return hints;
    }

    public List<String> getErrors() {
        return errors;
    }

    public DmDecision toDecision(WorldState worldState, String playerIdFallback) {
        return toDecision(worldState, playerIdFallback, null);
    }

    public DmDecision toDecision(WorldState worldState, String playerIdFallback, CheckService checkService) {
        DmDecision decision = new DmDecision(narration, turnConsumesTime);
        hints.forEach(decision::addHint);
        errors.forEach(decision::addError);

        List<StateChange> mapped = mapStateChanges(worldState, playerIdFallback, checkService, decision);
        mapped.forEach(decision::addStateChange);
        return decision;
    }

    private List<StateChange> mapStateChanges(WorldState worldState, String playerIdFallback,
                                              CheckService checkService,
                                              DmDecision decision) {
        List<StateChange> mapped = new ArrayList<>();
        for (StateChangeDto dto : actions) {
            switch (dto.getKind()) {
                case MOVE_PLAYER -> mapped.add(new MovePlayerChange(
                        dto.getPlayerId() != null ? dto.getPlayerId() : playerIdFallback,
                        dto.getTargetTileId()
                ));
                case TRANSFER_ITEM -> mapped.add(new TransferItemChange(
                        dto.getItemId(),
                        ItemInstance.OwnerKind.valueOf(dto.getOwnerKind()),
                        dto.getOwnerId(),
                        dto.getContainedByItemId()
                ));
                case SET_FLAG -> mapped.add(new SetFlagChange(
                        dto.getFlagTarget() != null ? FlagTarget.valueOf(dto.getFlagTarget()) : FlagTarget.SESSION,
                        null,
                        dto.getFlagName(),
                        dto.isFlagValue()
                ));
                case RUN_ITEM_HOOK -> {
                    ItemActionHandler handler = ItemActionRegistry.find(dto.getHookId());
                    ItemInstance item = worldState.getItem(dto.getItemId());
                    if (handler != null && item != null) {
                        ItemActionResult result = handler.apply(worldState, worldState.getPlayer(playerIdFallback), item);
                        mapped.addAll(result.getStateChanges());
                    }
                }
                case CHECK -> {
                    if (checkService != null && dto.getCheckType() != null && dto.getDifficulty() != null) {
                        CheckRequest req = new CheckRequest(
                                CheckType.valueOf(dto.getCheckType()),
                                dto.getCheckSubjectKind() != null
                                        ? CheckSubjectKind.valueOf(dto.getCheckSubjectKind())
                                        : CheckSubjectKind.PLAYER,
                                dto.getCheckSubjectId() != null ? dto.getCheckSubjectId() : playerIdFallback,
                                dto.getDifficulty()
                        );
                        CheckResult result = checkService.evaluate(worldState, req);
                        decision.addCheckResult(result);
                    }
                }
                default -> {
                }
            }
        }
        return mapped;
    }
}
