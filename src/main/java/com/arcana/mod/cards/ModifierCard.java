package com.arcana.mod.cards;

public interface ModifierCard extends Card {
    boolean canAttachTo(EffectCard effectCard);

    @Override
    default CardCategory category() {
        return CardCategory.MODIFIER;
    }
}