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

import java.util.HashMap;
import java.util.Map;

public record CooldownsSyncPacket(CasterId casterId,
                                  Map<Holder<AbstractSkill>, CooldownInstance> cooldowns) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CooldownsSyncPacket> TYPE = new CustomPacketPayload.Type<>(Skillcasting.id("sync_cooldowns"));

    private static final StreamCodec<RegistryFriendlyByteBuf, Map<Holder<AbstractSkill>, CooldownInstance>> COOLDOWNS_MAP =
            ByteBufCodecs.map(HashMap::new, SkillcastingRegistries.SKILL_HOLDER_STREAM_CODEC, CooldownInstance.STREAM_CODEC);

    public static final StreamCodec<RegistryFriendlyByteBuf, CooldownsSyncPacket> STREAM_CODEC = StreamCodec.composite(
            CasterId.STREAM_CODEC, CooldownsSyncPacket::casterId,
            COOLDOWNS_MAP, CooldownsSyncPacket::cooldowns,
            CooldownsSyncPacket::new);

    public static void handle(CooldownsSyncPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CasterRef caster = packet.casterId().resolve(context.player().level());
            if (caster == null) {
                return;
            }
            SkillcastingData data = caster.skillcastingData();
            if (data != null) {
                data.applySyncedCooldowns(packet.cooldowns);
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
