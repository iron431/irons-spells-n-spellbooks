package io.redspace.skillcasting.selection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class SkillSelection {
    public static final SkillSelection EMPTY = new SkillSelection();

    private static final StreamCodec<RegistryFriendlyByteBuf, @Nullable String> NULLABLE_STRING = StreamCodec.of(
            (buf, value) -> {
                buf.writeBoolean(value != null);
                if (value != null) {
                    buf.writeUtf(value);
                }
            },
            buf -> buf.readBoolean() ? buf.readUtf() : null
    );

    public static final Codec<SkillSelection> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.optionalFieldOf("equipmentSlot").forGetter(s -> Optional.ofNullable(s.equipmentSlot)),
            Codec.INT.optionalFieldOf("index", -1).forGetter(SkillSelection::index),
            Codec.STRING.optionalFieldOf("lastEquipmentSlot").forGetter(s -> Optional.ofNullable(s.lastEquipmentSlot)),
            Codec.INT.optionalFieldOf("lastIndex", -1).forGetter(SkillSelection::lastIndex)
    ).apply(builder, (equipmentSlot, index, lastEquipmentSlot, lastIndex) ->
            new SkillSelection(equipmentSlot.orElse(null), index, lastEquipmentSlot.orElse(null), lastIndex)));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillSelection> STREAM_CODEC = StreamCodec.composite(
            NULLABLE_STRING,
            SkillSelection::equipmentSlot,
            ByteBufCodecs.VAR_INT,
            SkillSelection::index,
            NULLABLE_STRING,
            SkillSelection::lastEquipmentSlot,
            ByteBufCodecs.VAR_INT,
            SkillSelection::lastIndex,
            SkillSelection::new
    );

    @Nullable
    private String equipmentSlot;
    private int index;
    @Nullable
    private String lastEquipmentSlot;
    private int lastIndex;

    public SkillSelection() {
        this(null, -1, null, -1);
    }

    public SkillSelection(@Nullable String equipmentSlot, int index) {
        this(equipmentSlot, index, null, -1);
    }

    public SkillSelection(@Nullable String equipmentSlot, int index, @Nullable String lastEquipmentSlot, int lastIndex) {
        this.equipmentSlot = equipmentSlot;
        this.index = index;
        this.lastEquipmentSlot = lastEquipmentSlot;
        this.lastIndex = lastIndex;
    }

    @Nullable
    public String equipmentSlot() {
        return equipmentSlot;
    }

    public int index() {
        return index;
    }

    @Nullable
    public String lastEquipmentSlot() {
        return lastEquipmentSlot;
    }

    public int lastIndex() {
        return lastIndex;
    }

    public boolean isEmpty() {
        return index < 0;
    }

    public void makeSelection(String equipmentSlot, int index) {
        if (equipmentSlot != null && index >= 0) {
            this.lastEquipmentSlot = this.equipmentSlot;
            this.lastIndex = this.index;
            this.equipmentSlot = equipmentSlot;
            this.index = index;
        }
    }

    public void copyFrom(SkillSelection other) {
        this.equipmentSlot = other.equipmentSlot;
        this.index = other.index;
        this.lastEquipmentSlot = other.lastEquipmentSlot;
        this.lastIndex = other.lastIndex;
    }

    public SkillSelection copy() {
        return new SkillSelection(equipmentSlot, index, lastEquipmentSlot, lastIndex);
    }

    @Override
    public String toString() {
        return String.format(
                "equipmentSlot:%s, index:%d, lastEquipmentSlot:%s, lastIndex:%d",
                equipmentSlot, index, lastEquipmentSlot, lastIndex);
    }
}
