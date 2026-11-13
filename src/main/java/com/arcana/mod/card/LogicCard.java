package com.arcana.mod.card;

import java.util.Stack;

import com.arcana.mod.engine.ExecutionContext;

import net.minecraft.world.item.Item;

public class LogicCard extends Card {
    public LogicCard(Item.Properties properties, String cardName,
                     String cardDescription, int textureIndex) {
        super(properties, Type.LOGIC, cardName, cardDescription, textureIndex);
    }

    @Override
    public boolean execute(ExecutionContext context, Stack<Card> modStack, Integer effectCredits) {
        return true;
    }
}