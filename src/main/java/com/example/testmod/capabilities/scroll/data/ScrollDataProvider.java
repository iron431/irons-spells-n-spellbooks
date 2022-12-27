package com.example.testmod.capabilities.scroll.data;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.spellbook.data.SpellBookData;
import com.example.testmod.spells.SpellType;
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
import java.util.Objects;

public class ScrollDataProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<ScrollData> SCROLL_DATA = CapabilityManager.get(new CapabilityToken<>() {
    });
    private final LazyOptional<ScrollData> opt = LazyOptional.of(this::getOrCreateScrollData);

    private ScrollData scrollData = null;
    private ItemStack stack;
    private SpellType spellType;
    private int level;

    public ScrollDataProvider(SpellType spellType, int level, ItemStack stack, CompoundTag tag) {
        this.spellType = spellType;
        this.level = level;

        this.stack = Objects.requireNonNullElse(stack, ItemStack.EMPTY);

        getOrCreateScrollData();
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
