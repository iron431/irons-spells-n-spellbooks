package com.example.testmod.capabilities;

import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ManaProvider implements ICapabilityProvider {
    public static Capability<Mana> playerManaCap = CapabilityManager.get(new CapabilityToken<Mana>() {});

    private Mana playerMana = null;
    private final LazyOptional<Mana> lazyOptMana = LazyOptional.of(this::createPlayerMana);

    @Nonnull
    private Mana createPlayerMana(){
        if(playerMana==null)
            playerMana = new Mana(Minecraft.getInstance().player);
        return playerMana;
    }
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap){
        if(cap == playerManaCap)
            return lazyOptMana.cast();
        return LazyOptional.empty();
    }
    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side){
        return getCapability(cap);
    }


}
