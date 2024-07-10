package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
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

        event.registerBelow(VanillaGuiLayers.HOTBAR, IronsSpellbooks.id("cast_bar"), CastBarOverlay.instance);
//
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, IronsSpellbooks.id("mana_overlay"), ManaBarOverlay.instance);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, IronsSpellbooks.id("spell_bar"), SpellBarOverlay.instance);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, IronsSpellbooks.id("scroll_overlay"), ActiveSpellOverlay.instance);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, IronsSpellbooks.id("recast_bar"), RecastOverlay.instance);

        event.registerAbove(VanillaGuiLayers.TAB_LIST, IronsSpellbooks.id("spell_wheel"), SpellWheelOverlay.instance);
        event.registerAbove(VanillaGuiLayers.TAB_LIST, IronsSpellbooks.id("screen_effects"), ScreenEffectsOverlay.instance);
    }
}
