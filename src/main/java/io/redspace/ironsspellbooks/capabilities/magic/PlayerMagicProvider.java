package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class PlayerMagicProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static Capability<MagicData> PLAYER_MAGIC = CapabilityManager.get(new CapabilityToken<>() {
    });

    private MagicData playerMagicData = null;
    private final LazyOptional<MagicData> opt = LazyOptional.of(this::createPlayerMagicData);
    private ServerPlayer serverPlayer;

    public PlayerMagicProvider(ServerPlayer serverPlayer) {
        this.serverPlayer = serverPlayer;
    }

    @Nonnull
    private MagicData createPlayerMagicData() {
        if (playerMagicData == null) {
            playerMagicData = new MagicData(serverPlayer);
        }
        return playerMagicData;
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
        createPlayerMagicData().saveNBTData(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerMagicData().loadNBTData(nbt);
    }
}
