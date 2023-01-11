package com.example.testmod.registries;

import com.example.testmod.TestMod;
import com.example.testmod.gui.CastBarOverlay;
import com.example.testmod.gui.ManaBarOverlay;
import com.example.testmod.gui.SpellBarOverlay;
import com.example.testmod.gui.SpellWheelOverlay;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)

public class OverlayRegistry {

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        TestMod.LOGGER.debug("Registering Overlays");
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "mana_overlay", ManaBarOverlay::render);
        event.registerBelow(VanillaGuiOverlay.HOTBAR.id(), "cast_bar", CastBarOverlay::render);

        event.registerAbove(VanillaGuiOverlay.ARMOR_LEVEL.id(), "spell_bar", SpellBarOverlay::render);

        event.registerAbove(VanillaGuiOverlay.PLAYER_LIST.id(), "spell_wheel", SpellWheelOverlay::render);
    }
}
