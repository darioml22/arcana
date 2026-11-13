package com.arcana.mod.card;

import java.util.ArrayList;
import java.util.List;

public class SpellModifier {
    private final List<StatChange> changes = new ArrayList<>();

    public void add(StatChange change) {
        changes.add(change);
    }

    public float resolve(SpellStat stat, float base) {
        float flat = 0F;
        float additiveSum = 0F;
        boolean hasAdditive = false;
        float product = 1F;

        for (StatChange c : changes) {
            if (c.stat() != stat) continue;

            switch (c.op()) {
                case ADD -> flat += c.value();
                case MULTIPLY_ADDITIVE -> {
                    additiveSum += c.value();
                    hasAdditive = true;
                }
                case MULTIPLY -> product *= c.value();
            }
        }

        float additiveMultiplier = hasAdditive ? additiveSum : 1F;

        return (base + flat) * additiveMultiplier * product;
    }
}