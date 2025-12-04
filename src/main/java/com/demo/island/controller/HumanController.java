package com.demo.island.controller;

import java.util.Scanner;

/**
 * Human controller that reads a command from console input.
 */
public final class HumanController implements ActorController {

    private final Scanner scanner;
    private final String playerId;

    public HumanController(Scanner scanner, String playerId) {
        this.scanner = scanner;
        this.playerId = playerId;
    }

    @Override
    public ActorIntent decide(ActorView view) {
        System.out.print("> ");
        String line = scanner.nextLine();
        return new ActorIntent(ActorIntent.Kind.PLAYER_COMMAND, line);
    }

    public String getPlayerId() {
        return playerId;
    }
}
