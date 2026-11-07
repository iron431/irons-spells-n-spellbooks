package io.redspace.skillcasting.data.component;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Encoder;
import io.redspace.skillcasting.Skillcasting;
import io.redspace.skillcasting.registry.SkillcastingRegistries;

import java.util.Map;

public class CastComponentMapCodec implements Codec<CastComponentMap> {
    private static final Codec<?> NOT_PERSISTED = Codec.of(Encoder.error("Cast ComponentType is not persisted"), Decoder.error("Cast ComponentType is not persisted"));

    Codec<Map<ComponentType<?>, Object>> mapCodec = Codec.<ComponentType<?>, Object>dispatchedMap(SkillcastingRegistries.COMPONENT_TYPE_CODEC,
                    c -> c.codec().<Codec<?>>map(codec -> codec).orElse(NOT_PERSISTED))
            .promotePartial(error -> Skillcasting.LOGGER.warn("Dropped unreadable cast components: {}", error));

    @Override
    public <T> DataResult<Pair<CastComponentMap, T>> decode(DynamicOps<T> ops, T input) {
        return mapCodec.decode(ops, input).map(pair -> pair.mapFirst(CastComponentMap::from));
    }

    @Override
    public <T> DataResult<T> encode(CastComponentMap input, DynamicOps<T> ops, T prefix) {
        Map<ComponentType<?>, Object> persistedComponents = input.getAllPersisted();
        return mapCodec.encode(persistedComponents, ops, prefix);
    }
}
