package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.data.cast.CasterId;
import io.redspace.skillcasting.data.cast.CasterRef;
import io.redspace.skillcasting.data.component.CastComponentMap;
import io.redspace.skillcasting.data.component.ComponentType;
import io.redspace.skillcasting.data.recast.RecastInstance;
import io.redspace.skillcasting.data.AbstractSkill;
import io.redspace.skillcasting.data.cast.ActiveCast;
import io.redspace.skillcasting.data.SkillcastingData;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashSet;
import java.util.Set;

public record SyncCastComponentsPacket(
        CasterId casterId,
        Holder<AbstractSkill> skill,
        CastComponentMap components,
        Set<ComponentType<?>> removed)
        implements CustomPacketPayload {

    public static final Type<SyncCastComponentsPacket> TYPE =
            new Type<>(Skillcasting.id("sync_cast_components"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCastComponentsPacket> STREAM_CODEC = StreamCodec.composite(
            CasterId.STREAM_CODEC, SyncCastComponentsPacket::casterId,
            SkillcastingRegistries.SKILL_HOLDER_STREAM_CODEC, SyncCastComponentsPacket::skill,
            CastComponentMap.STREAM_CODEC, SyncCastComponentsPacket::components,
            ByteBufCodecs.collection(HashSet::new, CastComponentMap.COMPONENT_TYPE_STREAM_CODEC), SyncCastComponentsPacket::removed,
            SyncCastComponentsPacket::new);

    public static SyncCastComponentsPacket of(CasterRef caster, Holder<AbstractSkill> skill, CastComponentMap components, Set<ComponentType<?>> removed) {
        return new SyncCastComponentsPacket(caster.id(), skill, components, removed);
    }

    public boolean isEmpty() {
        return components.isEmpty() && removed.isEmpty();
    }

    public static void handle(SyncCastComponentsPacket packet, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (packet.isEmpty()) {
                return;
            }
            CasterRef caster = packet.casterId().resolve(ctx.player().level());
            if (caster == null) {
                return;
            }
            applyComponents(caster.skillcastingData(), packet.skill(), packet.components(), packet.removed());
        });
    }

    private static synchronized void applyComponents(SkillcastingData data, Holder<AbstractSkill> skill, CastComponentMap components, Set<ComponentType<?>> removed) {
        ActiveCast active = data.getActiveCast();
        if (active != null && skill.equals(active.context().skill())) {
            active.context().components().applyFrom(components);
            removed.forEach(active.context().components()::remove);
        }
        RecastInstance recast = data.recasts().get(skill);
        if (recast != null) {
            recast.components().applyFrom(components);
            removed.forEach(recast.components()::remove);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
