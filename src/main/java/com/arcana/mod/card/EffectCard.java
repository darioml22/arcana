package com.arcana.mod.card;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.arcana.mod.engine.ExecutionContext;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

public class EffectCard extends Card {

    @FunctionalInterface
    public interface EffectAction {
        void fire(ExecutionContext ctx, SpellModifier mods);
    }

    private final String effectDescription;
    private final Set<SpellStat> supportedStats;
    private final EffectAction action;

    public EffectCard(Item.Properties properties, String cardName, String cardDescription,
                      int textureIndex, float manaCost, String effectDescription,
                      Set<SpellStat> supportedStats, EffectAction action) {
        super(properties, Type.EFFECT, cardName, cardDescription, textureIndex, manaCost);
        this.effectDescription = effectDescription;
        this.supportedStats = Set.copyOf(supportedStats);
        this.action = action;
    }

    // Consumes every modifier in the stack, pays the accumulated mana of the spell block
    // and fires the effect.
    @Override
    public boolean execute(ExecutionContext context) {
        if (context.getEffectCredits() <= 0) {
            return false;
        }

        List<ModCard> modifiers = context.drainModifiers();
        SpellModifier mods = new SpellModifier();
        for (ModCard mod : modifiers) {
            for (StatChange change : mod.getChanges()) {
                if (supports(change.stat())) {
                    mods.add(change);
                }
            }
        }

        float paid = context.payPendingManaCost();
        context.consumeEffectCredit();
        context.trace("  -> " + getCardName() + " fires"
                + (modifiers.isEmpty() ? "" : " with " + modifiers.stream().map(Card::getCardName).collect(Collectors.joining(", ")))
                + ", paying " + formatMana(paid) + " mana");

        action.fire(context, mods);
        return true;
    }

    public boolean supports(SpellStat stat) {
        return supportedStats.contains(stat);
    }

    public String getEffectDescription() { return effectDescription; }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.literal(effectDescription).withStyle(ChatFormatting.DARK_GRAY));
    }
}
