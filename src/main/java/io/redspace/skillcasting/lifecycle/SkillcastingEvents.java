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
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

public final class SkillcastingEvents {

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        var entity = event.getEntity();
        if (entity.hasData(SkillcastingAttachments.SKILLCASTING_DATA)) {
            entity.getData(SkillcastingAttachments.SKILLCASTING_DATA).tick(CasterRef.entity(entity));
        }
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
        SkillcastingManager.cancelCast(CasterRef.entity(event.getEntity()), CastEndReason.INTERRUPTED);
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        // Encapsulates logout (in multiplayer), dimension change, and other edge cases for both players and nonplayer entities, opposed to subscribing to each specific case (which are often player-only anyways)
        SkillcastingManager.cancelCast(CasterRef.entity(event.getEntity()), CastEndReason.INTERRUPTED);
    }

    @SubscribeEvent
    public static void onContainerOpen(PlayerContainerEvent.Open event) {
        SkillcastingManager.cancelCast(CasterRef.entity(event.getEntity()), CastEndReason.INTERRUPTED);
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
            SkillcastingManager.cancelCast(CasterRef.entity(player), CastEndReason.INTERRUPTED);
        }
        data.selectionManager().refresh(player);
        SkillcastingNetwork.syncSelection(player, data);
    }

    @SubscribeEvent
    public static void onJoinLevel(PlayerEvent.PlayerChangedDimensionEvent  event) {
        // fixme: duplicate code with onLogin, make "sync all" handler
        if (event.getEntity() instanceof ServerPlayer player) {
            CasterRef caster = CasterRef.entity(player);
            var data = SkillcastingData.get(player);
            data.selectionManager().refresh(player);
            SkillcastingNetwork.syncSelection(player, data);
            SkillcastingNetwork.syncAllCooldowns(caster, data);
            SkillcastingNetwork.syncAllRecasts(caster, data);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CasterRef caster = CasterRef.entity(player);
            var data = SkillcastingData.get(player);
            data.selectionManager().refresh(player);
            SkillcastingNetwork.syncSelection(player, data);
            SkillcastingNetwork.syncAllCooldowns(caster, data);
            SkillcastingNetwork.syncAllRecasts(caster, data);
        }
    }
}