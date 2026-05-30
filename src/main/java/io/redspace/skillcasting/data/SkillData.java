package io.redspace.skillcasting.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import io.redspace.skillcasting.registry.SkillRegistry;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class SkillData implements Comparable<SkillData> {
    public static final Codec<SkillData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("skill").forGetter(SkillData::skillId),
            Codec.INT.fieldOf("level").forGetter(SkillData::getLevel),
            Codec.BOOL.optionalFieldOf("locked", false).forGetter(SkillData::isLocked)
    ).apply(builder, SkillData::fromCodec));

    private final ResourceLocation skillId;
    private final int level;
    private final boolean locked;

    public SkillData(AbstractSkill skill, int level, boolean locked) {
        this(Objects.requireNonNull(SkillRegistry.id(skill)), level, locked);
    }

    public SkillData(AbstractSkill skill, int level) {
        this(skill, level, false);
    }

    public SkillData(ResourceLocation skillId, int level, boolean locked) {
        this.skillId = Objects.requireNonNull(skillId);
        this.level = level;
        this.locked = locked;
    }

    private static SkillData fromCodec(ResourceLocation skillId, int level, boolean locked) {
        return new SkillData(skillId, level, locked);
    }

    public ResourceLocation skillId() {
        return skillId;
    }

    @Nullable
    public AbstractSkill getSkill() {
        return SkillRegistry.get(skillId);
    }

    public int getLevel() {
        return level;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean canRemove() {
        return !locked;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SkillData other) {
            return this.skillId.equals(other.skillId) && this.level == other.level;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * skillId.hashCode() + level;
    }

    @Override
    public int compareTo(SkillData other) {
        int i = skillId.compareTo(other.skillId);
        if (i == 0) {
            i = Integer.compare(level, other.level);
        }
        return i;
    }
}
