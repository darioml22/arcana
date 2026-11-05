public class MagicBlockRegistry {

    public enum Type {
        CAPACITY,
        DECORATION,
        RELIC
    }

    public record MagicBlockInfo(
        Type type,
        int capacityValue,
        String relicId
    ) {}

    private static final Map<Block, MagicBlockInfo> BLOCKS =
        new HashMap<>();

    public static void registerDecorBlock(Block block, int value) {
        BLOCKS.put(block, new MagicBlockInfo(
            Type.DECORATION,
            value,
            null
        ));
    }

    public static void registerRelic(Block block, String unlockId) {
        BLOCKS.put(block, new MagicBlockInfo(
            Type.RELIC,
            0,
            relicId
        ));
    }

    public static MagicBlockInfo get(Block block) {
        return BLOCKS.get(block);
    }
}