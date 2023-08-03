package io.redspace.ironsspellbooks.datafix;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class IronsWorldUpgrader {
    public static final String REGION_FOLDER = "region";
    public static final String ENTITY_FOLDER = "entities";
    private final WorldGenSettings worldGenSettings;
    private final LevelStorageSource.LevelStorageAccess levelStorage;
    private final DataFixer dataFixer;
    private int converted;
    private int skipped;
    private int fixes;
    private boolean running;
    private final Object2FloatMap<ResourceKey<Level>> progressMap = Object2FloatMaps.synchronize(new Object2FloatOpenCustomHashMap<>(Util.identityStrategy()));
    private static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
    private final DimensionDataStorage overworldDataStorage;

    /*
       DimensionDataStorage storage = level.getServer().overworld().getDataStorage();
       magicManager = storage.computeIfAbsent(MagicManager::new, MagicManager::new, MAGIC_MANAGER);
     */

    public IronsWorldUpgrader(LevelStorageSource.LevelStorageAccess pLevelStorage, DataFixer pDataFixer, WorldGenSettings pWorldGenSettings) {
        //TODO: need to tag the level as upgraded so we don't run this every time we load it (DimensionDataStorage)
        this.worldGenSettings = pWorldGenSettings;
        this.dataFixer = pDataFixer;
        this.levelStorage = pLevelStorage;
        this.overworldDataStorage = new DimensionDataStorage(this.levelStorage.getDimensionPath(Level.OVERWORLD).resolve("data").toFile(), pDataFixer);
    }

    public void runUpgrade() {
        //TODO: change this to check for the world already upgraded flag
        if (true) {
            IronsSpellbooks.LOGGER.info("IronsWorldUpgrader starting upgrade");
            long millis = Util.getMillis();
            doWork(REGION_FOLDER, "block_entities");
            millis = Util.getMillis() - millis;
            IronsSpellbooks.LOGGER.info("IronsWorldUpgrader finished REGION_FOLDER after {} ms.  chunks updated:{} chunks skipped:{} tags fixed:{}", millis, this.converted, this.skipped, this.fixes);

            millis = Util.getMillis();
            doWork(ENTITY_FOLDER, null);
            millis = Util.getMillis() - millis;
            IronsSpellbooks.LOGGER.info("IronsWorldUpgrader finished ENTITY_FOLDER after {} ms.  chunks updated:{} chunks skipped:{} tags fixed:{}", millis, this.converted, this.skipped, this.fixes);

            millis = Util.getMillis();
            fixDimensionStorage();
            millis = Util.getMillis() - millis;
            IronsSpellbooks.LOGGER.info("IronsWorldUpgrader finished fixDimensionStorage after {} mx. tags fixed:{} ", millis, this.fixes);

            this.overworldDataStorage.save();
            IronsSpellbooks.LOGGER.info("IronsWorldUpgrader completed");
        }
    }

    private void fixDimensionStorage() {
        running = true;
        converted = 0;
        skipped = 0;
        fixes = 0;

        worldGenSettings.levels().stream().map(resourceKey -> {
            return this.levelStorage.getDimensionPath(resourceKey).resolve("data").toFile();
        }).forEach(dir -> {
            var files = dir.listFiles();
            if (files != null) {
                Arrays.stream(files).toList().forEach(file -> {
                    try {
                        var compoundTag = NbtIo.readCompressed(file);
                        var ironsTraverser = new IronsTagTraverser();
                        ironsTraverser.visit(compoundTag);
                        fixes += ironsTraverser.totalChanges();
                    } catch (Exception exception) {
                        IronsSpellbooks.LOGGER.debug("IronsWorldUpgrader: fixDimensionStorage error: {}", exception.getMessage());
                    }
                });
            }
        });
    }

    private void doWork(String regionFolder, String filterTag) {
        running = true;
        converted = 0;
        skipped = 0;
        fixes = 0;
        int totalChunks = 0;

        ImmutableMap.Builder<ResourceKey<Level>, ListIterator<ChunkPos>> builder = ImmutableMap.builder();
        ImmutableSet<ResourceKey<Level>> immutableset = this.worldGenSettings.levels();

        for (ResourceKey<Level> resourcekey : immutableset) {
            List<ChunkPos> list = this.getAllChunkPos(resourcekey, regionFolder);
            builder.put(resourcekey, list.listIterator());
            totalChunks += list.size();
        }

        if (totalChunks > 0) {
            ImmutableMap<ResourceKey<Level>, ListIterator<ChunkPos>> immutablemap = builder.build();
            ImmutableMap.Builder<ResourceKey<Level>, ChunkStorage> builder1 = ImmutableMap.builder();

            for (ResourceKey<Level> resourcekey1 : immutableset) {
                Path path = this.levelStorage.getDimensionPath(resourcekey1);
                builder1.put(resourcekey1, new ChunkStorage(path.resolve(regionFolder), this.dataFixer, true));
            }

            ImmutableMap<ResourceKey<Level>, ChunkStorage> immutablemap1 = builder1.build();
            while (this.running) {
                boolean processedItem = false;

                for (ResourceKey<Level> resourcekey2 : immutableset) {
                    ListIterator<ChunkPos> listiterator = immutablemap.get(resourcekey2);
                    ChunkStorage chunkstorage = immutablemap1.get(resourcekey2);
                    if (listiterator.hasNext()) {
                        ChunkPos chunkpos = listiterator.next();
                        boolean updated = false;

                        try {
                            CompoundTag chunkDataTag = chunkstorage.read(chunkpos).join().orElse(null);
                            if (chunkDataTag != null) {
                                ListTag blockEntitiesTag;

                                if (filterTag != null) {
                                    blockEntitiesTag = (ListTag) chunkDataTag.get(filterTag);
                                } else {
                                    blockEntitiesTag = new ListTag();
                                    blockEntitiesTag.add(chunkDataTag);
                                }

                                var ironsTagTraverser = new IronsTagTraverser();
                                ironsTagTraverser.visit(blockEntitiesTag);
                                if (ironsTagTraverser.changesMade()) {
                                    chunkstorage.write(chunkpos, chunkDataTag);
                                    this.fixes = ironsTagTraverser.totalChanges();
                                    updated = true;
                                }
                            }
                        } catch (Exception exception) {
                            IronsSpellbooks.LOGGER.error("IronsWorldUpgrader: Error upgrading chunk {}, {}", chunkpos, exception.getMessage());
                        }

                        if (updated) {
                            ++this.converted;
                        } else {
                            ++this.skipped;
                        }

                        processedItem = true;
                    }
                }

                if (!processedItem) {
                    this.running = false;
                }
            }

            for (ChunkStorage chunkstorage1 : immutablemap1.values()) {
                try {
                    chunkstorage1.close();
                } catch (IOException ioexception) {
                    IronsSpellbooks.LOGGER.error("IronsWorldUpgrader: Error closing chunk storage: {}", ioexception.getMessage());
                }
            }
        }
    }

    private List<ChunkPos> getAllChunkPos(ResourceKey<Level> p_18831_, String folder) {
        File file1 = this.levelStorage.getDimensionPath(p_18831_).toFile();
        File file2 = new File(file1, folder);
        File[] afile = file2.listFiles((p_18822_, p_18823_) -> {
            return p_18823_.endsWith(".mca");
        });
        if (afile == null) {
            return ImmutableList.of();
        } else {
            List<ChunkPos> list = Lists.newArrayList();

            for (File file3 : afile) {
                Matcher matcher = REGEX.matcher(file3.getName());
                if (matcher.matches()) {
                    int i = Integer.parseInt(matcher.group(1)) << 5;
                    int j = Integer.parseInt(matcher.group(2)) << 5;

                    try {
                        RegionFile regionfile = new RegionFile(file3.toPath(), file2.toPath(), true);

                        try {
                            for (int k = 0; k < 32; ++k) {
                                for (int l = 0; l < 32; ++l) {
                                    ChunkPos chunkpos = new ChunkPos(k + i, l + j);
                                    if (regionfile.doesChunkExist(chunkpos)) {
                                        list.add(chunkpos);
                                    }
                                }
                            }
                        } catch (Throwable throwable1) {
                            try {
                                regionfile.close();
                            } catch (Throwable throwable) {
                                throwable1.addSuppressed(throwable);
                            }

                            throw throwable1;
                        }

                        regionfile.close();
                    } catch (Throwable throwable2) {
                    }
                }
            }

            return list;
        }
    }

    public ImmutableSet<ResourceKey<Level>> levels() {
        return this.worldGenSettings.levels();
    }
}