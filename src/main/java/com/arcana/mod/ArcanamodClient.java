package com.arcana.mod;

import com.arcana.mod.registry.ModEntities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Arcanamod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = Arcanamod.MODID, value = Dist.CLIENT)
public class ArcanamodClient {
    public ArcanamodClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        Arcanamod.LOGGER.info("HELLO FROM CLIENT SETUP");
        Arcanamod.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Spell projectiles render as their item (a fire charge for now)
        event.registerEntityRenderer(ModEntities.PROJECTILE.get(), ThrownItemRenderer::new);
    }
}
