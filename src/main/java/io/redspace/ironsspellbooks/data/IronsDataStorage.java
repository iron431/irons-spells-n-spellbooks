package io.redspace.ironsspellbooks.data;

import io.redspace.ironsspellbooks.effect.guiding_bolt.GuidingBoltManager;
import net.minecraft.nbt.CompoundTag;
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
        return tag;
    }

    public static IronsDataStorage load(CompoundTag tag) {

        if (tag.contains("GuidingBoltManager")) {
            GuidingBoltManager.INSTANCE.deserializeNBT((CompoundTag) tag.get("GuidingBoltManager"));
        }

        return new IronsDataStorage();
    }
}
