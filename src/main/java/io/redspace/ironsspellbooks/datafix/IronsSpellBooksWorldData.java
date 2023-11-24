package io.redspace.ironsspellbooks.datafix;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

public class IronsSpellBooksWorldData extends SavedData {
    public static IronsSpellBooksWorldData INSTANCE;
    private int dataVersion;


    public IronsSpellBooksWorldData() {
        dataVersion = 0;
    }

    public IronsSpellBooksWorldData(int dataVersion) {
        this.dataVersion = dataVersion;
    }

    public int getDataVersion() {
        return dataVersion;
    }

    public void setDataVersion(int dataVersion) {
        this.dataVersion = dataVersion;
        this.setDirty();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag pCompoundTag) {
        var tag = new CompoundTag();
        tag.putInt("dataVersion", dataVersion);
        return tag;
    }

    public static IronsSpellBooksWorldData load(CompoundTag tag) {
        int dataVersion = tag.getInt("dataVersion");
        return new IronsSpellBooksWorldData(dataVersion);
    }
}