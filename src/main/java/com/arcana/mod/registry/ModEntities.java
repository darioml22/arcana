package com.arcana.mod.registry;

import java.util.function.Supplier;

import com.arcana.mod.Arcanamod;
import com.arcana.mod.entity.Projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister.Entities ENTITY_TYPES =
            DeferredRegister.createEntities(Arcanamod.MODID);

    public static final Supplier<EntityType<Projectile>> PROJECTILE =
            ENTITY_TYPES.registerEntityType(
                    "projectile",
                    Projectile::new,
                    MobCategory.MISC,
                    builder -> builder
                            .sized(0.25F, 0.25F)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .noSave()
            );

    private ModEntities() {}
}