package io.redspace.skillcasting.lifecycle;


import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.demo.SkillcastingDevCommands;
import io.redspace.skillcasting.network.SelectionSyncPacket;
import io.redspace.skillcasting.network.SkillcastingNetwork;
import io.redspace.skillcasting.registry.SkillcastingAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class SkillcastingEvents {

    private SkillcastingEvents() {

    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        SkillcastingManager.serverTick();
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        SkillcastingManager.serverStopped();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        SkillcastingDevCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        SkillcastingManager.cancelCast(CasterRef.entity(event.getEntity()), CastEndReason.ENTITY_STATE_CHANGE);
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        // should encapsulate logout, dimension change, and other edge cases for both players and nonplayer entities
        // todo: test dimension change
        SkillcastingManager.cancelCast(CasterRef.entity(event.getEntity()), CastEndReason.ENTITY_STATE_CHANGE);
    }
//    @SubscribeEvent
//    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
//        SkillcastingManager.cancelCast(event.getEntity(), CastEndReason.ENTITY_STATE_CHANGE);
//    }
//
//    @SubscribeEvent
//    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
//        SkillcastingManager.cancelCast(event.getEntity(), CastEndReason.ENTITY_STATE_CHANGE);
//    }
//
//    @SubscribeEvent
//    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
//        SkillcastingManager.cancelCast(event.getEntity(), CastEndReason.ENTITY_STATE_CHANGE);
//    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        SkillcastingManager.cancelCast(CasterRef.entity(event.getEntity()), CastEndReason.ENTITY_STATE_CHANGE);
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!ISkillContainer.isSkillContainer(event.getFrom()) && !ISkillContainer.isSkillContainer(event.getTo())) {
            return;
        }
        var data = player.getData(SkillcastingAttachments.SKILLCASTING_DATA.get());
        data.selectionManager().refresh(player);
        SkillcastingNetwork.syncSelection(player, data);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            var data = SkillcastingData.get(player);
            data.selectionManager().refresh(player);
            SkillcastingNetwork.syncSelection(player, data);
            SkillcastingNetwork.syncAllCooldowns(CasterRef.entity(player), SkillcastingData.get(player));
            SkillcastingNetwork.syncAllRecasts(CasterRef.entity(player), SkillcastingData.get(player));
        }
    }
}