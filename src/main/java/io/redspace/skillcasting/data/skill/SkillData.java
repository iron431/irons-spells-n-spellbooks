package io.redspace.skillcasting.data.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.data.AbstractSkill;
import io.redspace.skillcasting.registry.SkillRegistry;
import io.redspace.skillcasting.registry.SkillcastingRegistries;
import net.minecraft.core.Holder;
import org.jetbrains.annotations.NotNull;

public final class SkillData implements Comparable<SkillData> {
    public static final Codec<SkillData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            SkillcastingRegistries.SKILL_HOLDER_CODEC.fieldOf("skill").forGetter(SkillData::getHolder),
            Codec.INT.fieldOf("level").forGetter(SkillData::getLevel),
            Codec.BOOL.optionalFieldOf("locked", false).forGetter(SkillData::isLocked)
    ).apply(builder, SkillData::fromCodec));

    private final int level;
    private final boolean locked;
    private final Holder<AbstractSkill> skill;

    public SkillData(AbstractSkill skill, int level, boolean locked) {
        this(SkillRegistry.holder(skill), level, locked);
    }

    public SkillData(AbstractSkill skill, int level) {
        this(skill, level, false);
    }

    public SkillData(Holder<AbstractSkill> skill, int level, boolean locked) {
        this.skill = skill;
        this.level = level;
        this.locked = locked;
    }

    public SkillData(Holder<AbstractSkill> skill, int level) {
        this(skill, level, false);
    }


    private static SkillData fromCodec(Holder<AbstractSkill> skill, int level, boolean locked) {
        return new SkillData(skill, level, locked);
    }

    @NotNull
    public Holder<AbstractSkill> getHolder() {
        return skill;
    }

    @NotNull
    public AbstractSkill getSkill() {
        return skill.value();
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
            return this.skill.equals(other.skill) && this.level == other.level && this.locked == other.locked;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * skill.hashCode() + level;
    }

    @Override
    public int compareTo(SkillData other) {
        int i = this.getSkill().getSkillId().compareTo(other.getSkill().getSkillId());
        if (i == 0) {
            i = Integer.compare(level, other.level);
        }
        return i;
    }
}
