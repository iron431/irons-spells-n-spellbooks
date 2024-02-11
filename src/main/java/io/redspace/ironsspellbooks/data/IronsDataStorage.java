package io.redspace.ironsspellbooks.data;

import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.effect.guiding_bolt.GuidingBoltManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.jetbrains.annotations.NotNull;

public class IronsDataStorage extends SavedData {
    public static IronsDataStorage INSTANCE;

    public static void init(DimensionDataStorage dimensionDataStorage) {
        if (dimensionDataStorage != null) {
            IronsDataStorage.INSTANCE = dimensionDataStorage.computeIfAbsent(
                    IronsDataStorage::load,
                    IronsDataStorage::new,
                    "irons_spellbooks_data");
        }
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag pCompoundTag) {
        var tag = new CompoundTag();
        tag.put("GuidingBoltManager", GuidingBoltManager.INSTANCE.serializeNBT());
        tag.put("PortalManager", PortalManager.INSTANCE.serializeNBT());
        return tag;
    }

    public static IronsDataStorage load(CompoundTag tag) {
        if (tag.contains("GuidingBoltManager", Tag.TAG_COMPOUND)) {
            GuidingBoltManager.INSTANCE.deserializeNBT(tag.getCompound("GuidingBoltManager"));
        }
        if (tag.contains("PortalManager", Tag.TAG_COMPOUND)) {
            PortalManager.INSTANCE.deserializeNBT(tag.getCompound("PortalManager"));
        }

        return new IronsDataStorage();
    }
}
