package io.redspace.ironsspellbooks.api.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpellConfigHolder {
    //    public static final SpellConfigHolder EMPTY = new SpellConfigHolder();
    private final Map<SpellConfigParameter<?>, Object> config = new HashMap<>();

    public <T> void set(SpellConfigParameter<T> paramtype, T parameter) {
        config.put(paramtype, parameter);
    }

    public <T> Optional<T> get(SpellConfigParameter<T> paramtype) {
        return Optional.ofNullable((T) config.get(paramtype));
    }

    public boolean isEmpty() {
        return config.isEmpty();
    }

    public boolean isSet(SpellConfigParameter<?> paramtype) {
        return config.containsKey(paramtype);
    }
}
