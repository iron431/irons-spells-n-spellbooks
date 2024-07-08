package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jetbrains.annotations.Nullable;


import javax.annotation.Nonnull;

public class PlayerMagicProvider implements IAttachmentSerializer<CompoundTag, MagicData> {
    @Override
    public MagicData read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider provider) {
        //Entities implement IIAttachmentHolder
        var magicData = holder instanceof ServerPlayer serverPlayer ? new MagicData(serverPlayer) : new MagicData(true);
        magicData.loadNBTData(tag, provider);
        return magicData;
    }

    @Override
    public @Nullable CompoundTag write(MagicData attachment, HolderLookup.Provider provider) {
        var tag = new CompoundTag();
        attachment.saveNBTData(tag, provider);
        return tag;
    }
}
//public class PlayerMagicProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {
//
//    public static Capability<MagicData> PLAYER_MAGIC = CapabilityManager.get(new CapabilityToken<>() {
//    });
//
//    private MagicData playerMagicData = null;
//    private final LazyOptional<MagicData> opt = LazyOptional.of(this::createPlayerMagicData);
//    private ServerPlayer serverPlayer;
//
//    public PlayerMagicProvider(ServerPlayer serverPlayer) {
//        this.serverPlayer = serverPlayer;
//    }
//
//    @Nonnull
//    private MagicData createPlayerMagicData() {
//        if (playerMagicData == null) {
//            playerMagicData = new MagicData(serverPlayer);
//        }
//        return playerMagicData;
//    }
//
//    @Nonnull
//    @Override
//    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
//        if (cap == PLAYER_MAGIC) {
//            return opt.cast();
//        }
//        return LazyOptional.empty();
//    }
//
//    @Nonnull
//    @Override
//    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
//        return getCapability(cap);
//    }
//
//    @Override
//    public CompoundTag serializeNBT() {
//        CompoundTag nbt = new CompoundTag();
//        createPlayerMagicData().saveNBTData(nbt);
//        return nbt;
//    }
//
//    @Override
//    public void deserializeNBT(CompoundTag nbt) {
//        createPlayerMagicData().loadNBTData(nbt);
//    }
//}
