package io.redspace.skillcasting.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.EquipmentSlot;

public record CastSource(String name, String equipmentSlot) {
    public static final CastSource EMPTY = new CastSource("", "");
    public static final Codec<CastSource> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("name").forGetter(CastSource::name),
            Codec.STRING.fieldOf("equipment_slot").forGetter(CastSource::equipmentSlot)
    ).apply(builder, CastSource::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CastSource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CastSource::name,
            ByteBufCodecs.STRING_UTF8, CastSource::equipmentSlot,
            CastSource::new);

    public static CastSource of(String name, String equipmentSlot) {
        return new CastSource(name, equipmentSlot);
    }

    public static CastSource of(String name, EquipmentSlot equipmentSlot) {
        return new CastSource(name, equipmentSlot.getName());
    }

    public static CastSource of(EquipmentSlot equipmentSlot) {
        return of(equipmentSlot.getName());
    }

    public static CastSource of(String equipmentSlot) {
        return new CastSource(equipmentSlot, equipmentSlot);
    }
}
