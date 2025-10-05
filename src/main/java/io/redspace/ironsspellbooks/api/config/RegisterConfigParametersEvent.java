package io.redspace.ironsspellbooks.api.config;

import net.neoforged.bus.api.Event;

import java.util.List;

public class RegisterConfigParametersEvent extends Event {
    private final List<SpellConfigParameter<?>> types;

    public RegisterConfigParametersEvent(List<SpellConfigParameter<?>> types) {
        this.types = types;
    }

    public void register(SpellConfigParameter<?> parameterType) {
        this.types.add(parameterType);
    }
}
