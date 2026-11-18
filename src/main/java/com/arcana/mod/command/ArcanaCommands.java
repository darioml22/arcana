package com.arcana.mod.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import com.arcana.mod.ModDataComponents;
import com.arcana.mod.card.Card;
import com.arcana.mod.item.WandItem;
import com.arcana.mod.registry.ModCards;
import com.arcana.mod.wand.SpellTrace;
import com.arcana.mod.wand.WandData;
import com.arcana.mod.wand.WandState;
import com.arcana.mod.wand.WandStats;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

// Debug commands for testing wands (operator only):
// /arcana wand [cards...]      give a wand with the given cards, e.g. "/arcana wand fire empty projectile"
// /arcana presets              give every preset wand
// /arcana cards                list every card with its short name and mana cost
// /arcana inspect              show the held wand's stats, mana, focus and slots
// /arcana reset                refill the held wand's mana, reset its focus and clear its cooldown
// /arcana stats <maxMana> <manaRegen> <castDelay> <rechargeTime>   change the held wand's stats
// /arcana trace                toggle a step-by-step engine trace in chat on every cast
public final class ArcanaCommands {

    private ArcanaCommands() {}

    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("arcana")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("wand")
                        .executes(ctx -> giveWand(ctx, ""))
                        .then(Commands.argument("cards", StringArgumentType.greedyString())
                                .suggests(ArcanaCommands::suggestCards)
                                .executes(ctx -> giveWand(ctx, StringArgumentType.getString(ctx, "cards")))))
                .then(Commands.literal("presets").executes(ArcanaCommands::givePresets))
                .then(Commands.literal("cards").executes(ArcanaCommands::listCards))
                .then(Commands.literal("inspect").executes(ArcanaCommands::inspect))
                .then(Commands.literal("reset").executes(ArcanaCommands::reset))
                .then(Commands.literal("trace").executes(ArcanaCommands::toggleTrace))
                .then(Commands.literal("stats")
                        .then(Commands.argument("maxMana", FloatArgumentType.floatArg(0F))
                        .then(Commands.argument("manaRegen", FloatArgumentType.floatArg(0F))
                        .then(Commands.argument("castDelay", IntegerArgumentType.integer(0))
                        .then(Commands.argument("rechargeTime", IntegerArgumentType.integer(0))
                                .executes(ArcanaCommands::setStats)))))));
    }

    // ---------- /arcana wand ----------

    private static int giveWand(CommandContext<CommandSourceStack> ctx, String input) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        List<String> ids = new ArrayList<>();
        for (String token : input.trim().split("\\s+")) {
            if (token.isEmpty()) continue;
            Optional<String> id = WandData.resolveCardToken(token);
            if (id.isEmpty()) {
                ctx.getSource().sendFailure(Component.literal("Unknown card: '" + token
                        + "'. Use /arcana cards to see the available ones."));
                return 0;
            }
            ids.add(id.get());
        }

        WandStats stats = WandStats.DEFAULT.withCapacity(Math.max(WandStats.DEFAULT.capacity(), ids.size()));
        give(player, WandData.createWand(ids, stats, null));
        ctx.getSource().sendSuccess(() -> Component.literal("Gave a wand with " + ids.size() + " slot(s)"), false);
        return 1;
    }

    // Suggests card names for the last word of the greedy "cards" argument.
    private static CompletableFuture<Suggestions> suggestCards(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining();
        int lastSpace = remaining.lastIndexOf(' ');
        SuggestionsBuilder wordBuilder = builder.createOffset(builder.getStart() + lastSpace + 1);
        String prefix = remaining.substring(lastSpace + 1).toLowerCase(Locale.ROOT);
        for (String token : WandData.cardTokens()) {
            if (token.startsWith(prefix)) wordBuilder.suggest(token);
        }
        return wordBuilder.buildFuture();
    }

    private static int givePresets(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        List<ItemStack> presets = WandData.presets();
        presets.forEach(wand -> give(player, wand.copy()));
        ctx.getSource().sendSuccess(() -> Component.literal("Gave " + presets.size() + " preset wands"), false);
        return presets.size();
    }

    private static int listCards(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        source.sendSuccess(() -> Component.literal("Available cards (use these names with /arcana wand):")
                .withStyle(ChatFormatting.GOLD), false);
        for (Card card : ModCards.all()) {
            source.sendSuccess(() -> Component.literal(" " + WandData.shortName(card)).withStyle(card.getCardType().color())
                    .append(Component.literal(" - " + card.getCardName() + " (" + Card.formatMana(card.getManaCost())
                            + " mana): " + card.getDescription()).withStyle(ChatFormatting.GRAY)), false);
        }
        source.sendSuccess(() -> Component.literal(" " + WandData.EMPTY_TOKEN).withStyle(ChatFormatting.DARK_GRAY)
                .append(Component.literal(" - empty slot").withStyle(ChatFormatting.GRAY)), false);
        return 1;
    }

    // ---------- Held wand commands ----------

    private static int inspect(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack wand = heldWand(player);
        if (wand == null) return noWand(ctx);

        WandStats stats = WandData.getStats(wand);
        WandState state = WandData.getState(wand);
        float mana = state.manaAt(player.level().getGameTime(), stats);
        CommandSourceStack source = ctx.getSource();

        source.sendSuccess(() -> Component.literal("Wand: ").withStyle(ChatFormatting.GOLD).append(wand.getHoverName()), false);
        source.sendSuccess(() -> Component.literal(String.format(Locale.ROOT,
                "Mana %.1f/%.1f (+%.1f/s) | cast delay %d t | recharge %d t | capacity %d",
                mana, stats.maxMana(), stats.manaRegen(), stats.castDelay(), stats.rechargeTime(), stats.capacity()))
                .withStyle(ChatFormatting.AQUA), false);
        source.sendSuccess(() -> Component.literal("Focus: slot " + (state.focus() + 1) + ", reading "
                + (state.reversed() ? "backwards" : "forwards")).withStyle(ChatFormatting.GRAY), false);
        for (Component line : WandItem.describeSlots(wand)) {
            source.sendSuccess(() -> line, false);
        }
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack wand = heldWand(player);
        if (wand == null) return noWand(ctx);

        wand.set(ModDataComponents.WAND_STATE.get(), WandState.DEFAULT);
        player.getCooldowns().removeCooldown(player.getCooldowns().getCooldownGroup(wand));
        ctx.getSource().sendSuccess(() -> Component.literal("Wand reset: full mana, focus on slot 1"), false);
        return 1;
    }

    private static int setStats(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        ItemStack wand = heldWand(player);
        if (wand == null) return noWand(ctx);

        WandStats old = WandData.getStats(wand);
        WandStats stats = new WandStats(old.capacity(),
                FloatArgumentType.getFloat(ctx, "maxMana"),
                FloatArgumentType.getFloat(ctx, "manaRegen"),
                IntegerArgumentType.getInteger(ctx, "castDelay"),
                IntegerArgumentType.getInteger(ctx, "rechargeTime"));
        wand.set(ModDataComponents.WAND_STATS.get(), stats);
        wand.set(ModDataComponents.WAND_STATE.get(), WandState.DEFAULT);
        ctx.getSource().sendSuccess(() -> Component.literal("Wand stats updated and mana refilled"), false);
        return 1;
    }

    private static int toggleTrace(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        boolean on = SpellTrace.toggle(player.getUUID());
        ctx.getSource().sendSuccess(() -> Component.literal("Spell trace " + (on ? "enabled" : "disabled")), false);
        return 1;
    }

    // ---------- Helpers ----------

    private static ItemStack heldWand(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof WandItem) return stack;
        }
        return null;
    }

    private static int noWand(CommandContext<CommandSourceStack> ctx) {
        ctx.getSource().sendFailure(Component.literal("You must hold a wand"));
        return 0;
    }

    private static void give(ServerPlayer player, ItemStack stack) {
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
