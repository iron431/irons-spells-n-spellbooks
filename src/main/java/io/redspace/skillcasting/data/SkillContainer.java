package io.redspace.skillcasting.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.api.skill.AbstractSkill;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SkillContainer implements ISkillContainer {
    public static final String SPELL_DATA = "data";
    public static final String MAX_SLOTS = "size";
    public static final String MUST_EQUIP = "equip";
    public static final String SPELL_WHEEL = "wheel";

    public static final String SLOT_INDEX = "index";

    public static final Codec<SkillSlot> SPELL_SLOT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            SkillData.CODEC.fieldOf("skill").forGetter(SkillSlot::skillData),
            Codec.INT.fieldOf(SLOT_INDEX).forGetter(SkillSlot::index)
    ).apply(builder, SkillSlot::new));

    public static final Codec<ISkillContainer> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf(MAX_SLOTS).forGetter(ISkillContainer::getMaxSpellCount),
            Codec.BOOL.fieldOf(SPELL_WHEEL).forGetter(ISkillContainer::isSpellWheel),
            Codec.BOOL.fieldOf(MUST_EQUIP).forGetter(ISkillContainer::mustEquip),
            Codec.BOOL.optionalFieldOf("improved", false).forGetter(ISkillContainer::isImproved),
            Codec.list(SPELL_SLOT_CODEC).fieldOf(SPELL_DATA).forGetter(ISkillContainer::getActiveSpells)
    ).apply(builder, (count, wheel, equip, improved, spells) -> {
        var container = new SkillContainer(count, wheel, equip, improved);
        spells.forEach(slot -> container.slots[slot.index()] = slot);
        container.activeSlots = spells.size();
        return container;
    }));

    SkillSlot[] slots;
    int maxSpells;
    int activeSlots;
    boolean spellWheel;
    boolean mustEquip;
    boolean improved;

    public SkillContainer() {
        this(0, false, true, false);
    }

    public SkillContainer(int maxSpells, boolean spellWheel, boolean mustEquip) {
        this(maxSpells, spellWheel, mustEquip, false);
    }

    public SkillContainer(int maxSpells, boolean spellWheel, boolean mustEquip, boolean improved) {
        this.maxSpells = maxSpells;
        this.slots = new SkillSlot[this.maxSpells];
        this.spellWheel = spellWheel;
        this.mustEquip = mustEquip;
        this.improved = improved;
    }

    public SkillContainer(int maxSpells, boolean spellWheel, boolean mustEquip, boolean improved, SkillSlot[] slots) {
        this.maxSpells = maxSpells;
        this.slots = slots;
        this.spellWheel = spellWheel;
        this.mustEquip = mustEquip;
        this.improved = improved;
        this.activeSlots = (int) Arrays.stream(slots).filter(Objects::nonNull).count();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof SkillContainer o &&
                Arrays.equals(o.slots, this.slots) &&
                this.maxSpells == o.maxSpells &&
                this.activeSlots == o.activeSlots &&
                this.spellWheel == o.spellWheel &&
                this.mustEquip == o.mustEquip &&
                this.improved == o.improved);
    }

    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(slots);
        hash = (hash * 31 + maxSpells) * 31 + activeSlots;
        hash *= 1000;
        hash += spellWheel ? 100 : 0;
        hash += mustEquip ? 10 : 0;
        hash += improved ? 1 : 0;
        return hash;
    }

    @Override
    public int getMaxSpellCount() {
        return maxSpells;
    }

    @Override
    public int getActiveSpellCount() {
        return activeSlots;
    }

    @Override
    public boolean isEmpty() {
        return activeSlots == 0;
    }

    @Override
    public SkillSlot[] getAllSpells() {
        var result = new SkillSlot[maxSpells];
        if (maxSpells > 0) {
            System.arraycopy(slots, 0, result, 0, slots.length);
        }
        return result;
    }

    @Override
    public @NotNull List<SkillSlot> getActiveSpells() {
        return Arrays.stream(slots).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public int getNextAvailableIndex() {
        return ArrayUtils.indexOf(slots, null);
    }

    @Override
    public boolean mustEquip() {
        return mustEquip;
    }

    @Override
    public boolean isSpellWheel() {
        return spellWheel;
    }

    @Override
    public boolean isImproved() {
        return improved;
    }

    @Override
    public @NotNull SkillData getSpellAtIndex(int index) {
        if (index >= 0 && index < maxSpells) {
            var result = slots[index];
            if (result != null) {
                return result.skillData();
            }
        }
        throw new IndexOutOfBoundsException("No skill at index " + index);
    }

    @Override
    public int getIndexForSpell(AbstractSkill spell) {
        for (int i = 0; i < maxSpells; i++) {
            var slot = slots[i];
            if (slot != null && spell.equals(slot.getSkill())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ISkillContainerMutable mutableCopy() {
        return new Mutable(this);
    }

    public static class Mutable extends SkillContainer implements ISkillContainerMutable {
        public Mutable(SkillContainer container) {
            this.maxSpells = container.maxSpells;
            this.activeSlots = container.activeSlots;
            this.spellWheel = container.spellWheel;
            this.mustEquip = container.mustEquip;
            this.improved = container.improved;
            this.slots = Arrays.copyOf(container.slots, container.slots.length);
        }

        @Override
        public void setMaxSpellCount(int maxSpells) {
            this.maxSpells = maxSpells;
            slots = Arrays.copyOf(slots, maxSpells);
        }

        @Override
        public void setImproved(boolean improved) {
            this.improved = improved;
        }

        @Override
        public boolean addSpellAtIndex(AbstractSkill spell, int level, int index, boolean locked) {
            if (index > -1 && index < maxSpells &&
                    slots[index] == null &&
                    Arrays.stream(slots).noneMatch(s -> s != null && spell.equals(s.getSkill()))) {
                slots[index] = SkillSlot.of(new SkillData(spell, level, locked), index);
                activeSlots++;
                return true;
            }
            return false;
        }

        @Override
        public boolean addSpell(AbstractSkill spell, int level, boolean locked) {
            return addSpellAtIndex(spell, level, getNextAvailableIndex(), locked);
        }

        @Override
        public boolean removeSpellAtIndex(int index) {
            if (index > -1 && index < maxSpells && slots[index] != null) {
                slots[index] = null;
                activeSlots--;
                return true;
            }
            return false;
        }

        @Override
        public boolean removeSpell(AbstractSkill spell) {
            if (spell == null) {
                return false;
            }
            for (int i = 0; i < maxSpells; i++) {
                var slot = slots[i];
                if (slot != null && spell.equals(slot.getSkill())) {
                    return removeSpellAtIndex(i);
                }
            }
            return false;
        }

        @Override
        public ISkillContainer toImmutable() {
            return new SkillContainer(maxSpells, spellWheel, mustEquip, improved, slots);
        }
    }
}
