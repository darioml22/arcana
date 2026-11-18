package com.arcana.mod.wand;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

// Base stats of a wand (see ADD 2.2).
//
// capacity: number of card slots
// maxMana: mana capacity
// manaRegen: mana regenerated per second
// castDelay: cooldown in ticks between spell blocks ("casting speed")
// rechargeTime: cooldown in ticks once the wand finishes its loop ("loop speed"), also used on failure
public record WandStats(int capacity, float maxMana, float manaRegen, int castDelay, int rechargeTime) {

    public static final WandStats DEFAULT = new WandStats(8, 100F, 20F, 10, 30);

    public static final Codec<WandStats> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.INT.fieldOf("capacity").forGetter(WandStats::capacity),
            Codec.FLOAT.fieldOf("max_mana").forGetter(WandStats::maxMana),
            Codec.FLOAT.fieldOf("mana_regen").forGetter(WandStats::manaRegen),
            Codec.INT.fieldOf("cast_delay").forGetter(WandStats::castDelay),
            Codec.INT.fieldOf("recharge_time").forGetter(WandStats::rechargeTime)
    ).apply(i, WandStats::new));

    public static final StreamCodec<ByteBuf, WandStats> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, WandStats::capacity,
            ByteBufCodecs.FLOAT, WandStats::maxMana,
            ByteBufCodecs.FLOAT, WandStats::manaRegen,
            ByteBufCodecs.VAR_INT, WandStats::castDelay,
            ByteBufCodecs.VAR_INT, WandStats::rechargeTime,
            WandStats::new);

    public WandStats withCapacity(int newCapacity) {
        return new WandStats(newCapacity, maxMana, manaRegen, castDelay, rechargeTime);
    }
}
