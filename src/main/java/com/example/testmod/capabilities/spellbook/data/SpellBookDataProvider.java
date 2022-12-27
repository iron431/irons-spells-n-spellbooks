package com.example.testmod.capabilities.spellbook.data;

import com.example.testmod.TestMod;
import com.example.testmod.util.Utils;
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

    private final int spellSlots;
    private ItemStack stack;

    public SpellBookDataProvider(int spellSlots, ItemStack stack, CompoundTag tag) {
        if (stack != null) {
            this.stack = stack;
        } else {
            this.stack = ItemStack.EMPTY;
        }

        this.spellSlots = spellSlots;

        getOrCreateSpellbookData();
    }

    @Nonnull
    private SpellBookData getOrCreateSpellbookData() {
        if (spellBookData == null) {
            spellBookData = new SpellBookData(this.spellSlots);
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
        return spellBookData.saveNBTData();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        spellBookData.loadNBTData(nbt);
    }
}
