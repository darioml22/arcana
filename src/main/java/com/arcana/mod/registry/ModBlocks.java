package com.arcana.mod.registry;

import com.arcana.mod.Arcanamod;
import com.arcana.mod.block.TableBlock;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(Arcanamod.MODID);

    public static final DeferredBlock<TableBlock> ARCANA_TABLE =
            BLOCKS.registerBlock(
                    "arcana_table",
                    TableBlock::new,
                    properties -> properties.mapColor(MapColor.STONE).strength(2.0F)
            );

    private ModBlocks() {}
}