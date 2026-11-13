package com.arcana.mod.card;

public record StatChange(SpellStat stat, Operation op, float value) {
    public enum Operation { ADD, REMOVE, MULTIPLY, MULTIPLY_ADDITIVE, SET }

    public static StatChange add(SpellStat stat, float value) {
        return new StatChange(stat, Operation.ADD, value);
    }

    public static StatChange remove(SpellStat stat, float value) {
        return new StatChange(stat, Operation.REMOVE, -value);
    }

    public static StatChange multiplyAdditive(SpellStat stat, float value) {
        return new StatChange(stat, Operation.MULTIPLY_ADDITIVE, value);
    }

    public static StatChange multiply(SpellStat stat, float value) {
        return new StatChange(stat, Operation.MULTIPLY, value);
    }
    public static StatChange set(SpellStat stat, float value) {
        return new StatChange(stat, Operation.REMOVE, -value);
    }
}