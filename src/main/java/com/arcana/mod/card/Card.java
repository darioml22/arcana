package com.arcana.mod.card;

import java.util.Stack;

import com.arcana.mod.engine.ExecutionContext;

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
    private final int textureIndex;

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

    public abstract boolean execute(ExecutionContext context, Stack<Card> modStack, Integer effectCredits);

    public Type getCardType() { return cardType; }
    public String getCardName() { return cardName; }
    public String getDescription() { return cardDescription; }
    public int getTextureIndex() { return textureIndex; }
}