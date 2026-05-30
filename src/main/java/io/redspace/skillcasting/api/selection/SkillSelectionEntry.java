package io.redspace.skillcasting.api.selection;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

/**
 * One selectable skill in the aggregated bar, tagged with the equipment source that offered it.
 */
public record SkillSelectionEntry(ResourceLocation skillId, int level, String source) {
    public static final Codec<SkillSelectionEntry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf("skill").forGetter(SkillSelectionEntry::skillId),
            Codec.INT.fieldOf("level").forGetter(SkillSelectionEntry::level),
            Codec.STRING.fieldOf("source").forGetter(SkillSelectionEntry::source)
    ).apply(builder, SkillSelectionEntry::new));
}
