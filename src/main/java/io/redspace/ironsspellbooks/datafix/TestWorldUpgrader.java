package io.redspace.ironsspellbooks.datafix;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;
import net.minecraft.ReportedException;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestWorldUpgrader {
    private static final Logger LOGGER = IronsSpellbooks.LOGGER;
    private static final ThreadFactory THREAD_FACTORY = (new ThreadFactoryBuilder()).setDaemon(true).build();
    private final WorldGenSettings worldGenSettings;
//    private final boolean eraseCache;
    private final LevelStorageSource.LevelStorageAccess levelStorage;
    private final Thread thread;
    private final DataFixer dataFixer;
    private volatile boolean running = true;
    private volatile boolean finished;
    private volatile float progress;
    private volatile int totalChunks;
    private volatile int converted;
    private volatile int skipped;
    private final Object2FloatMap<ResourceKey<Level>> progressMap = Object2FloatMaps.synchronize(new Object2FloatOpenCustomHashMap<>(Util.identityStrategy()));
    private volatile Component status = Component.translatable("optimizeWorld.stage.counting");
    private static final Pattern REGEX = Pattern.compile("^r\\.(-?[0-9]+)\\.(-?[0-9]+)\\.mca$");
    private final DimensionDataStorage overworldDataStorage;

    public TestWorldUpgrader(LevelStorageSource.LevelStorageAccess pLevelStorage, DataFixer pDataFixer, WorldGenSettings pWorldGenSettings/*, boolean pEraseCache*/) {
        this.worldGenSettings = pWorldGenSettings;
//        this.eraseCache = pEraseCache;
        this.dataFixer = pDataFixer;
        this.levelStorage = pLevelStorage;
        this.overworldDataStorage = new DimensionDataStorage(this.levelStorage.getDimensionPath(Level.OVERWORLD).resolve("data").toFile(), pDataFixer);
        this.thread = THREAD_FACTORY.newThread(this::work);
        this.thread.setUncaughtExceptionHandler((p_18825_, p_18826_) -> {
            LOGGER.error("Error upgrading world", p_18826_);
            this.status = Component.translatable("optimizeWorld.stage.failed");
            this.finished = true;
        });
        this.thread.start();
    }

    public void cancel() {
        this.running = false;

        try {
            this.thread.join();
        } catch (InterruptedException interruptedexception) {
        }

    }

    private void work() {
        this.totalChunks = 0;
        ImmutableMap.Builder<ResourceKey<Level>, ListIterator<ChunkPos>> levelToChunkPosIteratorBuilder = ImmutableMap.builder();
        ImmutableSet<ResourceKey<Level>> levelKeys = this.worldGenSettings.levels();

        for (ResourceKey<Level> resourcekey : levelKeys) {
            List<ChunkPos> chunkPositions = this.getAllChunkPos(resourcekey);
            levelToChunkPosIteratorBuilder.put(resourcekey, chunkPositions.listIterator());
            this.totalChunks += chunkPositions.size();
        }

        if (this.totalChunks == 0) {
            this.finished = true;
        } else {
            float f1 = (float) this.totalChunks;
            ImmutableMap<ResourceKey<Level>, ListIterator<ChunkPos>> levelToChunkPosIterator = levelToChunkPosIteratorBuilder.build();
            ImmutableMap.Builder<ResourceKey<Level>, ChunkStorage> levelToChunkStorageBuilder = ImmutableMap.builder();

            for (ResourceKey<Level> level : levelKeys) {
                Path path = this.levelStorage.getDimensionPath(level);
                levelToChunkStorageBuilder.put(level, new ChunkStorage(path.resolve("region"), this.dataFixer, true));
            }

            ImmutableMap<ResourceKey<Level>, ChunkStorage> levelToChunkStorage = levelToChunkStorageBuilder.build();
            long j = Util.getMillis();
            this.status = Component.translatable("optimizeWorld.stage.upgrading");

            while (this.running) {
                boolean flag = false;
                float f = 0.0F;

                for (ResourceKey<Level> levelKey : levelKeys) {
                    ListIterator<ChunkPos> chunkPosIterator = levelToChunkPosIterator.get(levelKey);
                    ChunkStorage chunkstorage = levelToChunkStorage.get(levelKey);
                    if (chunkPosIterator.hasNext()) {
                        ChunkPos currentChunkPos = chunkPosIterator.next();
                        boolean successfulConversion = false;

                        try {
                            CompoundTag currentChunkNbt = chunkstorage.read(currentChunkPos).join().orElse(null);
                            if (currentChunkNbt != null) {
                                int currentChunkVersion = ChunkStorage.getVersion(currentChunkNbt);
                                ChunkGenerator chunkgenerator = this.worldGenSettings.dimensions().get(WorldGenSettings.levelToLevelStem(levelKey)).generator();
//                                CompoundTag newChunkNbt = chunkstorage.upgradeChunkTag(levelKey, () -> {
//                                    return this.overworldDataStorage;
//                                }, currentChunkNbt, chunkgenerator.getTypeNameForDataFixer());
                                CompoundTag newChunkNbt = currentChunkNbt;
                                IronsSpellbooks.LOGGER.debug("TestWorlderUpgrader: editable chunk nbt of chunk {}: {}", new ChunkPos(newChunkNbt.getInt("xPos"), newChunkNbt.getInt("zPos")), newChunkNbt);
//                                //TODO: we won't be messing with chunk positions so we can probably remove this warning
//                                ChunkPos chunkpos1 = new ChunkPos(newChunkNbt.getInt("xPos"), newChunkNbt.getInt("zPos"));
//                                if (!chunkpos1.equals(currentChunkPos)) {
//                                    LOGGER.warn("Chunk {} has invalid position {}", currentChunkPos, chunkpos1);
//                                }

                                //TODO: replace with our own versioning
                                int versionToCompareTo = SharedConstants.getCurrentVersion().getWorldVersion();
                                boolean isCurrentChunkOutdated = currentChunkVersion < versionToCompareTo;
//                                if (this.eraseCache) {
//                                    //TODO: this is vanilla specific stuff. we can prob remove
//                                    isCurrentChunkOutdated = isCurrentChunkOutdated || newChunkNbt.contains("Heightmaps");
//                                    newChunkNbt.remove("Heightmaps");
//                                    isCurrentChunkOutdated = isCurrentChunkOutdated || newChunkNbt.contains("isLightOn");
//                                    newChunkNbt.remove("isLightOn");
//                                    ListTag listtag = newChunkNbt.getList("sections", 10);
//
//                                    for(int i = 0; i < listtag.size(); ++i) {
//                                        CompoundTag compoundtag2 = listtag.getCompound(i);
//                                        isCurrentChunkOutdated = isCurrentChunkOutdated || compoundtag2.contains("BlockLight");
//                                        compoundtag2.remove("BlockLight");
//                                        isCurrentChunkOutdated = isCurrentChunkOutdated || compoundtag2.contains("SkyLight");
//                                        compoundtag2.remove("SkyLight");
//                                    }
//                                }

                                if (isCurrentChunkOutdated) {
                                    chunkstorage.write(currentChunkPos, newChunkNbt);
                                    successfulConversion = true;
                                }
                                if (true) {
                                    IronsSpellbooks.LOGGER.debug("Auto returning for testing");
                                    this.cancel();
                                    return;
                                }
                            }
                        } catch (CompletionException | ReportedException reportedexception) {
                            Throwable throwable = reportedexception.getCause();
                            if (!(throwable instanceof IOException)) {
                                throw reportedexception;
                            }

                            LOGGER.error("Error upgrading chunk {}", currentChunkPos, throwable);
                        }

                        if (successfulConversion) {
                            ++this.converted;
                        } else {
                            ++this.skipped;
                        }
                        //TODO: what is this flag?
                        flag = true;
                    }

                    //TODO: this looks like a progress tracker to me
                    float f2 = (float) chunkPosIterator.nextIndex() / f1;
                    this.progressMap.put(levelKey, f2);
                    f += f2;
                }

                this.progress = f;
                if (!flag) {
                    this.running = false;
                }
            }

            this.status = Component.translatable("optimizeWorld.stage.finished");

            for (ChunkStorage chunkstorage1 : levelToChunkStorage.values()) {
                try {
                    chunkstorage1.close();
                } catch (IOException ioexception) {
                    LOGGER.error("Error upgrading chunk", (Throwable) ioexception);
                }
            }

            this.overworldDataStorage.save();
            j = Util.getMillis() - j;
            LOGGER.info("World optimizaton finished after {} ms", (long) j);
            this.finished = true;
        }
    }

    private List<ChunkPos> getAllChunkPos(ResourceKey<Level> p_18831_) {
        File file1 = this.levelStorage.getDimensionPath(p_18831_).toFile();
        File file2 = new File(file1, "region");
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

    public boolean isFinished() {
        return this.finished;
    }

    public ImmutableSet<ResourceKey<Level>> levels() {
        return this.worldGenSettings.levels();
    }

    public float dimensionProgress(ResourceKey<Level> p_18828_) {
        return this.progressMap.getFloat(p_18828_);
    }

    public float getProgress() {
        return this.progress;
    }

    public int getTotalChunks() {
        return this.totalChunks;
    }

    public int getConverted() {
        return this.converted;
    }

    public int getSkipped() {
        return this.skipped;
    }

    public Component getStatus() {
        return this.status;
    }
}
