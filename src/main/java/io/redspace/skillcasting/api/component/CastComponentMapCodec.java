package io.redspace.skillcasting.api.component;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import io.redspace.skillcasting.registry.SkillcastingRegistries;

import java.util.Map;

public class CastComponentMapCodec implements Codec<CastComponentMap> {
    Codec<Map<ComponentType<?>, Object>> mapCodec = Codec.dispatchedMap(SkillcastingRegistries.COMPONENT_TYPE_CODEC,
            c -> c.codec().orElseThrow(() -> new IllegalArgumentException("Cannot serialize non-persisted Cast ComponentType")));

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
