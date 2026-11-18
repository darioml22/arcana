package com.arcana.mod;

import java.util.List;

import com.arcana.mod.wand.WandState;
import com.arcana.mod.wand.WandStats;
import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDataComponents {

    public static final DeferredRegister.DataComponents COMPONENTS =
        DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, Arcanamod.MODID);

    // Ordered card loadout of a wand: card item ids, with "" marking an empty slot.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<String>>> SPELLS =
        COMPONENTS.registerComponentType("spells", builder -> builder
            .persistent(Codec.STRING.listOf())
            .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list())));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WandStats>> WAND_STATS =
        COMPONENTS.registerComponentType("wand_stats", builder -> builder
            .persistent(WandStats.CODEC)
            .networkSynchronized(WandStats.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WandState>> WAND_STATE =
        COMPONENTS.registerComponentType("wand_state", builder -> builder
            .persistent(WandState.CODEC)
            .networkSynchronized(WandState.STREAM_CODEC));
}
