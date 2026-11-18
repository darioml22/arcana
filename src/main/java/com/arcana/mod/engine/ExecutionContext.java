package com.arcana.mod.engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.arcana.mod.card.Card;
import com.arcana.mod.card.ModCard;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

// Holds all runtime state for one spell evaluation: the cards being read, the focus (instruction
// pointer) and reading direction, mana, the modifier stack and the game-side context (level, caster).
//
// Focus, direction and mana are meant to be persisted on the wand between casts (see
// WandState); the modifier stack and pending cost only live for one cast.
//
// The focus may temporarily sit outside [0, slotCount): that means the reading head ran
// off one end of the wand, which the engine treats as the end of the loop.
public class ExecutionContext {
    public static final int DEFAULT_EFFECT_CREDITS = 1;

    private final List<Card> slots; // null entries are empty slots
    private final Level level;
    private final LivingEntity caster;
    private final float maxMana;

    private int focus;
    private boolean reversed;
    private float mana;

    private final List<ModCard> modStack = new ArrayList<>();
    private float pendingManaCost = 0F;
    private int effectCredits = DEFAULT_EFFECT_CREDITS;
    private boolean focusOverridden = false;
    private final List<String> trace = new ArrayList<>();

    public ExecutionContext(List<Card> slots, Level level, LivingEntity caster,
                            float maxMana, float mana, int focus, boolean reversed) {
        this.slots = new ArrayList<>(slots);
        this.level = level;
        this.caster = caster;
        this.maxMana = maxMana;
        this.mana = mana;
        this.focus = focus;
        this.reversed = reversed;
    }

    // ---------- Cast lifecycle (called by the engine) ----------

    // Resets the per-cast state. A stale focus (e.g. the loadout shrank) restarts the loop.
    void beginCast() {
        modStack.clear();
        pendingManaCost = 0F;
        effectCredits = DEFAULT_EFFECT_CREDITS;
        focusOverridden = false;
        trace.clear();
        if (!isFocusInBounds()) {
            focus = 0;
            reversed = false;
        }
    }

    // End of the wand reached: discard modifiers and go back to slot 0, reading forwards.
    void endLoop() {
        modStack.clear();
        pendingManaCost = 0F;
        focus = 0;
        reversed = false;
    }

    // Wand failure: mana is reset to 0, focus to slot 0 and the modifier stack is emptied.
    void fail() {
        endLoop();
        mana = 0F;
    }

    // Moves one slot in the current reading direction.
    void advance() {
        focus += direction();
    }

    // Returns true if a logic card moved the focus itself this step (the engine must not advance).
    boolean consumeFocusOverride() {
        boolean overridden = focusOverridden;
        focusOverridden = false;
        return overridden;
    }

    // ---------- Focus / reading ----------

    public int getFocus() { return focus; }

    public boolean isReversed() { return reversed; }

    public int direction() { return reversed ? -1 : 1; }

    public int getSlotCount() { return slots.size(); }

    public List<Card> getSlots() { return Collections.unmodifiableList(slots); }

    public boolean isFocusInBounds() {
        return focus >= 0 && focus < slots.size();
    }

    public Card getCurrentCard() {
        return isFocusInBounds() ? slots.get(focus) : null;
    }

    public boolean hasAnyCard() {
        for (Card card : slots) {
            if (card != null) return true;
        }
        return false;
    }

    // Returns true if a non-empty slot exists from the current focus (inclusive) onwards.
    public boolean hasCardsAhead() {
        for (int i = focus; i >= 0 && i < slots.size(); i += direction()) {
            if (slots.get(i) != null) return true;
        }
        return false;
    }

    // Returns index of the next non-empty slot after the focus in reading direction, or -1.
    public int nextCardIndex() {
        for (int i = focus + direction(); i >= 0 && i < slots.size(); i += direction()) {
            if (slots.get(i) != null) return i;
        }
        return -1;
    }

    // Places the focus on the given slot; it will be the next one read. The index may be out of
    // bounds, which makes the engine end the loop.
    public void jumpTo(int index) {
        this.focus = index;
        this.focusOverridden = true;
    }

    // Skips the next (non-empty) card in reading direction. Does nothing if there is none.
    public void skipNextCard() {
        int next = nextCardIndex();
        if (next != -1) {
            trace("  -> skips slot " + next + " (" + slots.get(next).getCardName() + ")");
            jumpTo(next + direction());
        }
    }

    // Continues reading from slot 0, forwards.
    public void restart() {
        reversed = false;
        jumpTo(0);
    }

    // Inverts the reading direction.
    public boolean reverseExecution() {
        reversed = !reversed;
        return reversed;
    }

    // ---------- Modifier stack ----------

    public void pushModifier(ModCard card) {
        modStack.add(card);
    }

    // Removes and returns every stored modifier, in the order they were pushed.
    public List<ModCard> drainModifiers() {
        List<ModCard> drained = new ArrayList<>(modStack);
        modStack.clear();
        return drained;
    }

    public List<ModCard> getModifierStack() {
        return Collections.unmodifiableList(modStack);
    }

    // ---------- Mana ----------

    public float getMana() { return mana; }

    public float getMaxMana() { return maxMana; }

    // Mana accumulated by the cards read in the current spell block, not yet paid.
    public float getPendingManaCost() { return pendingManaCost; }

    void addPendingManaCost(float cost) {
        pendingManaCost += cost;
    }

    public boolean canAffordPending() {
        return pendingManaCost <= mana;
    }

    // Pays the pending cost and returns the amount paid.
    public float payPendingManaCost() {
        float paid = pendingManaCost;
        mana = Math.max(0F, mana - paid);
        pendingManaCost = 0F;
        return paid;
    }

    // Adds mana to the wand (e.g. an "Absorb mana" effect), capped at the maximum.
    public void addMana(float amount) {
        mana = Math.min(maxMana, mana + amount);
    }

    // ---------- Effect credits ----------

    // Number of effect cards that may still fire in this cast before it ends.
    public int getEffectCredits() { return effectCredits; }

    // Lets more effects fire in the same cast (e.g. a future "Double cast").
    public void addEffectCredits(int amount) {
        effectCredits += amount;
    }

    public void consumeEffectCredit() {
        effectCredits--;
    }

    // ---------- Debug trace ----------

    public void trace(String line) {
        trace.add(line);
    }

    public List<String> getTrace() {
        return Collections.unmodifiableList(trace);
    }

    // ---------- Game context ----------

    public Level level() { return level; }

    public LivingEntity caster() { return caster; }
}
