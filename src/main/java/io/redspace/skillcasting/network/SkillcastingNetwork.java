package io.redspace.skillcasting.network;

import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.cooldown.CooldownInstance;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.network.SelectionSyncPacket;
import io.redspace.skillcasting.registry.SkillRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public final class SkillcastingNetwork {
//    public static void syncAll(CasterRef caster, SkillcastingData data) {
//        send(caster, StartCastPacket.from(caster, data.getActiveCast()));
//        send(caster, CooldownsSyncPacket.from(caster, data));
//        send(caster, RecastsSyncPacket.from(caster, data));
//        for (var entry : data.recasts().byId().entrySet()) {
//            CastContext ctx = entry.getValue().castContext();
//            if (ctx != null) {
//                Map<ComponentType<?>, Object> synced = ctx.getAllSynced();
//                if (!synced.isEmpty()) {
//                    send(caster, CastComponentsSyncPacket.of(caster, entry.getKey(), synced));
//                }
//            }
//        }
//    }

    public static void syncSelection(ServerPlayer player, SkillcastingData data) {
        SelectionSyncPacket.sendToPlayer(player, data.selectionManager());
    }

    public static void syncCastStart(CasterRef casterRef, ActiveCast activeCast) {
        CastContext ctx = activeCast.context();
        var packet = new CastStartPacket(
                casterRef.id(),
                SkillRegistry.id(ctx.skill().value()),
                activeCast.startedAtGameTime(),
                activeCast.durationTicks(),
                ctx.getAllSynced());
        casterRef.distributeToClients(packet);
    }

    public static void syncCastEnd(CasterRef casterRef) {
        casterRef.distributeToClients(new CastStopPacket(casterRef.id()));
    }

    public static void syncAllCooldowns(CasterRef caster, SkillcastingData data) {
        Map<ResourceLocation, CooldownInstance> synced = new HashMap<>();
        for (Map.Entry<ResourceLocation, CooldownInstance> entry : data.cooldowns().view().entrySet()) {
            CooldownInstance instance = entry.getValue();
            synced.put(entry.getKey(), new CooldownInstance(instance.totalTicks(), instance.endsAtGameTime()));
        }
        caster.distributeToClients(new CooldownsSyncPacket(caster.id(), synced));
    }

    public static void syncAllRecasts(CasterRef caster, SkillcastingData data) {
        caster.distributeToClients(RecastsSyncPacket.from(caster, data));
        for (var entry : data.recasts().byId().entrySet()) {
            CastContext ctx = entry.getValue().castContext();
            if (ctx != null) {
                Map<ComponentType<?>, Object> synced = ctx.getAllSynced();
                if (!synced.isEmpty()) {
                    caster.distributeToClients(CastComponentsSyncPacket.of(caster, entry.getKey(), synced));
                }
            }
        }
    }

    public static void syncDirtyCastComponents(CasterRef caster, CastContext context) {
        Map<ComponentType<?>, Object> dirty = context.popDirtySync();
        if (!dirty.isEmpty()) {
            caster.distributeToClients(CastComponentsSyncPacket.of(caster, context.skill().value().getSkillId(), dirty));
        }
    }

//    private static void send(CasterRef caster, CustomPacketPayload payload) {
//        caster.distributeToClients(payload);
//    }
}
