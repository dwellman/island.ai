package com.demo.island.game;

import java.util.Random;

/**
 * Simple dice roller utility.
 */
public final class DiceService {

    private final Random random;

    public DiceService() {
        this(new Random());
    }

    public DiceService(Random random) {
        this.random = random;
    }

    public int rollD20() {
        return roll(1, 20);
    }

    public int roll(int count, int sides) {
        int total = 0;
        for (int i = 0; i < count; i++) {
            total += random.nextInt(sides) + 1;
        }
        return total;
    }

    public int rollD20WithAdvantage() {
        int a = rollD20();
        int b = rollD20();
        return Math.max(a, b);
    }

    public int rollD20WithDisadvantage() {
        int a = rollD20();
        int b = rollD20();
        return Math.min(a, b);
    }
}
