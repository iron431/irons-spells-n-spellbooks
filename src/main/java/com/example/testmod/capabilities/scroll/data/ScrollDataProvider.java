package com.example.testmod.capabilities.scroll.data;

import com.example.testmod.TestMod;
import com.example.testmod.spells.SpellType;
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

public class ScrollDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<ScrollData> SCROLL_DATA = CapabilityManager.get(new CapabilityToken<>(){});

    private ScrollData scrollData = null;
    private final LazyOptional<ScrollData> opt = LazyOptional.of(this::getOrCreateScrollData);

//    public ScrollDataProvider() {
//        TestMod.LOGGER.debug("SDP.ScrollDataProvider.0");
//    }
//
//    public ScrollDataProvider(SpellType spellType, int spellLevel) {
//        TestMod.LOGGER.debug("SDP.ScrollDataProvider.1");
//        scrollData = new ScrollData(spellType, spellLevel);
//    }
//
//    public ScrollDataProvider(CompoundTag tag) {
//        TestMod.LOGGER.debug("SDP.ScrollDataProvider.2");
//        if (tag != null && !tag.isEmpty()) {
//            scrollData = new ScrollData(tag);
//        } else {
//            scrollData = new ScrollData(SpellType.NONE_SPELL, 0);
//        }
//
//    }
//    public SpellType getSpellType() {
//        return scrollData.getSpell().getSpellType();
//    }
//
//    public int getLevel() {
//        return scrollData.getLevel();
//    }

    @Nonnull
    private ScrollData getOrCreateScrollData() {
        TestMod.LOGGER.debug("SDP.getOrCreateScrollData");
        if (scrollData == null) {
            scrollData = new ScrollData(SpellType.NONE_SPELL, 0);
        }
        return scrollData;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        TestMod.LOGGER.debug("SDP.getCapability.1");
        if (cap == SCROLL_DATA) {
            return opt.cast();
        }
        return LazyOptional.empty();
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        TestMod.LOGGER.debug("SDP.getCapability.2");
        return getCapability(cap);
    }

    @Override
    public CompoundTag serializeNBT() {
        TestMod.LOGGER.debug("SDP.serializeNBT");
        return scrollData.saveNBTData();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        TestMod.LOGGER.debug("SDP.deserializeNBT");
        scrollData.loadNBTData(nbt);
    }
}
