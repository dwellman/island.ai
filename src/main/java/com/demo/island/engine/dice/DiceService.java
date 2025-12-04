package com.demo.island.engine.dice;

import java.util.Random;

public final class DiceService {

    private final Random random;

    public DiceService() {
        this(new Random());
    }

    public DiceService(long seed) {
        this(new Random(seed));
    }

    private DiceService(Random random) {
        this.random = random;
    }

    public int roll(int sides) {
        if (sides <= 0) {
            throw new IllegalArgumentException("sides must be > 0");
        }
        return random.nextInt(sides) + 1;
    }

    public int d20() {
        return roll(20);
    }
}
