package io.redspace.skillcasting.network;

import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.component.CastComponentMap;
import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.api.recast.RecastInstance;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.cooldown.CooldownInstance;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public final class SkillcastingNetwork {
    public static void syncSelection(ServerPlayer player, SkillcastingData data) {
        SyncSelectionPacket.sendToPlayer(player, data.selectionManager());
    }

    public static void syncAll(ServerPlayer player) {
        var casterRef = CasterRef.entity(player);
        var data = casterRef.skillcastingData();
        data.selectionManager().refresh(player);
        SkillcastingNetwork.syncSelection(player, data);
        SkillcastingNetwork.syncAllCooldowns(casterRef, data);
        SkillcastingNetwork.syncAllRecasts(casterRef, data);
    }

    public static void syncCastStart(CasterRef casterRef, ActiveCast activeCast) {
        CastContext context = activeCast.context();
        var packet = new CastStartPacket(
                casterRef.id(),
                context.skill(),
                activeCast.durationTicks(),
                CastComponentMap.from(context.components().getAllSynced()));
        casterRef.distributeToClients(packet);
    }

    public static void syncCastEnd(CasterRef casterRef, CastContext castContext, CastEndReason reason) {
        casterRef.distributeToClients(new CastStopPacket(casterRef.id(), castContext.skill(), castContext.components(), reason));
    }

    public static void syncCooldown(CasterRef caster, Holder<AbstractSkill> skill, CooldownInstance instance) {
        caster.distributeToClients(SyncCooldownPacket.set(caster, skill, instance));
    }

    public static void syncCooldownRemove(CasterRef caster, Holder<AbstractSkill> skill) {
        caster.distributeToClients(SyncCooldownPacket.remove(caster, skill));
    }

    public static void syncRecast(CasterRef caster, Holder<AbstractSkill> skill, RecastInstance instance) {
        caster.distributeToClients(SyncRecastPacket.set(caster, skill, instance));
    }

    public static void syncRecastRemove(CasterRef caster, Holder<AbstractSkill> skill) {
        caster.distributeToClients(SyncRecastPacket.remove(caster, skill));
    }

    public static void syncAllCooldowns(CasterRef caster, SkillcastingData data) {
        Map<Holder<AbstractSkill>, CooldownInstance> synced = new HashMap<>();
        for (var entry : data.cooldowns().view().entrySet()) {
            CooldownInstance instance = entry.getValue();
            synced.put(entry.getKey(), new CooldownInstance(instance.totalTicks(), instance.remainingTicks()));
        }
        caster.distributeToClients(new SyncAllCooldownsPacket(caster.id(), synced));
    }

    public static void syncAllRecasts(CasterRef caster, SkillcastingData data) {
        caster.distributeToClients(SyncAllRecastsPacket.from(caster, data));
    }

    public static void syncDirtyCastComponents(CasterRef caster, CastContext context) {
        Map<ComponentType<?>, Object> dirty = context.components().popDirtySync();
        if (!dirty.isEmpty()) {
            caster.distributeToClients(SyncCastComponentsPacket.of(caster, context.skill(), CastComponentMap.from(dirty)));
        }
    }
}
