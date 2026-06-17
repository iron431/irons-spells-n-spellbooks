package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.cooldown.CooldownInstance;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record SyncCooldownPacket(CasterId casterId, Holder<AbstractSkill> skill, @Nullable CooldownInstance cooldown)
        implements CustomPacketPayload {

    public static final Type<SyncCooldownPacket> TYPE = new Type<>(Skillcasting.id("sync_cooldown"));

    private static final StreamCodec<RegistryFriendlyByteBuf, @Nullable CooldownInstance> NULLABLE_COOLDOWN =
            ByteBufCodecs.optional(CooldownInstance.STREAM_CODEC)
                    .map(optional -> optional.orElse(null), instance -> Optional.ofNullable(instance));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCooldownPacket> STREAM_CODEC = StreamCodec.composite(
            CasterId.STREAM_CODEC, SyncCooldownPacket::casterId,
            SkillcastingRegistries.SKILL_HOLDER_STREAM_CODEC, SyncCooldownPacket::skill,
            NULLABLE_COOLDOWN, SyncCooldownPacket::cooldown,
            SyncCooldownPacket::new);

    public static SyncCooldownPacket set(CasterRef caster, Holder<AbstractSkill> skill, CooldownInstance instance) {
        return new SyncCooldownPacket(caster.id(), skill, instance);
    }

    public static SyncCooldownPacket remove(CasterRef caster, Holder<AbstractSkill> skill) {
        return new SyncCooldownPacket(caster.id(), skill, null);
    }

    public static void handle(SyncCooldownPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CasterRef caster = packet.casterId().resolve(context.player().level());
            if (caster == null) {
                return;
            }
            SkillcastingData data = caster.skillcastingData();
            if (data != null) {
                data.applySyncedCooldown(packet.skill(), packet.cooldown());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
