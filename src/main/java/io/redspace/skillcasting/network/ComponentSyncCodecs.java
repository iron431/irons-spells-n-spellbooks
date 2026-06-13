package io.redspace.skillcasting.network;

import io.redspace.skillcasting.api.component.ComponentType;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ComponentSyncCodecs {
    public static final StreamCodec<RegistryFriendlyByteBuf, Integer> INT = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, i -> i, i -> i);

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

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final StreamCodec<RegistryFriendlyByteBuf, Map<ComponentType<?>, Object>> CAST_COMPONENTS =
            StreamCodec.of(
                    (buf, components) -> {
                        ByteBufCodecs.writeCount(buf, components.size(), 32);
                        for (var entry : components.entrySet()) {
                            ComponentType<?> component = entry.getKey();
                            StreamCodec codec = component.streamCodec().orElseThrow();
                            buf.writeResourceLocation(SkillcastingComponentTypes.id(component));
                            codec.encode(buf, entry.getValue());
                        }
                    },
                    buf -> {
                        int count = ByteBufCodecs.readCount(buf, 32);
                        Map<ComponentType<?>, Object> map = new HashMap<>(count);
                        for (int i = 0; i < count; i++) {
                            ResourceLocation id = buf.readResourceLocation();
                            ComponentType<?> type = Objects.requireNonNull(
                                    SkillcastingComponentTypes.get(id), "Unknown component: " + id);
                            map.put(type, type.streamCodec().orElseThrow().decode(buf));
                        }
                        return map;
                    });

    private ComponentSyncCodecs() {}
}
