package com.arcana.mod.registry;

import java.util.List;
import java.util.Set;

import com.arcana.mod.Arcanamod;
import com.arcana.mod.card.EffectCard;
import com.arcana.mod.card.ModCard;
import com.arcana.mod.card.SpellStat;
import com.arcana.mod.card.StatChange;
import com.arcana.mod.entity.Projectile;
import com.arcana.mod.effectstats.ProjectileStats;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ModCards {
    public static final DeferredRegister.Items CARDS = DeferredRegister.createItems(Arcanamod.MODID);

    // ---------- EFFECT ----------

    public static final DeferredItem<EffectCard> PROJECTILE = CARDS.registerItem(
        "projectile_card",
        props -> new EffectCard(props, "Projectile", "Launches a projectile", 0,
                "Deals damage on impact",
                Set.of(SpellStat.DAMAGE, SpellStat.SPEED, SpellStat.FIRE_SECONDS, SpellStat.GRAVITY),
                (ctx, mods) -> Projectile.launch(ctx, ProjectileStats.from(mods))),
        p -> p.stacksTo(16));

    public static final DeferredItem<EffectCard> EXPLOSION = CARDS.registerItem(
        "explosion_card",
        props -> new EffectCard(props, "Explosion", "Creates an explosion", 1,
                "Explodes where the caster is looking",
                Set.of(SpellStat.RADIUS, SpellStat.RANGE, SpellStat.FIRE_SECONDS),
                (ctx, mods) -> {
                    float radius = mods.resolve(SpellStat.RADIUS, 2.0F);
                    float range = mods.resolve(SpellStat.RANGE, 8.0F);
                    float fire = mods.resolve(SpellStat.FIRE_SECONDS, 0F);

                    Vec3 pos = ctx.caster().pick(range, 1.0F, false).getLocation();
                    ctx.level().explode(ctx.caster(), pos.x, pos.y, pos.z,
                            radius, fire > 0F, Level.ExplosionInteraction.NONE);
                }),
        p -> p.stacksTo(16));

    // ---------- MOD ----------

    public static final DeferredItem<ModCard> FIRE = CARDS.registerItem(
            "fire_card",
            props -> new ModCard(props, "Fire", "Sets the target on fire", 2,
                    List.of(StatChange.add(SpellStat.FIRE_SECONDS, 5F))),
            p -> p.stacksTo(16));

    public static final DeferredItem<ModCard> HEAVY = CARDS.registerItem(
            "heavy_card",
            props -> new ModCard(props, "Heavy", "Slower but harder hitting", 3,
                    List.of(StatChange.multiply(SpellStat.SPEED, 0.5F),
                            StatChange.multiply(SpellStat.DAMAGE, 2.0F))),
            p -> p.stacksTo(16));

    private ModCards() {}
}