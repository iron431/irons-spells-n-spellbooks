package com.example.testmod.capabilities.spellbook;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
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

    public SpellBookDataProvider() {
        getOrCreateSpellbookData();
    }

    @Nonnull
    private SpellBookData getOrCreateSpellbookData() {
        //TestMod.LOGGER.debug("SBDP.getOrCreateSpellbookData");
        if (spellBookData == null) {
            spellBookData = new SpellBookData(5);
        }
        return spellBookData;
    }

    @Nonnull
    public SpellBookData getOrCreateSpellbookData(int spellSlots) {
        //TestMod.LOGGER.debug("SBDP.getOrCreateSpellbookData enter: {}", spellSlots);
        if (spellBookData == null) {
            //TestMod.LOGGER.debug("SBDP.getOrCreateSpellbookData create hydrated SpellBookData: {} ", spellSlots);
            spellBookData = new SpellBookData(spellSlots);
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
