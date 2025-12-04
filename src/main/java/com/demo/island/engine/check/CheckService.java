package com.demo.island.engine.check;

import com.demo.island.core.WorldState;
import com.demo.island.engine.dice.DiceService;

public final class CheckService {

    private final DiceService diceService;
    private final CheckConfig checkConfig;
    private final java.util.concurrent.atomic.AtomicLong idSequence = new java.util.concurrent.atomic.AtomicLong(1);

    public CheckService(DiceService diceService) {
        this(diceService, CheckConfig.defaultConfig());
    }

    public CheckService(DiceService diceService, CheckConfig checkConfig) {
        this.diceService = diceService;
        this.checkConfig = checkConfig;
    }

    public CheckResult evaluate(WorldState worldState, CheckRequest request) {
        int roll = diceService.d20();
        int modifier = checkConfig.modifierFor(worldState, request);
        int total = roll + modifier;
        boolean success = total >= request.getDifficulty();
        String checkId = "chk-" + idSequence.getAndIncrement();
        return new CheckResult(request, success, roll, modifier, total, checkId);
    }

    public DiceService getDiceService() {
        return diceService;
    }
}
