package io.redspace.skillcasting.client;

import io.redspace.skillcasting.Skillcasting;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Registers skillcasting HUD layers. Invoked from {@link SkillcastingClient} on the mod event bus.
 */
public final class OverlayRegistry {
    private OverlayRegistry() {
    }

    public static void register(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.CROSSHAIR, Skillcasting.id("cast_bar"), CastBarOverlay.instance);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, Skillcasting.id("skill_bar"), SkillBarOverlay.instance);
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, Skillcasting.id("recast_bar"), RecastOverlay.instance);
        event.registerAboveAll(Skillcasting.id("skill_wheel"), SkillWheelOverlay.instance);
        event.registerAboveAll(Skillcasting.id("debug_data"), SkillcastingDebugOverlay.instance);
    }
}
