package com.demo.island.game;

public final class ToolOutcome {
    private final OutcomeType outcomeType;
    private final ReasonCode reasonCode;
    private final String dmText;
    private final TurnContext turnContext;
    private final Challenge challenge;
    private final ChallengeResult challengeResult;

    public ToolOutcome(OutcomeType outcomeType,
                       ReasonCode reasonCode,
                       String dmText,
                       TurnContext turnContext,
                       Challenge challenge,
                       ChallengeResult challengeResult) {
        this.outcomeType = outcomeType;
        this.reasonCode = reasonCode;
        this.dmText = dmText;
        this.turnContext = turnContext;
        this.challenge = challenge;
        this.challengeResult = challengeResult;
    }

    public OutcomeType getOutcomeType() {
        return outcomeType;
    }

    public ReasonCode getReasonCode() {
        return reasonCode;
    }

    public String getDmText() {
        return dmText;
    }

    public TurnContext getTurnContext() {
        return turnContext;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public ChallengeResult getChallengeResult() {
        return challengeResult;
    }
}
