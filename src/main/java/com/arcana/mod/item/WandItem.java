package com.arcana.mod.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.arcana.mod.ModDataComponents;
import com.arcana.mod.card.Card;
import com.arcana.mod.engine.CastResult;
import com.arcana.mod.engine.ExecutionContext;
import com.arcana.mod.engine.SpellEngine;
import com.arcana.mod.wand.SpellTrace;
import com.arcana.mod.wand.WandData;
import com.arcana.mod.wand.WandState;
import com.arcana.mod.wand.WandStats;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public class WandItem extends Item {
    private static final int EMPTY_WAND_COOLDOWN = 10;

    public WandItem(Properties properties) {
        super(properties);
    }

    // One right click = one SpellEngine.cast(): cards are read from the persisted focus until a
    // spell block fires, the wand reaches its end, or it fails. The resulting focus/mana are saved back.
    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }

        WandStats stats = WandData.getStats(stack);
        WandState state = WandData.getState(stack);
        long now = level.getGameTime();

        ExecutionContext ctx = new ExecutionContext(
                WandData.getLoadout(stack), level, player,
                stats.maxMana(), state.manaAt(now, stats), state.focus(), state.reversed());

        CastResult result = SpellEngine.cast(ctx);

        stack.set(ModDataComponents.WAND_STATE.get(),
                new WandState(ctx.getFocus(), ctx.isReversed(), ctx.getMana(), now));

        int cooldown = switch (result) {
            case CAST -> stats.castDelay();
            case EMPTY -> EMPTY_WAND_COOLDOWN;
            default -> Math.max(stats.castDelay(), stats.rechargeTime());
        };
        if (cooldown > 0) {
            player.getCooldowns().addCooldown(stack, cooldown);
        }

        if (result.isFailure()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.6F, 1.4F);
        }

        serverPlayer.sendSystemMessage(statusLine(result, ctx, stats), true);
        if (SpellTrace.isTracing(player.getUUID())) {
            serverPlayer.sendSystemMessage(Component.literal("--- Cast: " + result + " ---").withStyle(ChatFormatting.GOLD));
            for (String line : ctx.getTrace()) {
                serverPlayer.sendSystemMessage(Component.literal(line).withStyle(ChatFormatting.GRAY));
            }
        }

        return InteractionResult.SUCCESS;
    }

    private static Component statusLine(CastResult result, ExecutionContext ctx, WandStats stats) {
        MutableComponent prefix = switch (result) {
            case EMPTY -> Component.literal("This wand has no cards").withStyle(ChatFormatting.GRAY);
            case FAILURE_NO_MANA -> Component.literal("Wand failure: not enough mana").withStyle(ChatFormatting.RED);
            case FAILURE_EXECUTION_LIMIT -> Component.literal("Wand failure: endless loop").withStyle(ChatFormatting.RED);
            case FAILURE_CARD -> Component.literal("Wand failure").withStyle(ChatFormatting.RED);
            case END_OF_WAND -> Component.literal("Recharging").withStyle(ChatFormatting.YELLOW);
            case CAST_END_OF_WAND -> Component.literal("Cast, recharging").withStyle(ChatFormatting.LIGHT_PURPLE);
            case CAST -> Component.literal("Cast").withStyle(ChatFormatting.LIGHT_PURPLE);
        };
        return prefix.append(Component.literal("  Mana " + Card.formatMana((float) Math.floor(ctx.getMana()))
                + "/" + Card.formatMana(stats.maxMana())).withStyle(ChatFormatting.AQUA));
    }

    // Changing the wand state on every cast must not replay the "re-equip" animation.
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        WandStats stats = WandData.getStats(stack);
        WandState state = WandData.getState(stack);
        Level level = context.level();
        // Without a level regeneration can't be computed, so the stored value is shown
        float mana = state.manaAt(level != null ? level.getGameTime() : state.lastUpdateTick(), stats);

        tooltip.accept(Component.literal("Mana: " + Card.formatMana((float) Math.floor(mana)) + "/" + Card.formatMana(stats.maxMana())
                + " (+" + Card.formatMana(stats.manaRegen()) + "/s)").withStyle(ChatFormatting.AQUA));
        tooltip.accept(Component.literal("Cast delay: " + seconds(stats.castDelay()) + "  Recharge: " + seconds(stats.rechargeTime()))
                .withStyle(ChatFormatting.GRAY));

        List<String> ids = WandData.getCardIds(stack);
        tooltip.accept(Component.literal("Cards (" + ids.stream().filter(s -> !s.isBlank()).count() + "/" + stats.capacity() + "):")
                .withStyle(ChatFormatting.GRAY));
        for (Component line : describeSlots(stack)) {
            tooltip.accept(line);
        }
    }

    // One line per slot (padded to capacity), marking the focused slot and the reading direction.
    public static List<Component> describeSlots(ItemStack stack) {
        WandStats stats = WandData.getStats(stack);
        WandState state = WandData.getState(stack);
        List<String> ids = WandData.getCardIds(stack);
        int slots = Math.max(stats.capacity(), ids.size());

        List<Component> lines = new ArrayList<>();
        for (int i = 0; i < slots; i++) {
            String id = i < ids.size() ? ids.get(i) : WandData.EMPTY_SLOT;
            Card card = WandData.cardFromId(id);
            String marker = i == state.focus() ? (state.reversed() ? " < " : " > ") : "   ";

            MutableComponent line = Component.literal(marker + (i + 1) + ". ").withStyle(ChatFormatting.DARK_PURPLE);
            if (card != null) {
                line.append(Component.literal(card.getCardName()).withStyle(card.getCardType().color()));
            } else if (id.isBlank()) {
                line.append(Component.literal("[empty]").withStyle(ChatFormatting.DARK_GRAY));
            } else {
                line.append(Component.literal("[unknown: " + id + "]").withStyle(ChatFormatting.DARK_RED));
            }
            lines.add(line);
        }
        return lines;
    }

    private static String seconds(int ticks) {
        return String.format("%.2fs", ticks / 20F);
    }
}
