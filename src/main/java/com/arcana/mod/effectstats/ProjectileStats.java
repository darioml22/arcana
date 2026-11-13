package com.arcana.mod.effectstats;

import com.arcana.mod.card.SpellModifier;
import com.arcana.mod.card.SpellStat;

public record ProjectileStats(float damage, float speed, float fireSeconds, float gravity) {

    public static final float BASE_DAMAGE = 4.0F;
    public static final float BASE_SPEED = 1.5F;
    public static final float BASE_FIRE_SECONDS = 0F;
    public static final float BASE_GRAVITY = 0.03F;

    public static ProjectileStats from(SpellModifier mods) {
        return new ProjectileStats(
                Math.max(0F, mods.resolve(SpellStat.DAMAGE, BASE_DAMAGE)),
                Math.max(0F, mods.resolve(SpellStat.SPEED, BASE_SPEED)),
                Math.max(0F, mods.resolve(SpellStat.FIRE_SECONDS, BASE_FIRE_SECONDS)),
                Math.max(0F, mods.resolve(SpellStat.GRAVITY, BASE_GRAVITY))
        );
    }
}