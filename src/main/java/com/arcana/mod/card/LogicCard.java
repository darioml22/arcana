package com.arcana.mod.card;

import net.minecraft.world.item.Item;

public class LogicCard extends Card {
    private final int spellId;

    public LogicCard(
            Item.Properties properties,
            int spellId,
            Type cardType,
            String cardName,
            String cardDescription,
            int textureIndex
    ) {
        super(properties, cardType, cardName, cardDescription, textureIndex);
        this.spellId = spellId;
    }

    @Override
    public String execute() {
        return "Logic Card executed: " + getCardName();
    }

    public int getSpellId() {
        return spellId;
    }
}
