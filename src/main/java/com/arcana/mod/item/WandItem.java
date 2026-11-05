package com.arcana.mod.item;
import java.util.List;
import java.util.function.Consumer;

import com.arcana.mod.ModDataComponents;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class WandItem extends Item {

    public WandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        List<String> spells = stack.getOrDefault(ModDataComponents.SPELLS.get(), List.of());

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.sendSystemMessage(Component.literal("\u00a1Wand used!"), true);
        }

        player.getCooldowns().addCooldown(stack, 20);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        List<String> spells = stack.getOrDefault(ModDataComponents.SPELLS.get(), List.of());
        tooltip.accept(Component.literal("Spells (" + spells.size() + "):").withStyle(ChatFormatting.GRAY));
        for (String spell : spells) {
            tooltip.accept(Component.literal(" - " + spell).withStyle(ChatFormatting.DARK_PURPLE));
        }
    }
}