package com.arcana.mod.card;

import net.minecraft.world.item.Item;

public abstract class Card extends Item {
    public enum Type {
        EFFECT,
        LOGIC,
        MOD
    }

    private final Type cardType;
    private final String cardName;
    private final String cardDescription;
    private final int textureIndex; //not sure how this works

    public Card(
            Item.Properties properties,
            Type cardType,
            String cardName,
            String cardDescription,
            int textureIndex
    ) {
        super(properties);
        this.cardType = cardType;
        this.cardName = cardName;
        this.cardDescription = cardDescription;
        this.textureIndex = textureIndex;
    }

    public abstract String execute();

    public Type getCardType() {
        return cardType;
    }

    public String getCardName() {
        return cardName;
    }

    public String getDescription() {
        return cardDescription;
    }

    public int getTextureIndex() {
        return textureIndex;
    }
}
