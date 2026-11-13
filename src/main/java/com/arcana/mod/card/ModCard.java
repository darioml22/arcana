package com.arcana.mod.card;

import java.util.List;
import java.util.Stack;

import com.arcana.mod.engine.ExecutionContext;

import net.minecraft.world.item.Item;

public class ModCard extends Card {
    private final List<StatChange> changes;

    public ModCard(Item.Properties properties, String cardName, String cardDescription,
                   int textureIndex, List<StatChange> changes) {
        super(properties, Type.MOD, cardName, cardDescription, textureIndex);
        this.changes = List.copyOf(changes);
    }

    @Override
    public boolean execute(ExecutionContext context, Stack<Card> modStack, Integer effectCredits) {
        modStack.push(this);  
        return true;
    }

    public List<StatChange> getChanges() { return changes; }
}