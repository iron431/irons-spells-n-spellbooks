package io.redspace.skillcasting.data.selection;

import io.netty.buffer.ByteBuf;
import io.redspace.skillcasting.api.event.GatherSkillSelectionEvent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Comparator;

public record SkillSelectionPriority(int order) {
    public static final SkillSelectionPriority PRIMARY_SKILL_SOURCE = new SkillSelectionPriority(0);
    public static final SkillSelectionPriority CURIO = new SkillSelectionPriority(100);
    public static final SkillSelectionPriority ARMOR = new SkillSelectionPriority(200);
    public static final SkillSelectionPriority HANDHELD = new SkillSelectionPriority(300);
    public static final SkillSelectionPriority OTHER = new SkillSelectionPriority(1000);

    public static final StreamCodec<ByteBuf, SkillSelectionPriority> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(SkillSelectionPriority::new, SkillSelectionPriority::order);

    // Fall back to cast source comparison for deterministic client ordering.
    public static final Comparator<GatherSkillSelectionEvent.Source> SOURCE_ORDER =
            Comparator.<GatherSkillSelectionEvent.Source>comparingInt(source -> source.priority().order())
                    .thenComparing(source -> source.castSource().type())
                    .thenComparing(source -> source.castSource().equipmentSlot());
}
