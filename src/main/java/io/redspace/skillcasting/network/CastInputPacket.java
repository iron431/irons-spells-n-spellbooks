package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.lifecycle.SkillcastingManager;
import io.redspace.skillcasting.registry.SkillRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

@Deprecated
// what in the fucking ai is this
public final class CastInputPacket implements CustomPacketPayload {
    public enum Action {
        CAST,
        CANCEL
    }

    public static final CustomPacketPayload.Type<CastInputPacket> TYPE =
            new CustomPacketPayload.Type<>(Skillcasting.id("cast_input"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CastInputPacket> STREAM_CODEC =
            CustomPacketPayload.codec(CastInputPacket::write, CastInputPacket::new);

    private final Action action;
    private final String skillId;
    private final int baseLevel;

    public CastInputPacket(Action action, String skillId, int baseLevel) {
        this.action = action;
        this.skillId = skillId;
        this.baseLevel = baseLevel;
    }

    public CastInputPacket(FriendlyByteBuf buf) {
        this.action = buf.readEnum(Action.class);
        this.skillId = buf.readUtf();
        this.baseLevel = buf.readVarInt();
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeEnum(action);
        buf.writeUtf(skillId);
        buf.writeVarInt(baseLevel);
    }

    public static void handle(CastInputPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) {
                return;
            }
            CasterRef caster = CasterRef.entity(player);
            if (packet.action == Action.CANCEL) {
                SkillcastingManager.cancelCast(caster, CastEndReason.MANUAL);
                return;
            }
            if (!packet.skillId.isEmpty()) {
                SkillcastingManager.attemptInitiateCast(caster, SkillRegistry.holder(ResourceLocation.parse(packet.skillId)), packet.baseLevel);
            } else {
                SkillcastingManager.attemptInitiateFromSelection(caster);
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
