package io.redspace.ironsspellbooks.datafix;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.data.DataFixerStorage;
import io.redspace.ironsspellbooks.util.ByteHelper;
import net.minecraft.Util;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.RegistryLayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.storage.LevelStorageSource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class IronsWorldUpgrader {
    public int tempCount = 0;
    public static int IRONS_WORLD_DATA_VERSION = IronsDataVersions.ELDRITCH_SCHOOL_DATA_VERSION;
    final int REPORT_PROGRESS_MS = 5000;
    public static final byte[] INHABITED_TIME_MARKER = new byte[]{0x49, 0x6E, 0x68, 0x61, 0x62, 0x69, 0x74, 0x65, 0x64, 0x54, 0x69, 0x6D, 0x65};
    public static final String REGION_FOLDER = "region";
    public static final String ENTITY_FOLDER = "entities";
    private final LevelStorageSource.LevelStorageAccess levelStorage;
    private final DataFixer dataFixer;
    private int converted;
    private int skipped;
    private int fixes;
    private boolean running;
    private static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
    private Set<ResourceKey<Level>> levels = null;

    public IronsWorldUpgrader(LevelStorageSource.LevelStorageAccess pLevelStorage, LayeredRegistryAccess<RegistryLayer> registries) {
        this.levelStorage = pLevelStorage;

        try {
            levels = registries.compositeAccess()
                    .registryOrThrow(Registries.LEVEL_STEM)
                    .registryKeySet()
                    .stream()
                    .map(Registries::levelStemToLevel)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (Exception exception) {
            IronsSpellbooks.LOGGER.error("IronsWorldUpgrader. Failed to init levels. Cannot upgrade", exception);
        }

        this.dataFixer = new DataFixerBuilder(1).buildUnoptimized();

    }

    public boolean worldNeedsUpgrading() {
        return DataFixerStorage.INSTANCE.getDataVersion() < IRONS_WORLD_DATA_VERSION;
    }

    public void runUpgrade() {
        if (levels != null && worldNeedsUpgrading()) {
            IronsSpellbooks.LOGGER.info("IronsWorldUpgrader starting upgrade");

//            try {
//                IronsSpellbooks.LOGGER.info("IronsWorldUpgrader Attempting minecraft world backup (this can take long on large worlds)");
//                levelStorage.makeWorldBackup();
//                IronsSpellbooks.LOGGER.info("IronsWorldUpgrader Minecraft world backup complete.");
//            } catch (Exception exception) {
//                IronsSpellbooks.LOGGER.error("IronsWorldUpgrader Level Backup failed: {}", exception.getMessage());
//            }

            long millis = 0;

            IronsSpellbooks.LOGGER.info("IronsWorldUpgrader starting ENTITY_FOLDER");
            millis = Util.getMillis();
            doWork(ENTITY_FOLDER, null, false, false);
            millis = Util.getMillis() - millis;
            IronsSpellbooks.LOGGER.info("IronsWorldUpgrader finished ENTITY_FOLDER after {} ms.  chunks updated:{} chunks skipped:{} tags fixed:{}", millis, this.converted, this.skipped, this.fixes);

            IronsSpellbooks.LOGGER.info("IronsWorldUpgrader starting REGION_FOLDER (this will take a few minutes on huge worlds..");
            millis = Util.getMillis();
            doWork(REGION_FOLDER, "block_entities", true, true);
            millis = Util.getMillis() - millis;
            IronsSpellbooks.LOGGER.info("IronsWorldUpgrader finished REGION_FOLDER after {} ms.  chunks updated:{} chunks skipped:{} tags fixed:{}", millis, this.converted, this.skipped, this.fixes);

            IronsSpellbooks.LOGGER.info("IronsWorldUpgrader starting fixDimensionStorage");
            millis = Util.getMillis();
            fixDimensionStorage();
            millis = Util.getMillis() - millis;
            IronsSpellbooks.LOGGER.info("IronsWorldUpgrader finished fixDimensionStorage after {} ms. tags fixed:{} ", millis, this.fixes);

            int previousVersion = DataFixerStorage.INSTANCE.getDataVersion();
            DataFixerStorage.INSTANCE.setDataVersion(IRONS_WORLD_DATA_VERSION);
            IronsSpellbooks.LOGGER.info("IronsWorldUpgrader V{} -> V{} completed", previousVersion, IRONS_WORLD_DATA_VERSION);
        }
    }

    private void fixDimensionStorage() {
        running = true;
        converted = 0;
        skipped = 0;
        fixes = 0;

        levels.stream().map(resourceKey -> {
            return this.levelStorage.getDimensionPath(resourceKey).resolve("data").toFile();
        }).forEach(dir -> {
            var files = dir.listFiles();
            if (files != null) {
                Arrays.stream(files).toList().forEach(this::fixDimensionDataFile);
            }
        });
    }

    private void fixDimensionDataFile(File file) {
        var subFiles = file.listFiles();
        if (subFiles != null && subFiles.length > 0) {
            Arrays.stream(subFiles).forEach(this::fixDimensionDataFile);
        } else {
            try {
                var compoundTag = NbtIo.readCompressed(file);
                var ironsTraverser = new IronsTagTraverser();
                ironsTraverser.visit(compoundTag);

                if (ironsTraverser.changesMade()) {
                    NbtIo.writeCompressed(compoundTag, file);
                }

                fixes += ironsTraverser.totalChanges();
            } catch (Exception exception) {
                IronsSpellbooks.LOGGER.error("IronsWorldUpgrader FixDimensionStorage error: {}", exception.getMessage());
            }
        }
    }

    private boolean preScanChunkUpdateNeeded(ChunkStorage chunkStorage, ChunkPos chunkPos) throws Exception {
        var regionFile = chunkStorage.worker.storage.getRegionFile(chunkPos);
        var dataInputStream = regionFile.getChunkDataInputStream(chunkPos);

        try (dataInputStream) {
            if (dataInputStream == null) {
                return false;
            }

            //int markerPos = ByteHelper.indexOf(dataInputStream, INHABITED_TIME_MARKER);
            int markerPos = ByteHelper.indexOf(dataInputStream, new ParallelMatcher(DataFixerHelpers.DATA_MATCHER_TARGETS));
            if (markerPos == -1) {
                return true;
            }
            var inhabitedTime = dataInputStream.readLong();

            tempCount++;

            return inhabitedTime != 0;

        } catch (Exception ignored) {
        }

        return true;
    }

    private void doWork(String regionFolder, String filterTag, boolean preScan, boolean checkInhabitedTime) {
        running = true;
        converted = 0;
        skipped = 0;
        fixes = 0;
        long nextProgressReportMS = System.currentTimeMillis() + REPORT_PROGRESS_MS;
        int totalChunks = 0;

        ImmutableMap.Builder<ResourceKey<Level>, ListIterator<ChunkPos>> builder = ImmutableMap.builder();

        for (ResourceKey<Level> resourcekey : levels) {
            List<ChunkPos> list = this.getAllChunkPos(resourcekey, regionFolder);
            builder.put(resourcekey, list.listIterator());
            totalChunks += list.size();
        }

        if (totalChunks > 0) {
            ImmutableMap<ResourceKey<Level>, ListIterator<ChunkPos>> immutablemap = builder.build();
            ImmutableMap.Builder<ResourceKey<Level>, ChunkStorage> builder1 = ImmutableMap.builder();

            for (ResourceKey<Level> resourcekey1 : levels) {
                Path path = this.levelStorage.getDimensionPath(resourcekey1);
                builder1.put(resourcekey1, new ChunkStorage(path.resolve(regionFolder), this.dataFixer, true));
            }

            ImmutableMap<ResourceKey<Level>, ChunkStorage> immutablemap1 = builder1.build();
            while (this.running) {
                boolean processedItem = false;

                for (ResourceKey<Level> resourcekey2 : levels) {
                    ListIterator<ChunkPos> listiterator = immutablemap.get(resourcekey2);
                    ChunkStorage chunkstorage = immutablemap1.get(resourcekey2);
                    if (listiterator.hasNext()) {
                        ChunkPos chunkpos = listiterator.next();
                        boolean updated = false;

                        try {
                            if (!preScan || preScanChunkUpdateNeeded(chunkstorage, chunkpos)) {
                                CompoundTag chunkDataTag = chunkstorage.read(chunkpos).join().orElse(null);

                                if (chunkDataTag != null && (!checkInhabitedTime || chunkDataTag.getInt("InhabitedTime") != 0)) {
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
                                        this.fixes += ironsTagTraverser.totalChanges();
                                        updated = true;
                                    }
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

                        if (System.currentTimeMillis() > nextProgressReportMS) {
                            nextProgressReportMS = System.currentTimeMillis() + REPORT_PROGRESS_MS;
                            int chunksProcessed = this.converted + this.skipped;
                            IronsSpellbooks.LOGGER.info("IronsWorldUpgrader {} PROGRESS: {} of {} chunks complete ({}%)", regionFolder, chunksProcessed, totalChunks, String.format("%.2f", (chunksProcessed / (float) totalChunks) * 100));
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
}