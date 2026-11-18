package com.arcana.mod.card;

// A single change a modifier applies to a spell stat. See SpellModifier.resolve() for how
// they combine: (base + ADD/REMOVE) * (1 + sum of MULTIPLY_ADDITIVE) * product of MULTIPLY,
// where SET replaces the base.
public record StatChange(SpellStat stat, Operation op, float value) {
    public enum Operation { ADD, REMOVE, MULTIPLY, MULTIPLY_ADDITIVE, SET }

    public static StatChange add(SpellStat stat, float value) {
        return new StatChange(stat, Operation.ADD, value);
    }

    // Stored negated, so it resolves as a flat addition.
    public static StatChange remove(SpellStat stat, float value) {
        return new StatChange(stat, Operation.REMOVE, -value);
    }

    // Value is the increment: 0.1 means "+10%". Several of these are summed before multiplying.
    public static StatChange multiplyAdditive(SpellStat stat, float value) {
        return new StatChange(stat, Operation.MULTIPLY_ADDITIVE, value);
    }

    public static StatChange multiply(SpellStat stat, float value) {
        return new StatChange(stat, Operation.MULTIPLY, value);
    }

    public static StatChange set(SpellStat stat, float value) {
        return new StatChange(stat, Operation.SET, value);
    }
}
