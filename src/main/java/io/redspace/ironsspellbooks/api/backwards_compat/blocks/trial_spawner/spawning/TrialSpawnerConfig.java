package io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner.spawning;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.SpawnData;

/*
ResourceKey<LootTable> replaced with ResourceLocation (loot tables aren't reloadable registries yet) and optionalLenientFields replaced with optionalFields in codecs.
 */
public record TrialSpawnerConfig(
    int spawnRange,
    float totalMobs,
    float simultaneousMobs,
    float totalMobsAddedPerPlayer,
    float simultaneousMobsAddedPerPlayer,
    int ticksBetweenSpawn,
    SimpleWeightedRandomList<SpawnData> spawnPotentialsDefinition,
//    SimpleWeightedRandomList<ResourceKey<LootTable>> lootTablesToEject,
    SimpleWeightedRandomList<ResourceLocation> lootTablesToEject,
//    ResourceKey<LootTable> itemsToDropWhenOminous
    ResourceLocation itemsToDropWhenOminous
) {
    public static final TrialSpawnerConfig DEFAULT = new TrialSpawnerConfig(
        4,
        6.0F,
        2.0F,
        2.0F,
        1.0F,
        40,
        SimpleWeightedRandomList.empty(),
        SimpleWeightedRandomList.<ResourceLocation>builder()
//            .add(BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_CONSUMABLES)
//            .add(BuiltInLootTables.SPAWNER_TRIAL_CHAMBER_KEY)
            .build(),
        /*BuiltInLootTables.SPAWNER_TRIAL_ITEMS_TO_DROP_WHEN_OMINOUS*/ResourceLocation.withDefaultNamespace("empty")
    );
    public static final Codec<TrialSpawnerConfig> CODEC = RecordCodecBuilder.create(
        p_338041_ -> p_338041_.group(
                    Codec.intRange(1, 128).optionalFieldOf("spawn_range", DEFAULT.spawnRange).forGetter(TrialSpawnerConfig::spawnRange),
                    Codec.floatRange(0.0F, Float.MAX_VALUE).optionalFieldOf("total_mobs", DEFAULT.totalMobs).forGetter(TrialSpawnerConfig::totalMobs),
                    Codec.floatRange(0.0F, Float.MAX_VALUE)
                        .optionalFieldOf("simultaneous_mobs", DEFAULT.simultaneousMobs)
                        .forGetter(TrialSpawnerConfig::simultaneousMobs),
                    Codec.floatRange(0.0F, Float.MAX_VALUE)
                        .optionalFieldOf("total_mobs_added_per_player", DEFAULT.totalMobsAddedPerPlayer)
                        .forGetter(TrialSpawnerConfig::totalMobsAddedPerPlayer),
                    Codec.floatRange(0.0F, Float.MAX_VALUE)
                        .optionalFieldOf("simultaneous_mobs_added_per_player", DEFAULT.simultaneousMobsAddedPerPlayer)
                        .forGetter(TrialSpawnerConfig::simultaneousMobsAddedPerPlayer),
                    Codec.intRange(0, Integer.MAX_VALUE)
                        .optionalFieldOf("ticks_between_spawn", DEFAULT.ticksBetweenSpawn)
                        .forGetter(TrialSpawnerConfig::ticksBetweenSpawn),
                    SpawnData.LIST_CODEC
                        .optionalFieldOf("spawn_potentials", SimpleWeightedRandomList.empty())
                        .forGetter(TrialSpawnerConfig::spawnPotentialsDefinition),
                    SimpleWeightedRandomList.wrappedCodecAllowingEmpty(ResourceLocation.CODEC)
                        .optionalFieldOf("loot_tables_to_eject", DEFAULT.lootTablesToEject)
                        .forGetter(TrialSpawnerConfig::lootTablesToEject),
                    ResourceLocation.CODEC
                        .optionalFieldOf("items_to_drop_when_ominous", DEFAULT.itemsToDropWhenOminous)
                        .forGetter(TrialSpawnerConfig::itemsToDropWhenOminous)
                )
                .apply(p_338041_, TrialSpawnerConfig::new)
    );

    public int calculateTargetTotalMobs(int players) {
        return (int)Math.floor((double)(this.totalMobs + this.totalMobsAddedPerPlayer * (float)players));
    }

    public int calculateTargetSimultaneousMobs(int players) {
        return (int)Math.floor((double)(this.simultaneousMobs + this.simultaneousMobsAddedPerPlayer * (float)players));
    }

    public long ticksBetweenItemSpawners() {
        return 160L;
    }
}
