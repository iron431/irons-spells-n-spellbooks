package io.redspace.skillcasting.data.skill;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.skillcasting.data.AbstractSkill;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    public static final Codec<ISkillContainer> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf(MAX_SLOTS).forGetter(ISkillContainer::getMaxSkillCount),
            Codec.BOOL.fieldOf(SPELL_WHEEL).forGetter(ISkillContainer::isSkillWheel),
            Codec.BOOL.fieldOf(MUST_EQUIP).forGetter(ISkillContainer::mustEquip),
            Codec.list(SkillSlot.CODEC).fieldOf(SPELL_DATA).forGetter(ISkillContainer::getActiveSkills)
    ).apply(builder, SkillContainer::fromSerialized));

    public static final StreamCodec<RegistryFriendlyByteBuf, ISkillContainer> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, ISkillContainer::getMaxSkillCount,
            ByteBufCodecs.BOOL, ISkillContainer::isSkillWheel,
            ByteBufCodecs.BOOL, ISkillContainer::mustEquip,
            SkillSlot.SKILL_SLOT.apply(ByteBufCodecs.list()), ISkillContainer::getActiveSkills,
            SkillContainer::fromSerialized
    );

    private static ISkillContainer fromSerialized(int count, boolean wheel, boolean equip, List<SkillSlot> skills) {
        var container = new SkillContainer(count, wheel, equip);
        skills.forEach(slot -> container.slots[slot.index()] = slot);
        container.activeSlots = skills.size();
        return container;
    }

    protected SkillSlot[] slots;
    protected int maxSpells;
    protected int activeSlots;
    protected boolean spellWheel;
    protected boolean mustEquip;

    public SkillContainer(int maxSpells, boolean spellWheel, boolean mustEquip) {
        this.maxSpells = maxSpells;
        this.slots = new SkillSlot[this.maxSpells];
        this.spellWheel = spellWheel;
        this.mustEquip = mustEquip;
    }

    public SkillContainer(int maxSpells, boolean spellWheel, boolean mustEquip, SkillSlot[] slots) {
        this.maxSpells = maxSpells;
        this.slots = slots;
        this.spellWheel = spellWheel;
        this.mustEquip = mustEquip;
        this.activeSlots = (int) Arrays.stream(slots).filter(Objects::nonNull).count();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof SkillContainer o &&
                Arrays.equals(o.slots, this.slots) &&
                this.maxSpells == o.maxSpells &&
                this.activeSlots == o.activeSlots &&
                this.spellWheel == o.spellWheel &&
                this.mustEquip == o.mustEquip);
    }

    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(slots);
        hash = (hash * 31 + maxSpells) * 31 + activeSlots;
        hash *= 1000;
        hash += spellWheel ? 100 : 0;
        hash += mustEquip ? 10 : 0;
        return hash;
    }

    @Override
    public String toString() {
        return String.format(
                "SkillContainer[maxSpells:%d, activeSlots:%d, spellWheel:%s, mustEquip:%s, slots:%s]",
                maxSpells, activeSlots, spellWheel, mustEquip, Arrays.toString(slots));
    }

    @Override
    public int getMaxSkillCount() {
        return maxSpells;
    }

    @Override
    public int getActiveSkillCount() {
        return activeSlots;
    }

    @Override
    public boolean isEmpty() {
        return activeSlots == 0;
    }

    @Override
    public SkillSlot[] getAllSkills() {
        var result = new SkillSlot[maxSpells];
        if (maxSpells > 0) {
            System.arraycopy(slots, 0, result, 0, slots.length);
        }
        return result;
    }

    @Override
    public @NotNull List<SkillSlot> getActiveSkills() {
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
    public boolean isSkillWheel() {
        return spellWheel;
    }

    @Override
    public @Nullable SkillData getSkillAtIndex(int index) {
        if (index >= 0 && index < maxSpells) {
            var result = slots[index];
            if (result != null) {
                return result.skillData();
            }
        }
        return null;
    }

    @Override
    public int getIndexForSkill(AbstractSkill spell) {
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
            super(container.maxSpells, container.spellWheel, container.mustEquip, Arrays.copyOf(container.slots, container.slots.length));
        }

        @Override
        public void setMaxSpellCount(int maxSpells) {
            this.maxSpells = maxSpells;
            slots = Arrays.copyOf(slots, maxSpells);
        }

        @Override
        public boolean setSpellAtIndex(SkillData skillData, int index) {
            if (index > -1 && index < maxSpells && Arrays.stream(slots).noneMatch(s -> s != null && skillData.getHolder().equals(s.skillData().getHolder()))) {
                if(slots[index] == null){
                    activeSlots++;
                }
                slots[index] = SkillSlot.of(skillData, index);
                return true;
            }
            return false;
        }

        @Override
        public boolean addSpell(SkillData skillData) {
            return setSpellAtIndex(skillData, getNextAvailableIndex());
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
            return new SkillContainer(maxSpells, spellWheel, mustEquip, slots);
        }
    }
}
