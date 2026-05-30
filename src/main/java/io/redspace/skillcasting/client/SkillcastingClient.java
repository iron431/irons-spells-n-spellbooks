package io.redspace.skillcasting.client;

import io.redspace.skillcasting.lifecycle.SkillcastingClientEvents;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

/**
 * Client-only bootstrap for skillcasting HUD overlays and input. Registered from
 * {@link io.redspace.skillcasting.Skillcasting#init} when running on the physical client.
 */
public final class SkillcastingClient {
    private SkillcastingClient() {
    }

    public static void register(IEventBus modEventBus, IEventBus neoForgeBus) {
        modEventBus.addListener(SkillcastingClient::registerGuiLayers);
        neoForgeBus.register(SkillcastingClientEvents.class);
        neoForgeBus.register(RecastOverlay.class);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        OverlayRegistry.register(event);
    }
}
