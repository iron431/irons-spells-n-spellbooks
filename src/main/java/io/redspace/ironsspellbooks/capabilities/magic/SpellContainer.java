package io.redspace.ironsspellbooks.capabilities.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.registries.ComponentRegistry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SpellContainer implements ISpellContainer {
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

    SpellSlot[] slots;
    int maxSpells = 0;
    int activeSlots = 0;
    boolean spellWheel = false;
    boolean mustEquip = true;
    boolean improved = false;


    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof SpellContainer o &&
                Arrays.equals(o.slots, this.slots) &&
                this.maxSpells == o.maxSpells &&
                this.activeSlots == o.activeSlots &&
                this.spellWheel == o.spellWheel &&
                this.mustEquip == o.mustEquip &&
                this.improved == o.improved
        );
    }

    @Override
    public int hashCode() {
        int hash = Arrays.hashCode(this.slots);
        hash = (hash * 31 + maxSpells) * 31 + activeSlots;
        hash *= 1000;
        hash += spellWheel ? 100 : 0;
        hash += mustEquip ? 10 : 0;
        hash += improved ? 1 : 0;
        return hash;
    }

    //Codec<List<SpellData>> SPELL_LIST_CODEC = Codec.list(SpellData.CODEC);
    public static final Codec<SpellSlot> SPELL_SLOT_CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.fieldOf(SPELL_ID).forGetter(data -> data.getSpell().getSpellResource()),
            Codec.INT.fieldOf(SLOT_INDEX).forGetter(SpellSlot::index),
            Codec.INT.fieldOf(SPELL_LEVEL).forGetter(SpellSlot::getLevel),
            Codec.BOOL.optionalFieldOf(SPELL_LOCKED, false).forGetter(SpellSlot::isLocked)
    ).apply(builder, (id, index, lvl, lock) -> SpellSlot.of(new SpellData(id, lvl, lock), index)));

    public static final Codec<ISpellContainer> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf(MAX_SLOTS).forGetter(ISpellContainer::getMaxSpellCount),
            Codec.BOOL.fieldOf(SPELL_WHEEL).forGetter(ISpellContainer::isSpellWheel),
            Codec.BOOL.fieldOf(MUST_EQUIP).forGetter(ISpellContainer::mustEquip),
            Codec.BOOL.optionalFieldOf(IMPROVED, false).forGetter(ISpellContainer::isImproved),
            Codec.list(SPELL_SLOT_CODEC).fieldOf(SPELL_DATA).forGetter(ISpellContainer::getActiveSpells)
    ).apply(builder, (count, wheel, equip, improved, spells) -> {
        var container = new SpellContainer(count, wheel, equip, improved);
        spells.forEach(slot -> container.slots[slot.index()] = slot);
        container.activeSlots = spells.size();
        return container;
    }));

    public static final StreamCodec<FriendlyByteBuf, ISpellContainer> STREAM_CODEC = StreamCodec.of((buf, container) -> {
        buf.writeInt(container.getMaxSpellCount());
        buf.writeBoolean(container.isSpellWheel());
        buf.writeBoolean(container.mustEquip());
        buf.writeBoolean(container.isImproved());
        var spells = container.getActiveSpells();
        int i = spells.size();
        buf.writeInt(i);
        for (int j = 0; j < i; j++) {
            var spell = spells.get(j);
            SpellData.writeToBuffer(buf, spell.spellData());
            buf.writeInt(spell.index());
        }
    }, (buf) -> {
        var count = buf.readInt();
        var wheel = buf.readBoolean();
        var equip = buf.readBoolean();
        var improved = buf.readBoolean();
        int i = buf.readInt();

        var container = new SpellContainer(count, wheel, equip, improved);
        for (int j = 0; j < i; j++) {
            var spell = new SpellSlot(SpellData.readFromBuffer(buf), buf.readInt());
            container.slots[spell.index()] = spell;
        }
        container.activeSlots = i;
        return container;
    });

    public SpellContainer() {
    }

    public SpellContainer(int maxSpells, boolean spellWheel, boolean mustEquip) {
        this(maxSpells, spellWheel, mustEquip, false);
    }

    public SpellContainer(int maxSpells, boolean spellWheel, boolean mustEquip, boolean improved) {
        this.maxSpells = maxSpells;
        this.slots = new SpellSlot[this.maxSpells];
        this.spellWheel = spellWheel;
        this.mustEquip = mustEquip;
        this.improved = improved;
    }

    public SpellContainer(int maxSpells, boolean spellWheel, boolean mustEquip, boolean improved, SpellSlot[] slots) {
        this.maxSpells = maxSpells;
        this.slots = slots;
        this.spellWheel = spellWheel;
        this.mustEquip = mustEquip;
        this.improved = improved;
        this.activeSlots = Arrays.stream(slots).filter(Objects::nonNull).toList().size();
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
    public SpellSlot[] getAllSpells() {
        var result = new SpellSlot[maxSpells];
        if (maxSpells > 0) {
            System.arraycopy(slots, 0, result, 0, slots.length);
        }
        return result;
    }

    @Override
    public @NotNull List<SpellSlot> getActiveSpells() {
        return Arrays.stream(this.slots).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public int getNextAvailableIndex() {
        return ArrayUtils.indexOf(this.slots, null);
    }

    @Override
    public boolean mustEquip() {
        return mustEquip;
    }

    @Override
    public boolean isImproved() {
        return improved;
    }

    @Override
    public boolean isSpellWheel() {
        return spellWheel;
    }

    @Override
    public @NotNull SpellData getSpellAtIndex(int index) {
        if (index >= 0 && index < maxSpells) {
            var result = slots[index];
            if (result != null) {
                return slots[index].spellData();
            }
        }
        return SpellData.EMPTY;
    }

    @Override
    public int getIndexForSpell(AbstractSpell spell) {
        for (int i = 0; i < maxSpells; i++) {
            var s = slots[i];

            if (s != null && s.getSpell().equals(spell)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ISpellContainerMutable mutableCopy() {
        return new Mutable(this);
    }

    public class Mutable extends SpellContainer implements ISpellContainerMutable {
//        private SpellSlot[] slots;
//        private int maxSpells = 0;
//        private int activeSlots = 0;
//        private boolean spellWheel = false;
//        private boolean mustEquip = true;
//        private boolean improved = false;

        public Mutable(SpellContainer spellContainer) {
            this.maxSpells = spellContainer.maxSpells;
            this.activeSlots = spellContainer.activeSlots;
            this.spellWheel = spellContainer.spellWheel;
            this.mustEquip = spellContainer.mustEquip;
            this.improved = spellContainer.improved;
            this.slots = Arrays.copyOf(spellContainer.slots, spellContainer.slots.length);
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
        public boolean addSpellAtIndex(AbstractSpell spell, int level, int index, boolean locked) {
            if (index > -1 && index < maxSpells &&
                    slots[index] == null &&
                    Arrays.stream(slots).noneMatch(s -> s != null && s.getSpell().equals(spell))) {
                slots[index] = SpellSlot.of(new SpellData(spell, level, locked), index);
                activeSlots++;
                return true;
            }
            return false;
        }

        @Override
        public boolean addSpell(AbstractSpell spell, int level, boolean locked) {
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
        public boolean removeSpell(AbstractSpell spell) {
            if (spell == null) {
                return false;
            }

            for (int i = 0; i < maxSpells; i++) {
                var spellData = slots[i];
                if (spellData != null && spell.equals(spellData.getSpell())) {
                    return removeSpellAtIndex(i);
                }
                break;
            }
            return false;
        }

        @Override
        public ISpellContainer toImmutable() {
            return new SpellContainer(this.maxSpells, this.spellWheel, this.mustEquip, this.improved, this.slots);
        }
    }
}
