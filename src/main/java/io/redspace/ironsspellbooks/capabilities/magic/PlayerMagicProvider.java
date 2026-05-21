package io.redspace.ironsspellbooks.capabilities.magic;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerMagicProvider implements IAttachmentSerializer<CompoundTag, MagicData> {
    @Override
    public @NotNull MagicData read(@NotNull IAttachmentHolder holder, @NotNull CompoundTag tag, HolderLookup.@NotNull Provider provider) {
        if (!(holder instanceof Entity entity)) {
            throw new IllegalArgumentException("MagicData does not support non-entity attachment holders");
        }
        MagicData magicData = new MagicData(entity);
        magicData.loadNBTData(tag, provider);
        return magicData;
    }

    @Override
    public @Nullable CompoundTag write(MagicData attachment, HolderLookup.@NotNull Provider provider) {
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
