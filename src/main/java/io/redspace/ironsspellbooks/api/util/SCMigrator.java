package io.redspace.ironsspellbooks.api.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.skillcasting.data.skill.ISkillContainer;
import io.redspace.skillcasting.data.skill.SkillContainer;
import io.redspace.skillcasting.data.skill.SkillData;
import io.redspace.skillcasting.data.skill.SkillSlot;
import net.minecraft.resources.ResourceLocation;

public class SCMigrator {
    //Container Root
    public static final String SPELL_SLOT_CONTAINER = "ISB_Spells";
    public static final String SPELL_DATA = "data";
    public static final String MAX_SLOTS = "maxSpells";
    public static final String MUST_EQUIP = "mustEquip";
    public static final String IMPROVED = "improved";
    public static final String SPELL_WHEEL = "spellWheel";

    //Slot Data
    public static final String SLOT_INDEX = "index";
    public static final String SPELL_ID = "id";
    public static final String SPELL_LEVEL = "level";
    public static final String SPELL_LOCKED = "locked";

    public static final Codec<SkillSlot> SPELL_SLOT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf(SPELL_ID).forGetter(data -> data.getSkill().getSkillId()),
            Codec.INT.fieldOf(SLOT_INDEX).forGetter(SkillSlot::index),
            Codec.INT.fieldOf(SPELL_LEVEL).forGetter(SkillSlot::getLevel),
            Codec.BOOL.optionalFieldOf(SPELL_LOCKED, false).forGetter(SkillSlot::isLocked)
    ).apply(builder, (id, index, lvl, lock) -> SkillSlot.of(new SkillData(SpellRegistry.getSpell(id), lvl, lock), index)));

    public static final Codec<ISkillContainer> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf(MAX_SLOTS).forGetter(ISkillContainer::getMaxSkillCount),
            Codec.BOOL.fieldOf(SPELL_WHEEL).forGetter(ISkillContainer::isSkillWheel),
            Codec.BOOL.fieldOf(MUST_EQUIP).forGetter(ISkillContainer::mustEquip),
            Codec.list(SPELL_SLOT_CODEC).fieldOf(SPELL_DATA).forGetter(ISkillContainer::getActiveSkills)
    ).apply(builder, SkillContainer::fromSerialized));
}
