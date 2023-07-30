package io.redspace.ironsspellbooks.datafix;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.spell.SpellData;
import io.redspace.ironsspellbooks.capabilities.spellbook.SpellBookData;
import io.redspace.ironsspellbooks.item.SpellBook;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatMaps;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenCustomHashMap;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.slf4j.Logger;

public class IronsWorldUpgrader {
    private final WorldGenSettings worldGenSettings;
    private final LevelStorageSource.LevelStorageAccess levelStorage;
    private final DataFixer dataFixer;
    private volatile boolean finished;
    private volatile int totalChunks;
    private volatile int converted;
    private volatile int skipped;
    private volatile boolean running;
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
//        DataFixerBuilder dataFixerBuilder = new DataFixerBuilder(1);
//        var schema = dataFixerBuilder.addSchema(100, IronsSchema::new);
//        dataFixerBuilder.addFixer(new ItemStackScrollFix(schema));
//        this.dataFixer = dataFixerBuilder.buildOptimized(Util.bootstrapExecutor());
        this.levelStorage = pLevelStorage;
        this.overworldDataStorage = new DimensionDataStorage(this.levelStorage.getDimensionPath(Level.OVERWORLD).resolve("data").toFile(), pDataFixer);
    }

    private boolean upgradeBlockEntities(CompoundTag chunkData) {
        AtomicBoolean updated = new AtomicBoolean(false);
        ListTag tag = (ListTag) chunkData.get("block_entities");

        tag.stream().filter(t -> {
            var ct = (CompoundTag) t;
            var item = ct.getString("id");
            return "minecraft:chest".equals(item);
        }).map(chest -> {
            var ct = (CompoundTag) chest;
            return ct.getList("Items", Tag.TAG_COMPOUND);
        }).forEach(itemList -> {
            itemList.forEach(item -> {
                var compoundItemTag = (CompoundTag) item;
                var itemTag = (CompoundTag) compoundItemTag.get("tag");

                if (itemTag != null) {
                    var spellTag = (CompoundTag) itemTag.get(SpellData.ISB_SPELL);
                    if (spellTag != null && spellTag.contains(SpellData.LEGACY_SPELL_TYPE)) {
                        DataFixerHelpers.fixScrollData(spellTag);
                        updated.set(true);
                    }

                    var spellBookTag = (CompoundTag) itemTag.get(SpellBookData.ISB_SPELLBOOK);
                    if (spellBookTag != null) {
                        ListTag listTagSpells = (ListTag) spellBookTag.get(SpellBookData.SPELLS);
                        if (listTagSpells != null && !listTagSpells.isEmpty()) {
                            if (((CompoundTag) listTagSpells.get(0)).contains(SpellBookData.LEGACY_ID)) {
                                DataFixerHelpers.fixSpellbookData(listTagSpells);
                                updated.set(true);
                            }
                        }
                    }
                }

//                var id = compoundItemTag.getString("id");
//
//                if ("irons_spellbooks:scroll".equals(id)) {
//                    var itemTag = (CompoundTag) compoundItemTag.get("tag");
//                    if (itemTag != null) {
//                        var spellTag = (CompoundTag) itemTag.get(SpellData.ISB_SPELL);
//                        if (spellTag != null && spellTag.contains(SpellData.LEGACY_SPELL_TYPE)) {
//                            DataFixerHelpers.fixScrollData(spellTag);
//                            updated.set(true);
//                        }
//                    }
//                }
            });
        });

        return updated.get();
    }

    public void work() {
        this.totalChunks = 0;
        ImmutableMap.Builder<ResourceKey<Level>, ListIterator<ChunkPos>> builder = ImmutableMap.builder();
        ImmutableSet<ResourceKey<Level>> immutableset = this.worldGenSettings.levels();

        for (ResourceKey<Level> resourcekey : immutableset) {
            List<ChunkPos> list = this.getAllChunkPos(resourcekey);
            builder.put(resourcekey, list.listIterator());
            this.totalChunks += list.size();
        }

        if (this.totalChunks == 0) {
            this.finished = true;
        } else {
            float f1 = (float) this.totalChunks;
            ImmutableMap<ResourceKey<Level>, ListIterator<ChunkPos>> immutablemap = builder.build();
            ImmutableMap.Builder<ResourceKey<Level>, ChunkStorage> builder1 = ImmutableMap.builder();

            for (ResourceKey<Level> resourcekey1 : immutableset) {
                Path path = this.levelStorage.getDimensionPath(resourcekey1);
                builder1.put(resourcekey1, new IronsChunkStorage(path.resolve("region"), this.dataFixer, true));
            }

            ImmutableMap<ResourceKey<Level>, ChunkStorage> immutablemap1 = builder1.build();
            long millis = Util.getMillis();
            while (this.running) {
                boolean processedItem = false;

                for (ResourceKey<Level> resourcekey2 : immutableset) {
                    ListIterator<ChunkPos> listiterator = immutablemap.get(resourcekey2);
                    ChunkStorage chunkstorage = immutablemap1.get(resourcekey2);
                    if (listiterator.hasNext()) {
                        ChunkPos chunkpos = listiterator.next();
                        boolean updated = false;

                        try {
                            CompoundTag compoundtag = chunkstorage.read(chunkpos).join().orElse((CompoundTag) null);
                            if (compoundtag != null) {

                                if ((chunkpos.x == -3 && chunkpos.z == -1)
                                        || (chunkpos.x == -3 && chunkpos.z == -2)) {
                                    int x = 0;
                                    if (upgradeBlockEntities(compoundtag)) {
                                        updated = true;
                                        chunkstorage.write(chunkpos, compoundtag);
                                    }
                                }
                            }
                        } catch (CompletionException | ReportedException reportedexception) {
                            Throwable throwable = reportedexception.getCause();
                            if (!(throwable instanceof IOException)) {
                                throw reportedexception;
                            }

                            IronsSpellbooks.LOGGER.error("Error upgrading chunk {}", chunkpos, throwable);
                        }

                        if (updated) {
                            ++this.converted;
                        } else {
                            ++this.skipped;
                        }

                        processedItem = true;
                    }

                    float f2 = (float) listiterator.nextIndex() / f1;
                    this.progressMap.put(resourcekey2, f2);
                    f += f2;
                }

                if (!processedItem) {
                    this.running = false;
                }
            }

            for (ChunkStorage chunkstorage1 : immutablemap1.values()) {
                try {
                    chunkstorage1.close();
                } catch (IOException ioexception) {
                    IronsSpellbooks.LOGGER.error("Error upgrading chunk", (Throwable) ioexception);
                }
            }

            this.overworldDataStorage.save();
            millis = Util.getMillis() - millis;
            IronsSpellbooks.LOGGER.info("Iron's World Upgrader finished after {} ms.  updated:{} skipped:{}", millis, this.converted, this.skipped);
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

    public ImmutableSet<ResourceKey<Level>> levels() {
        return this.worldGenSettings.levels();
    }
}