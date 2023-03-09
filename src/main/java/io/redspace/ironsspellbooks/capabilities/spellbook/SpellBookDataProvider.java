package io.redspace.ironsspellbooks.capabilities.spellbook;

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
    private final LazyOptional<SpellBookData> opt = LazyOptional.of(this::getOrCreateSpellBookData);

    private SpellBookData spellBookData = null;

    @Nonnull
    private SpellBookData getOrCreateSpellBookData() {
        //irons_spellbooks.LOGGER.debug("SBDP.getOrCreateSpellbookData");
        if (spellBookData == null) {
            spellBookData = new SpellBookData(5);
        }
        return spellBookData;
    }

    @Nonnull
    public SpellBookData getOrCreateSpellBookData(int spellSlots) {
        //irons_spellbooks.LOGGER.debug("SpellBookDataProvider.getOrCreateSpellbookData: {} ", spellBookData == null);

        if (spellBookData == null) {
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
        return getOrCreateSpellBookData().saveNBTData();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getOrCreateSpellBookData().loadNBTData(nbt);
    }
}
