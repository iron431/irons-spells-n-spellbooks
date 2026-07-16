package io.redspace.ironsspellbooks.network;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.data.cast.CasterId;
import io.redspace.skillcasting.data.cast.CasterRef;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

public record SyncManaPacket(CasterId casterId, int mana)
        implements CustomPacketPayload {

    public static final Type<SyncManaPacket> TYPE = new Type<>(Skillcasting.id("sync_mana"));

    public static void syncFor(CasterRef casterRef) {
        casterRef.distributeToClients(new SyncManaPacket(casterRef.id(), (int) MagicData.get(casterRef.get()).getMana()));
    }

    public static void syncFor(Entity entity) {
        if (MagicData.has(entity)) {
            syncFor(CasterRef.entity(entity));
        }
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncManaPacket> STREAM_CODEC = StreamCodec.composite(
            CasterId.STREAM_CODEC, SyncManaPacket::casterId,
            ByteBufCodecs.INT, SyncManaPacket::mana,
            SyncManaPacket::new);

    public static void handle(SyncManaPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            CasterRef caster = packet.casterId().resolve(context.player().level());
            if (caster == null) {
                return;
            }
            MagicData data = MagicData.get(caster.get());
            if (data != null) {
                data.setMana(packet.mana());
            }
        });
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}