package com.arcana.mod.wand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

// Execution state persisted on the wand between casts (ADD 2.3): where the focus is, the reading
// direction and the mana. Mana regenerates lazily: it is stored together with the game tick it was
// last updated, and manaAt() computes the current value.
//
// mana: stored mana, or FULL for a wand that has never been used
// lastUpdateTick: game time at which mana was stored
public record WandState(int focus, boolean reversed, float mana, long lastUpdateTick) {

    public static final float FULL = -1F;
    public static final WandState DEFAULT = new WandState(0, false, FULL, 0L);

    public static final Codec<WandState> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("focus").forGetter(WandState::focus),
            Codec.BOOL.fieldOf("reversed").forGetter(WandState::reversed),
            Codec.FLOAT.fieldOf("mana").forGetter(WandState::mana),
            Codec.LONG.fieldOf("last_update_tick").forGetter(WandState::lastUpdateTick)
    ).apply(i, WandState::new));

    public static final StreamCodec<ByteBuf, WandState> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WandState::focus,
            ByteBufCodecs.BOOL, WandState::reversed,
            ByteBufCodecs.FLOAT, WandState::mana,
            ByteBufCodecs.VAR_LONG, WandState::lastUpdateTick,
            WandState::new);

    // Current mana, including what regenerated since the last update.
    public float manaAt(long gameTime, WandStats stats) {
        if (mana < 0F) {
            return stats.maxMana();
        }
        long elapsedTicks = Math.max(0L, gameTime - lastUpdateTick);
        return Math.min(stats.maxMana(), mana + stats.manaRegen() * elapsedTicks / 20F);
    }
}
