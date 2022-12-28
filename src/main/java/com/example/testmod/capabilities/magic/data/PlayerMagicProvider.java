package com.example.testmod.capabilities.magic.data;

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

public class PlayerMagicProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<PlayerMagicData> PLAYER_MAGIC = CapabilityManager.get(new CapabilityToken<>(){});

    private PlayerMagicData playerMana = null;
    private final LazyOptional<PlayerMagicData> opt = LazyOptional.of(this::createPlayerMana);

    @Nonnull
    private PlayerMagicData createPlayerMana() {
        if (playerMana == null) {
            playerMana = new PlayerMagicData();
        }
        return playerMana;
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == PLAYER_MAGIC) {
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
        CompoundTag nbt = new CompoundTag();
        createPlayerMana().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerMana().loadNBTData(nbt);
    }
}
