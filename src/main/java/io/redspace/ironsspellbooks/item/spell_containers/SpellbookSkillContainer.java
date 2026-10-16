package io.redspace.ironsspellbooks.item.spell_containers;

import com.mojang.serialization.Codec;
import io.redspace.skillcasting.api.event.SkillSelectionPriority;
import io.redspace.skillcasting.data.skill.ISkillContainer;
import io.redspace.skillcasting.data.skill.SkillContainer;
import io.redspace.skillcasting.data.skill.SkillData;
import io.redspace.skillcasting.data.skill.SkillSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

/**
 * Skill container of a spellbook. Ranks as {@link SkillSelectionPriority#PRIMARY_SKILL_SOURCE}.
 */
public final class SpellbookSkillContainer extends SkillContainer {
    public static final Codec<ISkillContainer> CODEC = ISkillContainer.codec(SpellbookSkillContainer::new);
    public static final StreamCodec<RegistryFriendlyByteBuf, ISkillContainer> STREAM_CODEC = ISkillContainer.streamCodec(SpellbookSkillContainer::new);

    public SpellbookSkillContainer(int maxSkills, boolean skillWheel, boolean mustEquip, List<SkillSlot> skills) {
        super(maxSkills, skillWheel, mustEquip, skills, SpellbookSkillContainer::new);
    }

    @Override
    public SkillSelectionPriority getSelectionPriority() {
        return SkillSelectionPriority.PRIMARY_SKILL_SOURCE;
    }

    @Override
    public Mutable<SpellbookSkillContainer> mutableCopy() {
        return new Mutable<>(this);
    }

    /** Builds a must-equip, skill-wheel spellbook container with the given skills and {@code extraSlots} empty slots. */
    public static SpellbookSkillContainer create(int extraSlots, SkillData... skills) {
        var container = new SpellbookSkillContainer(extraSlots + skills.length, true, true, List.of()).mutableCopy();
        for (SkillData data : skills) {
            container.addSpell(data);
        }
        return container.toImmutable();
    }

    /** Same instance if already a spellbook container, otherwise a copy of {@code source} as one. */
    public static ISkillContainer of(ISkillContainer source) {
        if (source instanceof SpellbookSkillContainer) {
            return source;
        }
        return new SpellbookSkillContainer(source.getMaxSkillCount(), source.isSkillWheel(), source.mustEquip(), source.getActiveSkills());
    }
}
