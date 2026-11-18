package com.arcana.mod.card;

import com.arcana.mod.engine.ExecutionContext;

import net.minecraft.world.item.Item;

// Logic cards alter how the wand reads its cards (skipping, jumping, reversing, conditions...).
// Each one provides its own LogicAction, which manipulates the focus through the
// ExecutionContext API.
public class LogicCard extends Card {

    @FunctionalInterface
    public interface LogicAction {
        // Returns false if the wand should fail
        boolean apply(ExecutionContext ctx);
    }

    private final LogicAction action;

    public LogicCard(Item.Properties properties, String cardName, String cardDescription,
                     int textureIndex, float manaCost, LogicAction action) {
        super(properties, Type.LOGIC, cardName, cardDescription, textureIndex, manaCost);
        this.action = action;
    }

    @Override
    public boolean execute(ExecutionContext context) {
        return action.apply(context);
    }
}
