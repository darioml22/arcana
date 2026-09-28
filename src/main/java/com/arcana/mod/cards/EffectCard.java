package com.arcana.mod.cards;

public interface EffectCard extends Card {
    String actionId();

    @Override
    default CardCategory category() {
        return CardCategory.EFFECT;
    }
}