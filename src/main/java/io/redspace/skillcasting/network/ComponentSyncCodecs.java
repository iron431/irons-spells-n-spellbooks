package io.redspace.skillcasting.network;

import io.redspace.ironsspellbooks.entity.spells.portal.PortalData;
import io.redspace.ironsspellbooks.entity.spells.portal.PortalPos;
import io.redspace.skillcasting.api.component.MultiTargetEntityCastComponent;
import io.redspace.skillcasting.api.recast.RecastConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public final class ComponentSyncCodecs {
    public static final StreamCodec<RegistryFriendlyByteBuf, Integer> INT = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, i -> i, i -> i);

    public static final StreamCodec<RegistryFriendlyByteBuf, Float> FLOAT = StreamCodec.composite(
            ByteBufCodecs.FLOAT, f -> f, f -> f);

    public static final StreamCodec<RegistryFriendlyByteBuf, UUID> UUID = StreamCodec.of(
            (buf, uuid) -> buf.writeUUID(uuid),
            buf -> buf.readUUID());

    public static final StreamCodec<RegistryFriendlyByteBuf, String> STRING = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, s -> s, s -> s);

    public static final StreamCodec<RegistryFriendlyByteBuf, Vec3> VEC3 =
            StreamCodec.of(FriendlyByteBuf::writeVec3, FriendlyByteBuf::readVec3);

    public static final StreamCodec<RegistryFriendlyByteBuf, Vec2> VEC2 = StreamCodec.composite(
            ByteBufCodecs.FLOAT, v -> v.x,
            ByteBufCodecs.FLOAT, v -> v.y,
            Vec2::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, RecastConfig> RECAST_CONFIG = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RecastConfig::totalCasts,
            ByteBufCodecs.VAR_INT, RecastConfig::durationTicks,
            RecastConfig::new);

    public static final StreamCodec<RegistryFriendlyByteBuf, PortalPos> PORTAL_CAST_POS = StreamCodec.of(
            (buf, pos) -> {
                buf.writeResourceKey(pos.dimension());
                buf.writeVec3(pos.pos());
                buf.writeFloat(pos.rotation());
            },
            buf -> PortalPos.of(
                    buf.readResourceKey(Registries.DIMENSION),
                    buf.readVec3(),
                    buf.readFloat()));

    public static final StreamCodec<RegistryFriendlyByteBuf, MultiTargetEntityCastComponent> MULTI_TARGET_ENTITY = StreamCodec.of(
            (buf, component) -> {
                List<UUID> targets = component.getTargets();
                buf.writeVarInt(targets.size());
                for (UUID uuid : targets) {
                    buf.writeUUID(uuid);
                }
            },
            buf -> {
                int count = buf.readVarInt();
                MultiTargetEntityCastComponent component = new MultiTargetEntityCastComponent();
                for (int i = 0; i < count; i++) {
                    component.addTarget(buf.readUUID());
                }
                return component;
            });

    public static final StreamCodec<RegistryFriendlyByteBuf, PortalData> PORTAL_CAST_DATA = StreamCodec.of(
            (buf, data) -> {
                buf.writeVarInt(data.ticksToLive);
                if (data.globalPos1 != null && data.portalEntityId1 != null) {
                    buf.writeBoolean(true);
                    PORTAL_CAST_POS.encode(buf, data.globalPos1);
                    buf.writeUUID(data.portalEntityId1);
                    if (data.globalPos2 != null && data.portalEntityId2 != null) {
                        buf.writeBoolean(true);
                        PORTAL_CAST_POS.encode(buf, data.globalPos2);
                        buf.writeUUID(data.portalEntityId2);
                    } else {
                        buf.writeBoolean(false);
                    }
                } else {
                    buf.writeBoolean(false);
                }
                buf.writeBoolean(data.isBlock);
            },
            buf -> {
                PortalData data = new PortalData();
                data.ticksToLive = buf.readVarInt();
                if (buf.readBoolean()) {
                    data.globalPos1 = PORTAL_CAST_POS.decode(buf);
                    data.portalEntityId1 = buf.readUUID();
                    if (buf.readBoolean()) {
                        data.globalPos2 = PORTAL_CAST_POS.decode(buf);
                        data.portalEntityId2 = buf.readUUID();
                    }
                }
                data.isBlock = buf.readBoolean();
                return data;
            });

    private ComponentSyncCodecs() {}
}
