package io.redspace.skillcasting.client;

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
        modEventBus.addListener(KeyMappings::register);
        modEventBus.addListener(SkillcastingClient::registerGuiLayers);
        modEventBus.addListener(SkillTargetingLayer::register);
        neoForgeBus.register(ClientInputEvents.class);
        neoForgeBus.register(SkillcastingClientPlayerEvents.class);
        neoForgeBus.register(RecastOverlay.class);
        neoForgeBus.register(SkillcastClientTickManager.class);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        OverlayRegistry.register(event);
    }
}
