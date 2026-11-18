package com.arcana.mod.card;

import java.util.function.Consumer;

import com.arcana.mod.engine.ExecutionContext;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

// Base class for every card. A card is both an item (its in-world representation) and an
// instruction the SpellEngine can execute.
public abstract class Card extends Item {
    public enum Type {
        EFFECT(ChatFormatting.AQUA),
        LOGIC(ChatFormatting.RED),
        MOD(ChatFormatting.GREEN);

        private final ChatFormatting color;

        Type(ChatFormatting color) { this.color = color; }

        public ChatFormatting color() { return color; }
    }

    private final Type cardType;
    private final String cardName;
    private final String cardDescription;
    private final int textureIndex;
    private final float manaCost;

    public Card(
            Item.Properties properties,
            Type cardType,
            String cardName,
            String cardDescription,
            int textureIndex,
            float manaCost
    ) {
        super(properties);
        this.cardType = cardType;
        this.cardName = cardName;
        this.cardDescription = cardDescription;
        this.textureIndex = textureIndex;
        this.manaCost = manaCost;
    }

    // Runs this card against the given context. The engine has already added this card's
    // mana cost to the context's pending cost (and checked it can be paid) before calling this.
    //
    // Returns false if the wand should fail, true otherwise
    public abstract boolean execute(ExecutionContext context);

    public Type getCardType() { return cardType; }
    public String getCardName() { return cardName; }
    public String getDescription() { return cardDescription; }
    public int getTextureIndex() { return textureIndex; }
    public float getManaCost() { return manaCost; }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
            TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.literal(cardType.name().charAt(0) + cardType.name().substring(1).toLowerCase() + " card")
                .withStyle(cardType.color()));
        tooltip.accept(Component.literal(cardDescription).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.literal("Mana cost: " + formatMana(manaCost)).withStyle(ChatFormatting.BLUE));
    }

    public static String formatMana(float mana) {
        return mana == Math.floor(mana) ? String.valueOf((int) mana) : String.format("%.1f", mana);
    }
}
