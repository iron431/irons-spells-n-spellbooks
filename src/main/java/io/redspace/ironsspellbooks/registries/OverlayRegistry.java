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

        //ah it auto-wraps in resourcelocation, it seems

        event.registerBelow(VanillaGuiOverlay.CROSSHAIR.id(), "cast_bar"/*IronsSpellbooks.id("cast_bar").toString()*/, CastBarOverlay.instance);
        event.registerAbove(VanillaGuiOverlay.AIR_LEVEL.id(), "mana_overlay"/*IronsSpellbooks.id("mana_overlay").toString()*/, ManaBarOverlay.instance);
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "spell_bar"/*IronsSpellbooks.id("spell_bar").toString()*/, SpellBarOverlay.instance);
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "recast_bar"/*IronsSpellbooks.id("recast_bar").toString()*/, RecastOverlay.instance);

        event.registerAboveAll("spell_wheel"/*IronsSpellbooks.id("spell_wheel").toString()*/, SpellWheelOverlay.instance);
        event.registerAboveAll("screen_effects"/*IronsSpellbooks.id("screen_effects").toString()*/, ScreenEffectsOverlay.instance);
        event.registerAboveAll("screen_tooltip"/*IronsSpellbooks.id("screen_tooltip").toString()*/, ScreenTooltipOverlay.instance);
    }
}
