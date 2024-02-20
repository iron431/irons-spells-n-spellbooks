package io.redspace.ironsspellbooks.data;

import com.mojang.datafixers.DataFixerBuilder;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PortalManager;
import io.redspace.ironsspellbooks.effect.guiding_bolt.GuidingBoltManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;

public class DataFixerStorage extends SavedData {
    public static DataFixerStorage INSTANCE;
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

        DataFixerStorage.INSTANCE = overworldDataStorage.computeIfAbsent(
                DataFixerStorage::load,
                DataFixerStorage::new,
                IronsSpellbooks.MODID);

        DataFixerStorage.INSTANCE.overworldDataStorage = overworldDataStorage;
    }

    public DataFixerStorage() {
        dataVersion = 0;
    }

    public DataFixerStorage(int dataVersion) {
        this.dataVersion = dataVersion;
    }

    public int getDataVersion() {
        return dataVersion;
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
        return tag;
    }

    public static DataFixerStorage load(CompoundTag tag) {
        int dataVersion = tag.getInt("dataVersion");
        return new DataFixerStorage(dataVersion);
    }
}