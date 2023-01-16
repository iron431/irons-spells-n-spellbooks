package com.example.testmod.capabilities.scroll;

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

    public ScrollDataProvider(){}

    public static Capability<ScrollData> SCROLL_DATA = CapabilityManager.get(new CapabilityToken<>() {
    });

    private ScrollData scrollData = null;
    private final LazyOptional<ScrollData> opt = LazyOptional.of(this::getOrCreateScrollData);

    @Nonnull
    public ScrollData getOrCreateScrollData() {
        //TestMod.LOGGER.debug("SDP.getOrCreateScrollData");
        if (scrollData == null) {
            //TestMod.LOGGER.debug("SDP.getOrCreateScrollData create blank ScrollData");
            scrollData = new ScrollData(SpellType.NONE_SPELL, 0);
        }
        return scrollData;
    }

    @Nonnull
    public ScrollData getOrCreateScrollData(SpellType spellType, int level) {
        //TestMod.LOGGER.debug("SDP.getOrCreateScrollData {} {}", spellType.getValue(), level);
        if (scrollData == null) {
            //TestMod.LOGGER.debug("SDP.getOrCreateScrollData create hydrated ScrollData {} {}", spellType.getValue(), level);
            scrollData = new ScrollData(spellType, level);
        }
        return scrollData;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        //TestMod.LOGGER.debug("SDP.getCapability.1");
        if (cap == SCROLL_DATA) {
            //TestMod.LOGGER.debug("SDP.getCapability.2");
            return opt.cast();
        }
        //TestMod.LOGGER.debug("SDP.getCapability.3");
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return getOrCreateScrollData().saveNBTData();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        getOrCreateScrollData().loadNBTData(nbt);
    }
}
