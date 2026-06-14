package io.redspace.skillcasting.lifecycle;


import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.data.ISkillContainer;
import io.redspace.skillcasting.demo.SkillcastingDevCommands;
import io.redspace.skillcasting.network.SkillcastingNetwork;
import io.redspace.skillcasting.registry.SkillcastingAttachments;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
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
        // Encapsulates logout (in multiplayer), dimension change, and other edge cases for both players and nonplayer entities, opposed to subscribing to each specific case (which are often player-only anyways)
        SkillcastingManager.cancelCast(CasterRef.entity(event.getEntity()), CastEndReason.ENTITY_STATE_CHANGE);
    }

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
        if (data.isCasting()
                && SkillcastingUtils.shouldCancelCastOnEquipmentChange(
                        data.getActiveCast(), event.getFrom(), event.getTo(), event.getSlot())) {
            SkillcastingManager.cancelCast(CasterRef.entity(player), CastEndReason.ENTITY_STATE_CHANGE);
        }
        data.selectionManager().refresh(player);
        SkillcastingNetwork.syncSelection(player, data);
    }

    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        CasterRef caster = CasterRef.entity(player);
        SkillcastingData.get(player).rehydrateRecasts(caster);
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CasterRef caster = CasterRef.entity(player);
            var data = SkillcastingData.get(player);
            data.rehydrateRecasts(caster);
            data.selectionManager().refresh(player);
            SkillcastingNetwork.syncSelection(player, data);
            SkillcastingNetwork.syncAllCooldowns(caster, data);
            SkillcastingNetwork.syncAllRecasts(caster, data);
        }
    }
}