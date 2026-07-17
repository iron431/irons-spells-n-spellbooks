package io.redspace.skillcasting.data.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.data.AbstractSkill;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A skill stored at a fixed index inside a {@link ISkillContainer}.
 */
public record SkillSlot(SkillData skillData, int index) {
    public static final Codec<SkillSlot> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            SkillData.CODEC.fieldOf("skill").forGetter(SkillSlot::skillData),
            Codec.INT.fieldOf(SkillContainer.SLOT_INDEX).forGetter(SkillSlot::index)
    ).apply(builder, SkillSlot::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, SkillSlot> SKILL_SLOT = StreamCodec.composite(
            SkillData.STREAM_CODEC, SkillSlot::skillData,
            ByteBufCodecs.VAR_INT, SkillSlot::index,
            SkillSlot::new
    );

    public static SkillSlot of(SkillData skillData, int index) {
        return new SkillSlot(skillData, index);
    }

    public AbstractSkill getSkill() {
        return skillData.getSkill();
    }

    public int getLevel() {
        return skillData.getLevel();
    }

    public boolean isLocked() {
        return skillData.isLocked();
    }
}
