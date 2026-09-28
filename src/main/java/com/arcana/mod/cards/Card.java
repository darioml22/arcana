package com.arcana.mod.cards;

/**
 * Execution-facing representation of a card in a spell loadout.
 */
public interface Card {
    int manaCost();

    CardCategory category();
}