package com.arcana.mod.cards;

public interface LogicCard extends Card {
    LogicBlockBehavior blockBehavior();

    @Override
    default CardCategory category() {
        return CardCategory.LOGIC;
    }
}