package io.redspace.ironsspellbooks.api.config;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpellConfigHolder {
    private final Map<SpellConfigParameter<?>, Object> defaultConfig = new HashMap<>();
    private final Map<SpellConfigParameter<?>, Object> config = new HashMap<>();

    public <T> void set(SpellConfigParameter<T> paramtype, T parameter) {
        config.put(paramtype, parameter);
    }

    public <T> void setDefaultValue(SpellConfigParameter<T> paramtype, T parameter) {
        defaultConfig.put(paramtype, parameter);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(SpellConfigParameter<T> paramtype) {
        if (config.containsKey(paramtype)) {
            return (T) config.get(paramtype);
        } else {
            return (T) defaultConfig.getOrDefault(paramtype, paramtype.defaultValue().get());
        }
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getDefaultValue(SpellConfigParameter<T> paramtype) {
        return Optional.ofNullable((T) defaultConfig.get(paramtype));
    }

    public <T> boolean isDefault(SpellConfigParameter<T> parameter) {
        return !config.containsKey(parameter);
    }

    @SuppressWarnings("unchecked")
    public <T> JsonObject toJson(Gson gson) {
        JsonObject json = new JsonObject();
        for (Map.Entry<SpellConfigParameter<?>, Object> entry : this.config.entrySet()) {
            SpellConfigParameter<T> param = (SpellConfigParameter<T>) entry.getKey();
            T value = (T) entry.getValue();
            Codec<T> codec = param.datatype();
            DataResult<JsonElement> result = codec.encodeStart(JsonOps.INSTANCE, value);
            json.add(param.key().toString(), gson.toJsonTree(result.getOrThrow()));
        }
        return json;
    }
}
