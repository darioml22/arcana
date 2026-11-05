package com.arcana.mod.registry;

import com.arcana.mod.Arcanamod;
import com.arcana.mod.block.TableBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, Arcanamod.MODID);

    public static final Supplier<BlockEntityType<TableBlockEntity>> ARCANA_TABLE =
            BLOCK_ENTITY_TYPES.register(
                    "arcana_table",
                    () -> new BlockEntityType<>(
                            TableBlockEntity::new,
                            ModBlocks.ARCANA_TABLE.get()
                    )
            );

    private ModBlockEntities() {}
}
