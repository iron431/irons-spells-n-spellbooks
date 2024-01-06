package io.redspace.ironsspellbooks.registries;

import io.redspace.ironsspellbooks.gui.overlays.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)

public class OverlayRegistry {

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "cast_bar", CastBarOverlay::render);

        event.registerBelow(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "mana_overlay", ManaBarOverlay::render);
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "spell_bar", SpellBarOverlay::render);
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "scroll_overlay", ActiveSpellOverlay::render);
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "recast_bar", RecastOverlay::render);

        event.registerAbove(VanillaGuiOverlay.PLAYER_LIST.id(), "spell_wheel", SpellWheelOverlay.instance::render);
        event.registerAbove(VanillaGuiOverlay.PLAYER_LIST.id(), "screen_effects", ScreenEffectsOverlay::render);
    }
}
