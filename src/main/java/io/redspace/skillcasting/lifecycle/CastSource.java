package io.redspace.skillcasting.lifecycle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.EquipmentSlot;

import java.util.Optional;

public record CastSource(String name, Optional<EquipmentSlot> equipmentSource) {
    public static final Codec<CastSource> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("name").forGetter(CastSource::name),
            EquipmentSlot.CODEC.optionalFieldOf("slot").forGetter(CastSource::equipmentSource)
    ).apply(builder, CastSource::new));
}
