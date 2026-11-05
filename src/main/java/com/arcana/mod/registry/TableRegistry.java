package com.arcana.mod.registry;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.world.level.block.Block;

public final class TableRegistry {
    public enum Type {
        DECORATION,
        RELIC
    }

    public record TableInfo(Type type, int capacityValue, String relicId) {}

    private static final Map<Block, TableInfo> BLOCKS = new HashMap<>();

    public static void registerDecorBlock(Block block, int value) {
        BLOCKS.put(block, new TableInfo(Type.DECORATION, value, null));
    }

    public static void registerRelic(Block block, String relicId) {
        BLOCKS.put(block, new TableInfo(Type.RELIC, 0, relicId));
    }

    public static TableInfo get(Block block) {
        return BLOCKS.get(block);
    }

    private TableRegistry() {}
}
