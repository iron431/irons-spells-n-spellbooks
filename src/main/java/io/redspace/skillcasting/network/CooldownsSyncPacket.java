package io.redspace.skillcasting.network;

import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.api.cast.CasterId;
import io.redspace.skillcasting.api.cast.CasterRef;
import io.redspace.skillcasting.cooldown.CooldownInstance;
import io.redspace.skillcasting.lifecycle.SkillcastingData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public record CooldownsSyncPacket(CasterId casterId,
                                  Map<ResourceLocation, CooldownInstance> cooldowns) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CooldownsSyncPacket> TYPE = new CustomPacketPayload.Type<>(Skillcasting.id("sync_cooldowns"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CooldownsSyncPacket> STREAM_CODEC = CustomPacketPayload.codec(CooldownsSyncPacket::write, CooldownsSyncPacket::new);

    public CooldownsSyncPacket(RegistryFriendlyByteBuf buf) {
        this(CasterId.STREAM_CODEC.decode(buf), readMap(buf));
    }

    public void write(RegistryFriendlyByteBuf buf) {
        CasterId.STREAM_CODEC.encode(buf, casterId);
        buf.writeVarInt(cooldowns.size());
        cooldowns.forEach((id, entry) -> {
            buf.writeUtf(id.toString());
            buf.writeVarInt(entry.totalTicks());
            buf.writeLong(entry.endsAtGameTime());
        });
    }

    private static Map<ResourceLocation, CooldownInstance> readMap(RegistryFriendlyByteBuf buf) {
        int count = buf.readVarInt();
        Map<ResourceLocation, CooldownInstance> map = new HashMap<>();
        for (int i = 0; i < count; i++) {
            map.put(
                    ResourceLocation.parse(buf.readUtf()),
                    new CooldownInstance(buf.readVarInt(), buf.readLong()));
        }
        return map;
    }

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
