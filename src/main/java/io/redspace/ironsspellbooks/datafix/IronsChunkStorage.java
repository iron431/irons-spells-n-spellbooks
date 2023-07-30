package io.redspace.ironsspellbooks.datafix;

import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Codec;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.levelgen.structure.LegacyStructureDataHandler;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.Supplier;

public class IronsChunkStorage extends ChunkStorage {
    private volatile LegacyStructureDataHandler legacyStructureHandler;

    public IronsChunkStorage(Path pRegionFolder, DataFixer pFixerUpper, boolean pSync) {
        super(pRegionFolder, pFixerUpper, pSync);
    }

    @Override
    public CompoundTag upgradeChunkTag(ResourceKey<Level> pLevelKey, Supplier<DimensionDataStorage> pStorage, CompoundTag pChunkData, Optional<ResourceKey<Codec<? extends ChunkGenerator>>> pChunkGeneratorKey) {
        int i = 1;

        pChunkData = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, pChunkData, i, 1493);
        if (pChunkData.getCompound("Level").getBoolean("hasLegacyStructureData")) {
            LegacyStructureDataHandler legacystructuredatahandler = this.getLegacyStructureHandler(pLevelKey, pStorage);
            pChunkData = legacystructuredatahandler.updateFromLegacy(pChunkData);
        }

        injectDatafixingContext(pChunkData, pLevelKey, pChunkGeneratorKey);
        pChunkData = NbtUtils.update(this.fixerUpper, DataFixTypes.CHUNK, pChunkData, Math.max(1493, i));

//        if (i < SharedConstants.getCurrentVersion().getWorldVersion()) {
//            pChunkData.putInt("DataVersion", SharedConstants.getCurrentVersion().getWorldVersion());
//        }

        pChunkData.remove("__context");
        return pChunkData;
    }

    private LegacyStructureDataHandler getLegacyStructureHandler(ResourceKey<Level> p_223449_, Supplier<DimensionDataStorage> p_223450_) {
        LegacyStructureDataHandler legacystructuredatahandler = this.legacyStructureHandler;
        if (legacystructuredatahandler == null) {
            synchronized(this) {
                legacystructuredatahandler = this.legacyStructureHandler;
                if (legacystructuredatahandler == null) {
                    this.legacyStructureHandler = legacystructuredatahandler = LegacyStructureDataHandler.getLegacyStructureHandler(p_223449_, p_223450_.get());
                }
            }
        }

        return legacystructuredatahandler;
    }
}
