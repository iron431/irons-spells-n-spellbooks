package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.gui.overlays.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;


@EventBusSubscriber(modid = IronsSpellbooks.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)

public class OverlayRegistry {
    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {

        event.registerBelow(VanillaGuiOverlay.CROSSHAIR.id(), IronsSpellbooks.id("cast_bar").toString(), CastBarOverlay.instance);
        event.registerAbove(VanillaGuiOverlay.AIR_LEVEL.id(), IronsSpellbooks.id("mana_overlay").toString(), ManaBarOverlay.instance);
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), IronsSpellbooks.id("spell_bar").toString(), SpellBarOverlay.instance);
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), IronsSpellbooks.id("recast_bar").toString(), RecastOverlay.instance);

        event.registerAboveAll(IronsSpellbooks.id("spell_wheel").toString(), SpellWheelOverlay.instance);
        event.registerAboveAll(IronsSpellbooks.id("screen_effects").toString(), ScreenEffectsOverlay.instance);
        event.registerAboveAll(IronsSpellbooks.id("screen_tooltip").toString(), ScreenTooltipOverlay.instance);
    }
}
