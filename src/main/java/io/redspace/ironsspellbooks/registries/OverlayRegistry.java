package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.gui.overlays.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;


@EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)

public class OverlayRegistry {
@SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiLayersEvent event) {
        //Ironsspellbooks.logger.debug("Registering Overlays");

        event.registerBelow(VanillaGuiLayers.CROSSHAIR, IronsSpellbooks.id("cast_bar"), CastBarOverlay.instance);
//
        event.registerAbove(VanillaGuiLayers.AIR_LEVEL, IronsSpellbooks.id("mana_overlay"), ManaBarOverlay.instance);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, IronsSpellbooks.id("spell_bar"), SpellBarOverlay.instance);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, IronsSpellbooks.id("scroll_overlay"), ActiveSpellOverlay.instance);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, IronsSpellbooks.id("recast_bar"), RecastOverlay.instance);

        event.registerAboveAll(IronsSpellbooks.id("spell_wheel"), SpellWheelOverlay.instance);
        event.registerAboveAll(IronsSpellbooks.id("screen_effects"), ScreenEffectsOverlay.instance);
    }
}
