package com.arcana.mod.engine;

// Outcome of a single wand use (one right click).
public enum CastResult {
    // An effect fired and there are still cards ahead: apply the cast delay.
    CAST(true, false, false),
    // An effect fired and it was the last card: the loop ends, apply the recharge time.
    CAST_END_OF_WAND(true, true, false),
    // The end of the wand was reached without firing anything: apply the recharge time.
    END_OF_WAND(false, true, false),
    // The accumulated mana cost exceeded the wand's current mana.
    FAILURE_NO_MANA(false, true, true),
    // The execution limit was hit (most likely an infinite loop).
    FAILURE_EXECUTION_LIMIT(false, true, true),
    // A card reported a failure or threw an exception.
    FAILURE_CARD(false, true, true),
    // The wand holds no cards.
    EMPTY(false, false, false);

    private final boolean fired;
    private final boolean recharge;
    private final boolean failure;

    CastResult(boolean fired, boolean recharge, boolean failure) {
        this.fired = fired;
        this.recharge = recharge;
        this.failure = failure;
    }

    public boolean fired() { return fired; }
    public boolean triggersRecharge() { return recharge; }
    public boolean isFailure() { return failure; }
}
