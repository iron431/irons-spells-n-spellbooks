package com.example.testmod.capabilities.spellbook.data;

import com.example.testmod.TestMod;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SpellBookDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<SpellBookData> SPELL_BOOK_DATA = CapabilityManager.get(new CapabilityToken<>() {
    });
    private final LazyOptional<SpellBookData> opt = LazyOptional.of(this::getOrCreateSpellbookData);

    private SpellBookData spellBookData = null;

    private final SpellBookTypes spellBookType;
    private final int spellSlots;
    private ItemStack stack;
    private CompoundTag tag;

    public SpellBookDataProvider(SpellBookTypes spellBookType, int spellSlots, ItemStack stack, CompoundTag tag) {
        this.spellBookType = spellBookType;

        if (stack != null) {
            this.stack = stack;
        } else {
            this.stack = ItemStack.EMPTY;
        }

        if (tag != null) {
            //TODO: remove this at some point
            TestMod.LOGGER.info(tag.toString());
            this.tag = tag;
        } else {
            this.tag = new CompoundTag();
        }
        this.spellSlots = spellSlots;

        getOrCreateSpellbookData();
    }

    @Nonnull
    private SpellBookData getOrCreateSpellbookData() {
        if (spellBookData == null) {
            spellBookData = new SpellBookData();
        }
        return spellBookData;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == SPELL_BOOK_DATA) {
            return opt.cast();
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return getCapability(cap);
    }

    @Override
    public CompoundTag serializeNBT() {
        return getOrCreateSpellbookData().saveNBTData();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getOrCreateSpellbookData().loadNBTData(nbt);
    }
}
