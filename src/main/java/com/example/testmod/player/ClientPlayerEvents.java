package com.example.testmod.player;

import com.example.testmod.effect.AbyssalShroudEffect;
import com.example.testmod.entity.AbstractSpellCastingMob;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class ClientPlayerEvents {
    //
    //  Handle (Client Side) cast duration
    //
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.side.isClient() && event.phase == TickEvent.Phase.END && event.player == Minecraft.getInstance().player) {
            var level = Minecraft.getInstance().level;

            ClientMagicData.getCooldowns().tick(1);
            if (ClientMagicData.getCastDuration() > 0) {
                ClientMagicData.handleCastDuration();
            }

            if(level!=null){
                List<Entity> spellcasters = level.getEntities((Entity) null, event.player.getBoundingBox().inflate(64), (mob) -> mob instanceof Player || mob instanceof AbstractSpellCastingMob);
                spellcasters.forEach((entity) -> {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    var spellData = ClientMagicData.getSyncedSpellData(livingEntity);
                    if (spellData.hasAbyssalShroud()) {
                        AbyssalShroudEffect.ambientParticles(level, livingEntity);
                    }
                });
            }

        }
    }
}