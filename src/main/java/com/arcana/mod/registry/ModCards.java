package com.arcana.mod.registry;

import java.util.List;
import java.util.Set;

import com.arcana.mod.Arcanamod;
import com.arcana.mod.card.Card;
import com.arcana.mod.card.EffectCard;
import com.arcana.mod.card.LogicCard;
import com.arcana.mod.card.ModCard;
import com.arcana.mod.card.SpellStat;
import com.arcana.mod.card.StatChange;
import com.arcana.mod.engine.ExecutionContext;
import com.arcana.mod.entity.Projectile;
import com.arcana.mod.effectstats.ProjectileStats;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.level.Level;

public final class ModCards {
    public static final DeferredRegister.Items CARDS = DeferredRegister.createItems(Arcanamod.MODID);

    // ---------- EFFECT ----------

    public static final DeferredItem<EffectCard> PROJECTILE = CARDS.registerItem(
        "projectile_card",
        props -> new EffectCard(props, "Projectile", "Launches a projectile", 0, 5F,
                "Deals damage on impact",
                Set.of(SpellStat.DAMAGE, SpellStat.SPEED, SpellStat.FIRE_SECONDS, SpellStat.GRAVITY),
                (ctx, mods) -> Projectile.launch(ctx, ProjectileStats.from(mods))),
        p -> p.stacksTo(16));

    public static final DeferredItem<EffectCard> EXPLOSION = CARDS.registerItem(
        "explosion_card",
        props -> new EffectCard(props, "Explosion", "Creates an explosion", 1, 25F,
                "Explodes at the caster's position, also hitting the caster",
                Set.of(SpellStat.RADIUS, SpellStat.FIRE_SECONDS),
                (ctx, mods) -> {
                    float radius = mods.resolve(SpellStat.RADIUS, 2.0F);
                    float fire = mods.resolve(SpellStat.FIRE_SECONDS, 0F);

                    ctx.level().explode(ctx.caster(), ctx.caster().getX(), ctx.caster().getY(), ctx.caster().getZ(),
                            radius, fire > 0F, Level.ExplosionInteraction.NONE);
                }),
        p -> p.stacksTo(16));

    // ---------- MOD ----------

    public static final DeferredItem<ModCard> FIRE = CARDS.registerItem(
            "fire_card",
            props -> new ModCard(props, "Fire", "Sets the target on fire", 2, 10F,
                    List.of(StatChange.add(SpellStat.FIRE_SECONDS, 5F))),
            p -> p.stacksTo(16));

    public static final DeferredItem<ModCard> HEAVY = CARDS.registerItem(
            "heavy_card",
            props -> new ModCard(props, "Heavy", "Slower but harder hitting", 3, 15F,
                    List.of(StatChange.multiply(SpellStat.SPEED, 0.5F),
                            StatChange.multiply(SpellStat.DAMAGE, 2.0F))),
            p -> p.stacksTo(16));

    // ---------- LOGIC ----------

    public static final DeferredItem<LogicCard> SKIP = CARDS.registerItem(
            "skip_card",
            props -> new LogicCard(props, "Skip", "The wand skips the next card", 4, 0F,
                    ctx -> {
                        ctx.skipNextCard();
                        return true;
                    }),
            p -> p.stacksTo(16));

    public static final DeferredItem<LogicCard> RESTART = CARDS.registerItem(
            "restart_card",
            props -> new LogicCard(props, "Restart", "The wand continues reading from the first slot", 5, 5F,
                    ctx -> {
                        ctx.trace("  -> focus back to slot 0");
                        ctx.restart();
                        return true;
                    }),
            p -> p.stacksTo(16));

    public static final DeferredItem<LogicCard> BACKWARDS = CARDS.registerItem(
            "backwards_card",
            props -> new LogicCard(props, "Backwards",
                    "The wand continues reading in the opposite direction, ending the loop at the other side", 6, 5F,
                    ctx -> {
                        boolean reversed = ctx.reverseExecution();
                        ctx.trace("  -> now reading " + (reversed ? "backwards" : "forwards"));
                        return true;
                    }),
            p -> p.stacksTo(16));

    public static final DeferredItem<LogicCard> CAST_IF_HIGH_COST = CARDS.registerItem(
            "cast_if_high_cost_card",
            props -> new LogicCard(props, "Cast if high cost",
                    "The next card is only cast if the current mana cost is at least 50% of the wand's total mana", 7, 0F,
                    ctx -> skipUnless(ctx, ctx.getPendingManaCost() >= ctx.getMaxMana() * 0.5F)),
            p -> p.stacksTo(16));

    public static final DeferredItem<LogicCard> CAST_IF_ON_FIRE = CARDS.registerItem(
            "cast_if_on_fire_card",
            props -> new LogicCard(props, "Cast if on fire",
                    "The next card is only cast if the caster is on fire", 8, 0F,
                    ctx -> skipUnless(ctx, ctx.caster() != null && ctx.caster().isOnFire())),
            p -> p.stacksTo(16));

    // Shared behaviour of the "Cast if ..." cards: skip the next card when the condition is false.
    private static boolean skipUnless(ExecutionContext ctx, boolean condition) {
        ctx.trace("  -> condition is " + condition);
        if (!condition) {
            ctx.skipNextCard();
        }
        return true;
    }

    // Every registered card, in registration order.
    public static List<Card> all() {
        return CARDS.getEntries().stream()
                .map(h -> h.get())
                .filter(i -> i instanceof Card)
                .map(i -> (Card) i)
                .toList();
    }

    private ModCards() {}
}
