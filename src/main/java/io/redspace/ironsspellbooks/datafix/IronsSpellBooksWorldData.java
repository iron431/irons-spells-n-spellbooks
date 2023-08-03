package io.redspace.ironsspellbooks.datafix;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class IronsSpellBooksWorldData extends SavedData {
    private int dataVersion;
    private boolean isUpgraded;

    public IronsSpellBooksWorldData() {
        dataVersion = 1;
        isUpgraded = false;
    }

    public IronsSpellBooksWorldData(int dataVersion, boolean upgraded) {
        this.dataVersion = dataVersion;
        this.isUpgraded = upgraded;
    }

    public int getDataVersion() {
        return dataVersion;
    }

    public boolean isUpgraded() {
        return isUpgraded;
    }

    public void setDataVersion(int dataVersion) {
        this.dataVersion = dataVersion;
        this.setDirty();
    }

    public void setUpgraded(boolean isUpgraded) {
        this.isUpgraded = isUpgraded;
        this.setDirty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag pCompoundTag) {
        var tag = new CompoundTag();
        tag.putInt("dataVersion", dataVersion);
        tag.putBoolean("isUpgraded", isUpgraded);
        return tag;
    }

    public static IronsSpellBooksWorldData load(CompoundTag tag) {
        int dataVersion = tag.getInt("dataVersion");
        boolean isUpgraded = tag.getBoolean("isUpgraded");
        return new IronsSpellBooksWorldData(dataVersion, isUpgraded);
    }
}