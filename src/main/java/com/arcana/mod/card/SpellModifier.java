package com.arcana.mod.card;

import java.util.ArrayList;
import java.util.List;

public class SpellModifier {
    private final List<StatChange> changes = new ArrayList<>();

    public void add(StatChange change) {
        changes.add(change);
    }

    public boolean isEmpty() {
        return changes.isEmpty();
    }

    public float resolve(SpellStat stat, float base) {
        float effectiveBase = base;
        float flat = 0F;
        float additiveSum = 0F;
        float product = 1F;

        for (StatChange c : changes) {
            if (c.stat() != stat) continue;

            switch (c.op()) {
                case ADD, REMOVE -> flat += c.value(); // REMOVE is stored negated
                case MULTIPLY_ADDITIVE -> additiveSum += c.value();
                case MULTIPLY -> product *= c.value();
                case SET -> effectiveBase = c.value(); // last SET wins
            }
        }

        return (effectiveBase + flat) * (1F + additiveSum) * product;
    }
}
