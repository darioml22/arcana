package com.arcana.mod.engine;
import java.util.ArrayList;
import com.arcana.mod.card.Card;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class ExecutionContext {
    private ArrayList<Card> cards; // List of cards to be executed
    private int focus = 0; // Pointer to the current card being executed
    private boolean reversed = false; // Flag to indicate if the execution order is reversed
    // TODO: Wand context (current stats like mana, cooldown, etc.)
    // TODO: Implement minecraft context (current world state, player state, etc.)
    // TODO: Implement caster context (entity casting the spell, their stats, position, etc.)
    private Level level;
    private LivingEntity caster;

    public int getFocus() {
        return focus;
    }

    public void setFocus(int focus) {
        if (focus >= 0 && focus < cards.size()) {
            this.focus = focus;
        }
    }

    public void moveFocus(int steps) {
        if (reversed) {
            focus -= steps;
        } else {
            focus += steps;
        }
        // Ensure focus stays within bounds
        if (focus < 0) {
            focus = 0;
        } else if (focus >= cards.size()) {
            focus = cards.size() - 1;
        }
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public boolean reverseExecution() {
        this.reversed = !this.reversed;
        return this.reversed;
    }

    public Card getCurrentCard() {
        if (focus >= 0 && focus < cards.size()) {
            return cards.get(focus);
        }
        return null; // Return null if focus is out of bounds
    }

    public ExecutionContext() {
        this.cards = new ArrayList<>();
        this.level = level;
        this.caster = caster; 
    }

    public void addCard(Card card, Integer index) {
        if (index >= 0 && index <= cards.size()) {
            cards.add(index, card);
        } else {
            cards.add(card); // Add to the end if index is out of bounds
        }
    }

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void clearCards() {
        cards.clear();
    }

    public int getCardCount() {
        return cards.size();
    }

    public void removeCard(Integer index) {
        if (index >= 0 && index < cards.size()) {
            cards.remove((int) index);
        }
    }
    
    public void swapCards(int index1, int index2) {
        if (index1 >= 0 && index1 < cards.size() && index2 >= 0 && index2 < cards.size()) {
            Card temp = cards.get(index1);
            cards.set(index1, cards.get(index2));
            cards.set(index2, temp);
        }
    }

    public boolean hasCards() {
        return !cards.isEmpty();
    }

    public Level level() { return level; }
    public LivingEntity caster() { return caster; }
}
