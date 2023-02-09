package com.example.testmod.player;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.effect.EvasionEffect;
import com.example.testmod.item.Scroll;
import com.example.testmod.item.SpellBook;
import com.example.testmod.setup.Messages;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber()
public class ServerPlayerEvents {

    @SubscribeEvent()
    public static void onLivingEquipmentChangeEvent(LivingEquipmentChangeEvent event) {

        if (event.getEntity().level.isClientSide) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting()
                    && (event.getSlot().getIndex() == 0 || event.getSlot().getIndex() == 1)
                    && (event.getFrom().getItem() instanceof SpellBook || event.getFrom().getItem() instanceof Scroll || event.getFrom().getItem() instanceof SwordItem)) {
                PlayerMagicData.serverSideCancelCast(serverPlayer, playerMagicData);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerOpenContainer(PlayerContainerEvent.Open event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }
        TestMod.LOGGER.debug("onPlayerOpenContainer {} {}", event.getEntity().getName().getString(), event.getContainer().getType());

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting()) {
                PlayerMagicData.serverSideCancelCast(serverPlayer, playerMagicData);
            }
        }
    }

    @SubscribeEvent
    public static void onStartTracking(final PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer && event.getTarget() instanceof ServerPlayer targetPlayer) {
            PlayerMagicData.getPlayerMagicData(serverPlayer).syncToPlayer(targetPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TestMod.LOGGER.debug("onPlayerLoggedIn syncing cooldowns to {}", serverPlayer.getName().getString());
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            Messages.sendToPlayer(playerMagicData.getPlayerCooldowns().getClientSyncCooldownsMessage(), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerMagicData.getPlayerMagicData(serverPlayer).resetCastingState();
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event) {
        TestMod.LOGGER.debug("onPlayerCloned: {}", event.getEntity().getName().getString());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        TestMod.LOGGER.debug("PlayerChangedDimension: {}", event.getEntity().getName().getString());
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            TestMod.LOGGER.debug("onPlayerLoggedIn syncing cooldowns to {}", serverPlayer.getName().getString());
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            Messages.sendToPlayer(playerMagicData.getPlayerCooldowns().getClientSyncCooldownsMessage(), serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        TestMod.LOGGER.debug("onPlayerRespawn: {}", event.getEntity().getName().getString());
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            PlayerMagicData.getPlayerMagicData(serverPlayer).resetCastingState();
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event){
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.getSyncedData().getHasEvasion()) {
                if (EvasionEffect.doEffect(serverPlayer, event.getSource())) {
                    event.setCanceled(true);
                }
            }
        }
    }

//    @SubscribeEvent
//    public static void onPlayerHurt(LivingHurtEvent event) {
//        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
//            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
//            if (playerMagicData.getSyncedData().getHasEvasion()) {
//                if (EvasionEffect.doEffect(serverPlayer, event.getSource())) {
//                    event.setCanceled(true);
//                }
//            }
//        }
//    }

    @SubscribeEvent
    public static void onPlayerTakeDamage(LivingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting() &&
                    SpellType.values()[playerMagicData.getCastingSpellId()].getCastType() == CastType.LONG &&
                    event.getSource() != DamageSource.ON_FIRE &&
                    event.getSource() != DamageSource.WITHER) {
                PlayerMagicData.serverSideCancelCast(serverPlayer, playerMagicData);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityMountEvent(EntityMountEvent event) {
        if (event.getEntity().level.isClientSide) {
            return;
        }

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
            if (playerMagicData.isCasting()) {
                PlayerMagicData.serverSideCancelCast(serverPlayer, playerMagicData);
            }
        }
    }
}
