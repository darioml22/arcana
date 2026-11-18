package com.arcana.mod.engine;

import com.arcana.mod.Arcanamod;
import com.arcana.mod.card.Card;

// Interpreter for card sequences. One call to cast() corresponds to one use of the wand:
// cards are read from the current focus, one at a time, until an effect card fires (a full
// spell block), the end of the wand is reached, or the wand fails.
//
// - Empty slot: ignored.
// - Modifier card: stored in the modifier stack.
// - Effect card: fires with every stored modifier, paying the accumulated mana and
//   clearing the stack.
// - Logic card: applies its own action (skip, jump, reverse, conditions...).
//
// Every card read adds its cost to the pending mana cost; if that ever exceeds the wand's mana
// the wand fails. Reaching the end discards the modifiers and moves the focus back to slot 0.
public final class SpellEngine {
    // Max cards read in a single cast, to stop infinite loops (e.g. a lone Restart card).
    public static final int EXECUTION_LIMIT = 1000;

    private SpellEngine() {}

    public static CastResult cast(ExecutionContext ctx) {
        if (!ctx.hasAnyCard()) {
            return CastResult.EMPTY;
        }

        ctx.beginCast();

        for (int steps = 0; ; steps++) {
            if (steps >= EXECUTION_LIMIT) {
                ctx.trace("Execution limit (" + EXECUTION_LIMIT + ") reached: wand failure");
                ctx.fail();
                return CastResult.FAILURE_EXECUTION_LIMIT;
            }

            if (!ctx.isFocusInBounds()) {
                ctx.trace("End of wand reached, " + ctx.getModifierStack().size() + " modifier(s) discarded");
                ctx.endLoop();
                return CastResult.END_OF_WAND;
            }

            int slot = ctx.getFocus();
            Card card = ctx.getCurrentCard();
            if (card == null) {
                ctx.advance();
                continue;
            }

            ctx.addPendingManaCost(card.getManaCost());
            ctx.trace("[" + slot + "] " + card.getCardName() + " (" + card.getCardType().name().toLowerCase()
                    + ", pending " + Card.formatMana(ctx.getPendingManaCost()) + "/" + Card.formatMana(ctx.getMana()) + ")");

            if (!ctx.canAffordPending()) {
                ctx.trace("Not enough mana: wand failure");
                ctx.fail();
                return CastResult.FAILURE_NO_MANA;
            }

            boolean ok;
            try {
                ok = card.execute(ctx);
            } catch (RuntimeException e) {
                Arcanamod.LOGGER.error("Card {} threw while executing", card.getCardName(), e);
                ok = false;
            }
            if (!ok) {
                ctx.trace(card.getCardName() + " failed: wand failure");
                ctx.fail();
                return CastResult.FAILURE_CARD;
            }

            if (!ctx.consumeFocusOverride()) {
                ctx.advance();
            }

            if (ctx.getEffectCredits() <= 0) {
                if (!ctx.hasCardsAhead()) {
                    ctx.trace("Last card reached, wand recharges");
                    ctx.endLoop();
                    return CastResult.CAST_END_OF_WAND;
                }
                return CastResult.CAST;
            }
        }
    }
}
