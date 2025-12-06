package io.redspace.ironsspellbooks.api.config;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public record SpellConfigParameter<T>(ResourceLocation key, Codec<T> datatype, T defaultValue) {
}
