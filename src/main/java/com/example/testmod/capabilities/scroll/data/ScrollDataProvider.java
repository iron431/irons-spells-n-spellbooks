package com.example.testmod.capabilities.scroll.data;

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

    public static Capability<ScrollData> SCROLL_DATA = CapabilityManager.get(new CapabilityToken<>() {
    });
    private final LazyOptional<ScrollData> opt = LazyOptional.of(this::getOrCreateScrollData);

    private ScrollData scrollData = null;
    private final SpellType spellType;
    private final int level;

    public ScrollDataProvider(SpellType spellType, int level) {
        this.spellType = spellType;
        this.level = level;
        getOrCreateScrollData();
    }

    public SpellType getSpellType() {
        return spellType;
    }

    public int getLevel() {
        return level;
    }

    public ScrollDataProvider(CompoundTag tag) {
        if (tag != null && !tag.isEmpty()) {
            scrollData = new ScrollData(tag);
            this.spellType = scrollData.getSpell().getSpellType();
            this.level = scrollData.getSpell().getLevel();
        } else {
            spellType = SpellType.NONE;
            level = 0;
            scrollData= new ScrollData(spellType, level);
        }
    }

    @Nonnull
    private ScrollData getOrCreateScrollData() {
        if (scrollData == null) {
            scrollData = new ScrollData(this.spellType, this.level);
        }
        return scrollData;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == SCROLL_DATA) {
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

        return scrollData.saveNBTData();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        scrollData.loadNBTData(nbt);
    }
}
