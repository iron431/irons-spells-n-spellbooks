package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.gui.overlays.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import static net.minecraftforge.client.gui.ForgeIngameGui.*;

@Mod.EventBusSubscriber(Dist.CLIENT)

public class OverlayRegistry {

    @SubscribeEvent
    public static void onRegisterOverlays(FMLClientSetupEvent event) {
 //Ironsspellbooks.logger.debug("Registering Overlays");
        net.minecraftforge.client.gui.OverlayRegistry.registerOverlayAbove(EXPERIENCE_BAR_ELEMENT, "cast_bar", CastBarOverlay::render);

        net.minecraftforge.client.gui.OverlayRegistry.registerOverlayAbove(EXPERIENCE_BAR_ELEMENT, "mana_overlay", ManaBarOverlay::render);
        net.minecraftforge.client.gui.OverlayRegistry.registerOverlayAbove(EXPERIENCE_BAR_ELEMENT, "spell_bar", SpellBarOverlay::render);
        net.minecraftforge.client.gui.OverlayRegistry.registerOverlayAbove(EXPERIENCE_BAR_ELEMENT, "imbued_spell", ImbuedSpellOverlay::render);

        net.minecraftforge.client.gui.OverlayRegistry.registerOverlayAbove(PLAYER_LIST_ELEMENT, "spell_wheel", SpellWheelOverlay.instance::render);
        net.minecraftforge.client.gui.OverlayRegistry.registerOverlayAbove(PLAYER_LIST_ELEMENT, "screen_effects", ScreenEffectsOverlay::render);
    }
}
