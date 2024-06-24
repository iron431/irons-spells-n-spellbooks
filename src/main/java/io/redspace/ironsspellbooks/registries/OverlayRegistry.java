package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.gui.overlays.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;


@EventBusSubscriber(Dist.CLIENT)

public class OverlayRegistry {

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiLayersEvent event) {
        //Ironsspellbooks.logger.debug("Registering Overlays");

        event.registerBelow(VanillaGuiLayers.HOTBAR, "cast_bar", CastBarOverlay.instance);
//
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, "mana_overlay", ManaBarOverlay.instance);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, "spell_bar", SpellBarOverlay.instance);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, "scroll_overlay", ActiveSpellOverlay.instance);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, "recast_bar", RecastOverlay.instance);

        event.registerAbove(VanillaGuiLayers.TAB_LIST, "spell_wheel", SpellWheelOverlay.instance);
        event.registerAbove(VanillaGuiLayers.TAB_LIST, "screen_effects", ScreenEffectsOverlay.instance);
    }
}
