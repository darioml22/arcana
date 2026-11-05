public class TableBlockEntity extends BlockEntity {

    private static final int SCAN_RADIUS = 5;

    private static final int SCAN_INTERVAL = 20;

    private int arcaneCapacity = 0;

    private Map<Block, Integer> nearbyBlocks = new HashMap<>();

    private Set<String> unlockedFeatures = new HashSet<>();

    private int tickCounter = 0;


    public TableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGIC_TABLE.get(), pos, state);
    }

    public void refreshEnvironment() { //scan sorunding blocks and update capacity and unlocked features

        if (level == null || level.isClientSide()) {
            return;
        }

        int capacity = 0;

        Map<Block, Integer> countedBlocks = new HashMap<>();
        Set<String> unlocked = new HashSet<>();

        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {

                    if (x * x + y * y + z * z > SCAN_RADIUS * SCAN_RADIUS) {
                        continue;
                    }

                    BlockPos checkPos = worldPosition.offset(x, y, z);

                    Block block = level.getBlockState(checkPos).getBlock();

                    MagicBlockRegistry.MagicBlockInfo info =
                            MagicBlockRegistry.get(block);

                    if (info == null) {
                        continue;
                    }

                    switch (info.type()) {

                        case CAPACITY -> {

                            countedBlocks.merge(block, 1, Integer::sum);

                            capacity += info.capacityValue();
                        }

                        case UNLOCK -> {

                            unlocked.add(info.unlockId());
                        }
                    }
                }
            }
        }

        this.arcaneCapacity = capacity;
        this.nearbyBlocks = countedBlocks;
        this.unlockedFeatures = unlocked;

        setChanged();
    }


    public static void serverTick(
            Level level,
            BlockPos pos,
            BlockState state,
            TableBlockEntity blockEntity
    ) {

        blockEntity.tickCounter++;

        if (blockEntity.tickCounter >= SCAN_INTERVAL) {

            blockEntity.tickCounter = 0;

            blockEntity.refreshEnvironment();
        }
    }

    public int getArcaneCapacity() {
        return arcaneCapacity;
    }


    public int getBlockCount(Block block) {
        return nearbyBlocks.getOrDefault(block, 0);
    }


    public boolean hasFeature(String feature) {
        return unlockedFeatures.contains(feature);
    }


    public Set<String> getUnlockedFeatures() {
        return Collections.unmodifiableSet(unlockedFeatures);
    }


    public Map<Block, Integer> getNearbyBlocks() {
        return Collections.unmodifiableMap(nearbyBlocks);
    }
}