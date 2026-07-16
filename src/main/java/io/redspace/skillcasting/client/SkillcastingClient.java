package io.redspace.skillcasting.client;

import io.redspace.skillcasting.client.keybinds.KeyMappings;
import io.redspace.skillcasting.client.overlays.OverlayRegistry;
import io.redspace.skillcasting.client.overlays.RecastOverlay;
import io.redspace.skillcasting.client.render.SkillcastClientTickManager;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;

public final class SkillcastingClient {
    public static void register(IEventBus modEventBus, IEventBus neoForgeBus) {
        modEventBus.addListener(KeyMappings::register);
        modEventBus.addListener(SkillcastingClient::registerGuiLayers);
        neoForgeBus.register(SkillcastingInputEvents.class);
        neoForgeBus.register(SkillcastingClientPlayerEvents.class);
        neoForgeBus.register(RecastOverlay.class);
        neoForgeBus.register(SkillcastClientTickManager.class);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        OverlayRegistry.register(event);
    }
}
