package com.arcana.mod.card;

import java.util.List;

import com.arcana.mod.engine.ExecutionContext;

import net.minecraft.world.item.Item;

public class ModCard extends Card {
    private final List<StatChange> changes;

    public ModCard(Item.Properties properties, String cardName, String cardDescription,
                   int textureIndex, float manaCost, List<StatChange> changes) {
        super(properties, Type.MOD, cardName, cardDescription, textureIndex, manaCost);
        this.changes = List.copyOf(changes);
    }

    // Modifiers never act on their own: they wait in the stack for the next effect card.
    @Override
    public boolean execute(ExecutionContext context) {
        context.pushModifier(this);
        return true;
    }

    public List<StatChange> getChanges() { return changes; }
}
