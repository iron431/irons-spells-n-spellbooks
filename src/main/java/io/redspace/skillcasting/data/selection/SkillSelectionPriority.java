package io.redspace.skillcasting.data.selection;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record SkillSelectionPriority(int order) {
    public static final SkillSelectionPriority PRIMARY_SKILL_SOURCE = new SkillSelectionPriority(0);
    public static final SkillSelectionPriority CURIO = new SkillSelectionPriority(100);
    public static final SkillSelectionPriority ARMOR = new SkillSelectionPriority(200);
    public static final SkillSelectionPriority HANDHELD = new SkillSelectionPriority(300);
    public static final SkillSelectionPriority OTHER = new SkillSelectionPriority(1000);

    public static final StreamCodec<ByteBuf, SkillSelectionPriority> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(SkillSelectionPriority::new, SkillSelectionPriority::order);
}
