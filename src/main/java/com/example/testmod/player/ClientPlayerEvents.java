package com.example.testmod.player;

import com.example.testmod.effect.AbyssalShroudEffect;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientPlayerEvents {
    //
    //  Handle (Client Side) cast duration
    //
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && event.phase == TickEvent.Phase.END && event.player == Minecraft.getInstance().player) {
            ClientMagicData.getCooldowns().tick(1);
            if (ClientMagicData.getCastDuration() > 0) {
                ClientMagicData.handleCastDuration();
            }

            //TODO: what about spellcasting mobs?
            var level = Minecraft.getInstance().level;
            level.players().forEach((player) -> {
                var spellData = ClientMagicData.getSyncedSpellData(player);
                if (spellData.hasAbyssalShroud()) {
                    AbyssalShroudEffect.ambientParticles(level, player);
                }
            });
        }
    }
}