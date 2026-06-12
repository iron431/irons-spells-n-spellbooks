package io.redspace.skillcasting.selection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public class SkillSelection {
    public static final SkillSelection EMPTY = new SkillSelection();

    public static final Codec<SkillSelection> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.STRING.optionalFieldOf("source", "").forGetter(SkillSelection::sourceId),
            Codec.INT.optionalFieldOf("index", -1).forGetter(SkillSelection::index),
            Codec.STRING.optionalFieldOf("lastSource", "").forGetter(SkillSelection::lastSourceId),
            Codec.INT.optionalFieldOf("lastIndex", -1).forGetter(SkillSelection::lastIndex)
    ).apply(builder, SkillSelection::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillSelection> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            SkillSelection::sourceId,
            ByteBufCodecs.VAR_INT,
            SkillSelection::index,
            ByteBufCodecs.STRING_UTF8,
            SkillSelection::lastSourceId,
            ByteBufCodecs.VAR_INT,
            SkillSelection::lastIndex,
            SkillSelection::new
    );

    private String sourceId;
    private int index;
    private String lastSourceId;
    private int lastIndex;

    public SkillSelection() {
        this("", -1, "", -1);
    }

    public SkillSelection(String sourceId, int index) {
        this(sourceId, index, "", -1);
    }

    public SkillSelection(String sourceId, int index, String lastSourceId, int lastIndex) {
        this.sourceId = sourceId;
        this.index = index;
        this.lastSourceId = lastSourceId;
        this.lastIndex = lastIndex;
    }

    public String sourceId() {
        return sourceId;
    }

    public int index() {
        return index;
    }

    public String lastSourceId() {
        return lastSourceId;
    }

    public int lastIndex() {
        return lastIndex;
    }

    public boolean isEmpty() {
        return index < 0;
    }

    public void makeSelection(String sourceId, int index) {
        if (sourceId != null && index >= 0) {
            this.lastSourceId = this.sourceId;
            this.lastIndex = this.index;
            this.sourceId = sourceId;
            this.index = index;
        }
    }

    public void copyFrom(SkillSelection other) {
        this.sourceId = other.sourceId;
        this.index = other.index;
        this.lastSourceId = other.lastSourceId;
        this.lastIndex = other.lastIndex;
    }

    public SkillSelection copy() {
        return new SkillSelection(sourceId, index, lastSourceId, lastIndex);
    }

    @Override
    public String toString() {
        return String.format(
                "sourceId:%s, index:%d, lastSourceId:%s, lastIndex:%d",
                sourceId, index, lastSourceId, lastIndex);
    }
}
