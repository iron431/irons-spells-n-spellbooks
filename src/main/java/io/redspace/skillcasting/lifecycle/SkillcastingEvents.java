package io.redspace.skillcasting.lifecycle;


import net.neoforged.neoforge.event.level.ChunkEvent;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.level.LevelEvent;
import io.redspace.skillcasting.data.SkillcastingData;
import io.redspace.skillcasting.data.cast.ActiveCast;
import io.redspace.skillcasting.data.cast.CastEndReason;
import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.network.SkillcastingNetwork;
import io.redspace.skillcasting.registry.SkillcastingAttachments;
import io.redspace.skillcasting.registry.SkillcastingDataComponents;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class SkillcastingEvents {

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        var entity = event.getEntity();
        if (SkillcastingData.has(entity)) {
            SkillcastingData.get(entity).tick(CasterRef.entity(entity));
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        SkillcastingManager.serverTick();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SkillcastingManager.serverStopped();
        VirtualCasterManager.clearAll();
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (event.getLevel() instanceof Level level && level.isClientSide()) {
            VirtualCasterManager.dropInChunk(level, event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof Level level) {
            VirtualCasterManager.clear(level);
        }
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
//        SkillcastingDevCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer player && SkillcastingData.has(event.getTarget())) {
            ActiveCast activeCast = SkillcastingData.get(event.getTarget()).getActiveCast();
            if (activeCast != null) {
                SkillcastingNetwork.syncCastStart(player, CasterRef.entity(event.getTarget()), activeCast);
            }
        }
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        if (SkillcastingData.has(event.getEntity())) {
            SkillcastingManager.cancelCast(CasterRef.entity(event.getEntity()), CastEndReason.INTERRUPTED);
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        // Encapsulates logout (in multiplayer), dimension change, and other edge cases for both players and nonplayer entities, opposed to subscribing to each specific case (which are often player-only anyways)
        if (SkillcastingData.has(event.getEntity())) {
            SkillcastingManager.cancelCast(CasterRef.entity(event.getEntity()), CastEndReason.INTERRUPTED);
        }
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        if (SkillcastingData.has(event.getEntity())) {
            SkillcastingManager.cancelCast(CasterRef.entity(event.getEntity()), CastEndReason.INTERRUPTED);
        }
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!SkillcastingUtils.hasAnySkillContainers(event.getFrom()) && !SkillcastingUtils.hasAnySkillContainers(event.getTo())) {
            return;
        }

        var data = player.getData(SkillcastingAttachments.SKILLCASTING_DATA.get());
        if (data.isCasting()
                && SkillcastingUtils.shouldCancelCastOnEquipmentChange(
                data.getActiveCast(), event.getFrom(), event.getTo(), event.getSlot())) {
            SkillcastingManager.cancelCast(CasterRef.entity(player), CastEndReason.INTERRUPTED);
        }
        data.selectionManager().refresh(player);
        SkillcastingNetwork.syncSelection(player, data);
    }

    @SubscribeEvent
    public static void onJoinLevel(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SkillcastingNetwork.syncAll(player);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SkillcastingNetwork.syncAll(player);
        }
    }

    @SubscribeEvent
    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SkillcastingNetwork.syncAll(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        if (SkillcastingData.has(event.getEntity()) && SkillcastingData.get(event.getEntity()).isCasting() &&
                !SkillcastingData.get(event.getEntity()).getActiveCast().context().getCastSource().isFromSlot(event.getHand())) {
            event.setCanceled(true);
        }
        if (event.getItemStack().has(SkillcastingDataComponents.CASTING_IMPLEMENT)) {
            if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                SkillcastingManager.attemptInitiateFromSelection(CasterRef.entity(serverPlayer));
            }
            event.setCancellationResult(InteractionResult.CONSUME);
        }
    }
}