package com.arcana.mod.entity;

import com.arcana.mod.engine.ExecutionContext;
import com.arcana.mod.card.SpellModifier;
import com.arcana.mod.effectstats.ProjectileStats;
import com.arcana.mod.registry.ModEntities;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class Projectile extends ThrowableItemProjectile {
    private ProjectileStats stats = ProjectileStats.from(new SpellModifier());

    public Projectile(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.FIRE_CHARGE;   // visual placeholder
    }

    @Override
    protected double getDefaultGravity() {
        return stats.gravity();
    }

    public static void launch(ExecutionContext ctx, ProjectileStats stats) {
        Level level = ctx.level();
        LivingEntity caster = ctx.caster();

        Projectile projectile = new Projectile(ModEntities.PROJECTILE.get(), level);
        projectile.stats = stats;
        projectile.setOwner(caster);
        projectile.setPos(caster.getX(), caster.getEyeY() - 0.1, caster.getZ());
        projectile.shootFromRotation(caster, caster.getXRot(), caster.getYRot(), 0.0F, stats.speed(), 1.0F);

        level.addFreshEntity(projectile);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide()) return;

        Entity target = result.getEntity();
        target.hurt(this.damageSources().thrown(this, this.getOwner()), stats.damage());
        if (stats.fireSeconds() > 0F) {
            target.igniteForSeconds(stats.fireSeconds());
        }
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide()) {
            this.discard();
        }
    }
}