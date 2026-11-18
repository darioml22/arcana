package com.arcana.mod.wand;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jspecify.annotations.Nullable;

import com.arcana.mod.Arcanamod;
import com.arcana.mod.ModDataComponents;
import com.arcana.mod.card.Card;
import com.arcana.mod.registry.ModCards;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;

// Helpers to read, build and describe wand item stacks.
public final class WandData {
    // Stored in the loadout for an empty slot.
    public static final String EMPTY_SLOT = "";
    // What players type in commands for an empty slot.
    public static final String EMPTY_TOKEN = "empty";
    private static final String CARD_SUFFIX = "_card";

    private WandData() {}

    // ---------- Reading ----------

    public static List<String> getCardIds(ItemStack wand) {
        return wand.getOrDefault(ModDataComponents.SPELLS.get(), List.of());
    }

    // The wand's cards in slot order; empty slots and unknown ids are null.
    public static List<Card> getLoadout(ItemStack wand) {
        List<Card> cards = new ArrayList<>();
        for (String id : getCardIds(wand)) {
            cards.add(cardFromId(id));
        }
        return cards;
    }

    public static @Nullable Card cardFromId(String id) {
        if (id == null || id.isBlank()) return null;
        Identifier key = Identifier.tryParse(id);
        if (key == null) return null;
        return BuiltInRegistries.ITEM.getOptional(key).orElse(null) instanceof Card card ? card : null;
    }

    public static WandStats getStats(ItemStack wand) {
        return wand.getOrDefault(ModDataComponents.WAND_STATS.get(), WandStats.DEFAULT);
    }

    public static WandState getState(ItemStack wand) {
        return wand.getOrDefault(ModDataComponents.WAND_STATE.get(), WandState.DEFAULT);
    }

    // ---------- Building ----------

    public static ItemStack createWand(List<String> cardIds, WandStats stats, @Nullable String name) {
        ItemStack wand = new ItemStack(Arcanamod.WAND.get());
        wand.set(ModDataComponents.SPELLS.get(), List.copyOf(cardIds));
        wand.set(ModDataComponents.WAND_STATS.get(), stats);
        wand.set(ModDataComponents.WAND_STATE.get(), WandState.DEFAULT);
        if (name != null) {
            wand.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        }
        return wand;
    }

    // Resolves what a player typed into a stored card id. Accepts empty, a short name
    // (fire), an item path (fire_card) or a full id (arcanamod:fire_card).
    public static Optional<String> resolveCardToken(String token) {
        if (token.equalsIgnoreCase(EMPTY_TOKEN)) return Optional.of(EMPTY_SLOT);

        List<String> candidates = token.contains(":")
                ? List.of(token)
                : List.of(Arcanamod.MODID + ":" + token, Arcanamod.MODID + ":" + token + CARD_SUFFIX);
        for (String candidate : candidates) {
            Card card = cardFromId(candidate);
            if (card != null) return Optional.of(idOf(card));
        }
        return Optional.empty();
    }

    // Short names of every registered card, plus empty, for command suggestions.
    public static List<String> cardTokens() {
        List<String> tokens = new ArrayList<>();
        for (DeferredHolder<Item, ? extends Item> holder : ModCards.CARDS.getEntries()) {
            if (holder.get() instanceof Card card) tokens.add(shortName(card));
        }
        tokens.add(EMPTY_TOKEN);
        return tokens;
    }

    public static String idOf(Item card) {
        return BuiltInRegistries.ITEM.getKey(card).toString();
    }

    public static String shortName(Card card) {
        String path = BuiltInRegistries.ITEM.getKey(card).getPath();
        return path.endsWith(CARD_SUFFIX) ? path.substring(0, path.length() - CARD_SUFFIX.length()) : path;
    }

    // ---------- Debug presets (creative tab) ----------

    public static List<ItemStack> presets() {
        String fire = idOf(ModCards.FIRE.get());
        String heavy = idOf(ModCards.HEAVY.get());
        String projectile = idOf(ModCards.PROJECTILE.get());
        String explosion = idOf(ModCards.EXPLOSION.get());
        String skip = idOf(ModCards.SKIP.get());
        String restart = idOf(ModCards.RESTART.get());
        String backwards = idOf(ModCards.BACKWARDS.get());
        String highCost = idOf(ModCards.CAST_IF_HIGH_COST.get());
        String onFire = idOf(ModCards.CAST_IF_ON_FIRE.get());

        return List.of(
                // "Basic example" from the Spell Using Examples document
                createWand(List.of(fire, heavy, projectile), WandStats.DEFAULT, "Debug Wand: Basic"),
                // "Empty slot example"
                createWand(List.of(fire, EMPTY_SLOT, projectile), WandStats.DEFAULT, "Debug Wand: Empty Slot"),
                // "Advanced example 1", with Heavy instead of Accelerating shot. Max mana is tuned so
                // Cast if high cost is false on the first pass (15 < 30) and true after Restart (35 >= 30).
                createWand(List.of(heavy, highCost, skip, restart, fire, projectile),
                        new WandStats(8, 60F, 20F, 10, 30), "Debug Wand: Logic"),
                // Skip / Backwards: casts Projectile, then Projectile again reading backwards, then Explosion
                createWand(List.of(skip, explosion, heavy, projectile, backwards),
                        WandStats.DEFAULT, "Debug Wand: Backwards"),
                // Explosion only while burning, a plain projectile otherwise
                createWand(List.of(onFire, explosion, projectile), WandStats.DEFAULT, "Debug Wand: On Fire"),
                // Infinite loop: hits the execution limit (or runs out of mana) and fails
                createWand(List.of(heavy, restart), WandStats.DEFAULT, "Debug Wand: Failure")
        );
    }
}
