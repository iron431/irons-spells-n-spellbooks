package com.example.testmod.player;

import com.example.testmod.capabilities.magic.SyncedSpellData;
import com.example.testmod.effect.AbyssalShroudEffect;
import com.example.testmod.entity.mobs.AbstractSpellCastingMob;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
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

            if (level != null) {
                List<Entity> spellcasters = level.getEntities((Entity) null, event.player.getBoundingBox().inflate(64), (mob) -> mob instanceof Player || mob instanceof AbstractSpellCastingMob);
                spellcasters.forEach((entity) -> {
                    LivingEntity livingEntity = (LivingEntity) entity;
                    var spellData = ClientMagicData.getSyncedSpellData(livingEntity);
                    if (spellData.hasEffect(SyncedSpellData.ABYSSAL_SHROUD)) {
                        AbyssalShroudEffect.ambientParticles(level, livingEntity);
                    }
                });
            }

        }
    }

    @SubscribeEvent
    public static void beforeLivingRender(RenderLivingEvent.Pre<? extends LivingEntity, ? extends EntityModel<? extends LivingEntity>> event) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;

        var livingEntity = event.getEntity();
        if (livingEntity instanceof Player || livingEntity instanceof AbstractSpellCastingMob) {

            var syncedData = ClientMagicData.getSyncedSpellData(livingEntity);
            if (syncedData.hasEffect(SyncedSpellData.TRUE_INVIS) && livingEntity.isInvisibleTo(player)) {
                event.setCanceled(true);
            }
        }
    }
}