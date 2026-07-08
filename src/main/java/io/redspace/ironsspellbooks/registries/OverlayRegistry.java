package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.gui.overlays.ManaBarOverlay;
import io.redspace.ironsspellbooks.gui.overlays.ScreenEffectsOverlay;
import io.redspace.ironsspellbooks.gui.overlays.ScreenTooltipOverlay;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;


@EventBusSubscriber(modid = IronsSpellbooks.MODID, value = Dist.CLIENT)

public class OverlayRegistry {
    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiLayersEvent event) {

        event.registerAbove(VanillaGuiLayers.AIR_LEVEL, IronsSpellbooks.id("mana_overlay"), ManaBarOverlay.instance);
        event.registerAboveAll(IronsSpellbooks.id("screen_effects"), ScreenEffectsOverlay.instance);
        event.registerAboveAll(IronsSpellbooks.id("screen_tooltip"), ScreenTooltipOverlay.instance);
    }
}
