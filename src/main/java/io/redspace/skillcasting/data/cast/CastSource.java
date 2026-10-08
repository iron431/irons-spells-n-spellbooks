package io.redspace.skillcasting.data.cast;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;

/**
 * Where a cast came from.
 *
 * @param type a string identifier for delineating your own cast sources. By default, it is populated by the key of the data component holding the {@link io.redspace.skillcasting.data.skill.ISkillContainer} granting this skill
 * @param equipmentSlot which slot the source sits in, by {@link EquipmentSlot#getName()} or a mod-defined slot name (such as a curio slot).
 */
public record CastSource(String type, String equipmentSlot) {
    public static final CastSource EMPTY = new CastSource("", "");
    public static final Codec<CastSource> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.fieldOf("type").forGetter(CastSource::type),
            Codec.STRING.fieldOf("equipment_slot").forGetter(CastSource::equipmentSlot)
    ).apply(builder, CastSource::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, CastSource> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, CastSource::type,
            ByteBufCodecs.STRING_UTF8, CastSource::equipmentSlot,
            CastSource::new);

    public boolean isFromSlot(EquipmentSlot slot) {
        return slot.getName().equals(this.equipmentSlot);
    }

    public boolean isFromSlot(InteractionHand hand) {
        return isFromSlot(hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
    }

    public boolean isType(String type) {
        return this.type.equalsIgnoreCase(type);
    }

    public static CastSource of(String name, String equipmentSlot) {
        return new CastSource(name, equipmentSlot);
    }

    public static CastSource of(String name, EquipmentSlot equipmentSlot) {
        return new CastSource(name, equipmentSlot.getName());
    }
}
