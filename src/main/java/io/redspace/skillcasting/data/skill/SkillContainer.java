package io.redspace.skillcasting.data.skill;

import com.mojang.serialization.Codec;
import io.redspace.skillcasting.data.selection.SkillSelectionPriority;
import io.redspace.skillcasting.data.AbstractSkill;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SkillContainer implements ISkillContainer {
    public static final String SKILL_DATA = "data";
    public static final String MAX_SLOTS = "size";
    public static final String MUST_EQUIP = "equip";
    public static final String SKILL_WHEEL = "wheel";

    public static final String SLOT_INDEX = "index";

    public static final Codec<ISkillContainer> CODEC = ISkillContainer.codec(SkillContainer::fromSerialized);

    public static final StreamCodec<RegistryFriendlyByteBuf, ISkillContainer> STREAM_CODEC = ISkillContainer.streamCodec(SkillContainer::fromSerialized);

    private static SkillContainer basic(int maxSkills, boolean skillWheel, boolean mustEquip, List<SkillSlot> skills) {
        return new SkillContainer(maxSkills, skillWheel, mustEquip, skills, SkillContainer::basic);
    }

    public static ISkillContainer fromSerialized(int count, boolean wheel, boolean equip, List<SkillSlot> skills) {
        return basic(count, wheel, equip, skills);
    }

    protected final ISkillContainer.Factory<? extends SkillContainer> factory;
    protected SkillSlot[] slots;
    protected int maxSkills;
    protected int activeSlots;
    protected boolean skillWheel;
    protected boolean mustEquip;

    private SkillContainer(int maxSkills, boolean skillWheel, boolean mustEquip) {
        this(maxSkills, skillWheel, mustEquip, List.of(), SkillContainer::basic);
    }

    private SkillContainer(int maxSkills, boolean skillWheel, boolean mustEquip, SkillSlot[] slots) {
        this(maxSkills, skillWheel, mustEquip, slots, SkillContainer::basic);
    }

    protected SkillContainer(int maxSkills, boolean skillWheel, boolean mustEquip, List<SkillSlot> skills, ISkillContainer.Factory<? extends SkillContainer> factory) {
        this.maxSkills = maxSkills;
        this.slots = new SkillSlot[maxSkills];
        this.skillWheel = skillWheel;
        this.mustEquip = mustEquip;
        this.factory = factory;
        skills.forEach(slot -> this.slots[slot.index()] = slot);
        this.activeSlots = skills.size();
    }

    protected SkillContainer(int maxSkills, boolean skillWheel, boolean mustEquip, SkillSlot[] slots, ISkillContainer.Factory<? extends SkillContainer> factory) {
        this.maxSkills = maxSkills;
        this.slots = slots;
        this.skillWheel = skillWheel;
        this.mustEquip = mustEquip;
        this.factory = factory;
        this.activeSlots = (int) Arrays.stream(slots).filter(Objects::nonNull).count();
    }

    public final SkillContainer copyWith(int maxSkills, boolean skillWheel, boolean mustEquip, SkillSlot[] slots) {
        return factory.create(maxSkills, skillWheel, mustEquip, Arrays.stream(slots).filter(Objects::nonNull).toList());
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof SkillContainer o &&
                getClass() == obj.getClass() &&
                Arrays.equals(o.slots, this.slots) &&
                this.maxSkills == o.maxSkills &&
                this.activeSlots == o.activeSlots &&
                this.skillWheel == o.skillWheel &&
                this.mustEquip == o.mustEquip);
    }

    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(slots);
        hash = hash * 31 + getClass().hashCode();
        hash = (hash * 31 + maxSkills) * 31 + activeSlots;
        hash *= 1000;
        hash += skillWheel ? 100 : 0;
        hash += mustEquip ? 10 : 0;
        return hash;
    }

    @Override
    public String toString() {
        return String.format(
                getClass().getSimpleName() + "[maxSkills:%d, activeSlots:%d, skillWheel:%s, mustEquip:%s, slots:%s]",
                maxSkills, activeSlots, skillWheel, mustEquip, Arrays.toString(slots));
    }

    @Override
    public int getMaxSkillCount() {
        return maxSkills;
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
        var result = new SkillSlot[maxSkills];
        if (maxSkills > 0) {
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
        return skillWheel;
    }

    @Override
    public @Nullable SkillData getSkillAtIndex(int index) {
        if (index >= 0 && index < maxSkills) {
            var result = slots[index];
            if (result != null) {
                return result.skillData();
            }
        }
        return null;
    }

    @Override
    public int getIndexForSkill(AbstractSkill skill) {
        for (int i = 0; i < maxSkills; i++) {
            var slot = slots[i];
            if (slot != null && skill.equals(slot.getSkill())) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Mutable<? extends SkillContainer> mutableCopy() {
        return new Mutable<>(this);
    }

    public static class Mutable<C extends SkillContainer> extends SkillContainer implements ISkillContainerMutable {
        private final C origin;

        public Mutable(C container) {
            super(container.maxSkills, container.skillWheel, container.mustEquip, Arrays.copyOf(container.slots, container.slots.length), container.factory);
            this.origin = container;
        }

        @Override
        public SkillSelectionPriority getSelectionPriority() {
            return origin.getSelectionPriority();
        }

        @Override
        public void setMaxSkillCount(int maxSkills) {
            this.maxSkills = maxSkills;
            slots = Arrays.copyOf(slots, maxSkills);
            activeSlots = (int) Arrays.stream(slots).filter(Objects::nonNull).count();
        }

        @Override
        public boolean setSkillAtIndex(SkillData skillData, int index) {
            if (index > -1 && index < maxSkills && Arrays.stream(slots).noneMatch(s -> s != null && skillData.getHolder().equals(s.skillData().getHolder()))) {
                if(slots[index] == null){
                    activeSlots++;
                }
                slots[index] = SkillSlot.of(skillData, index);
                return true;
            }
            return false;
        }

        @Override
        public boolean addSkill(SkillData skillData) {
            return setSkillAtIndex(skillData, getNextAvailableIndex());
        }

        @Override
        public boolean removeSkillAtIndex(int index) {
            if (index > -1 && index < maxSkills && slots[index] != null) {
                slots[index] = null;
                activeSlots--;
                return true;
            }
            return false;
        }

        @Override
        public boolean removeSkill(AbstractSkill skill) {
            if (skill == null) {
                return false;
            }
            for (int i = 0; i < maxSkills; i++) {
                var slot = slots[i];
                if (slot != null && skill.equals(slot.getSkill())) {
                    return removeSkillAtIndex(i);
                }
            }
            return false;
        }

        @Override
        @SuppressWarnings("unchecked")
        public C toImmutable() {
            return (C) copyWith(maxSkills, skillWheel, mustEquip, slots);
        }
    }
}
