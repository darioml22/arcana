package com.arcana.mod.card;

import java.util.Set;
import java.util.Stack;

import com.arcana.mod.engine.ExecutionContext;

import net.minecraft.world.item.Item;

public class EffectCard extends Card {

    @FunctionalInterface
    public interface EffectAction {
        void fire(ExecutionContext ctx, SpellModifier mods);
    }

    private final String effectDescription;
    private final Set<SpellStat> supportedStats;
    private final EffectAction action;

    public EffectCard(Item.Properties properties, String cardName, String cardDescription,
                      int textureIndex, String effectDescription,
                      Set<SpellStat> supportedStats, EffectAction action) {
        super(properties, Type.EFFECT, cardName, cardDescription, textureIndex);
        this.effectDescription = effectDescription;
        this.supportedStats = Set.copyOf(supportedStats);
        this.action = action;
    }

    @Override
    public boolean execute(ExecutionContext context, Stack<Card> modStack, Integer effectCredits) {
        if (effectCredits <= 0) {
            return false;  
        }

        SpellModifier mods = new SpellModifier();
        while (!modStack.isEmpty()) {
            if (modStack.pop() instanceof ModCard mod) {
                for (StatChange change : mod.getChanges()) {
                    if (supports(change.stat())) {
                        mods.add(change);
                    }
                }
            }
        }

        action.fire(context, mods);
        return true;
    }

    public boolean supports(SpellStat stat) {
        return supportedStats.contains(stat);
    }

    public String getEffectDescription() { return effectDescription; }
}