package io.redspace.ironsspellbooks.data;

import com.mojang.datafixers.DataFixerBuilder;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.effect.guiding_bolt.GuidingBoltManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;

public class IronsSpellBooksWorldData extends SavedData {
    public static IronsSpellBooksWorldData INSTANCE;
    private DimensionDataStorage overworldDataStorage;
    private int dataVersion;

    public static void init(LevelStorageSource.LevelStorageAccess levelStorageAccess) {
        var dataFixer = new DataFixerBuilder(1).buildUnoptimized();
        var file = levelStorageAccess.getDimensionPath(Level.OVERWORLD).resolve("data").toFile();

        try {
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {

        }

        var overworldDataStorage = new DimensionDataStorage(file, dataFixer);

        IronsSpellBooksWorldData.INSTANCE = overworldDataStorage.computeIfAbsent(
                IronsSpellBooksWorldData::load,
                IronsSpellBooksWorldData::new,
                IronsSpellbooks.MODID);

        IronsSpellBooksWorldData.INSTANCE.overworldDataStorage = overworldDataStorage;

    }

    public IronsSpellBooksWorldData() {
        dataVersion = 0;
    }

    public IronsSpellBooksWorldData(int dataVersion) {
        this.dataVersion = dataVersion;
    }

    public int getDataVersion() {
        return dataVersion;
    }

    public void save(){
        overworldDataStorage.save();
    }

    public void setDataVersion(int dataVersion) {
        this.dataVersion = dataVersion;
        setDirty();
        overworldDataStorage.save();
    }

    @Override
    public @NotNull CompoundTag save(@NotNull CompoundTag pCompoundTag) {
        var tag = new CompoundTag();
        tag.putInt("dataVersion", dataVersion);
        tag.put("GuidingBoltManager", GuidingBoltManager.INSTANCE.serializeNBT());
        return tag;
    }

    public static IronsSpellBooksWorldData load(CompoundTag tag) {
        int dataVersion = tag.getInt("dataVersion");

        if (tag.contains("GuidingBoltManager")) {
            GuidingBoltManager.INSTANCE.deserializeNBT((CompoundTag) tag.get("GuidingBoltManager"));
        }

        return new IronsSpellBooksWorldData(dataVersion);
    }
}