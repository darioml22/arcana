package com.arcana.mod.card;

import net.minecraft.world.item.Item;

public class EffectCard extends Card {
    private final String effectDescription;

    public EffectCard(
            Item.Properties properties,
            Type cardType,
            String cardName,
            String cardDescription,
            int textureIndex,
            String effectDescription
    ) {
        super(properties, cardType, cardName, cardDescription, textureIndex);
        this.effectDescription = effectDescription;
    }

    @Override
    public String execute() {
        return "Effect Card executed: " + getCardName();
    }

    public String getEffectDescription() {
        return effectDescription;
    }
}
