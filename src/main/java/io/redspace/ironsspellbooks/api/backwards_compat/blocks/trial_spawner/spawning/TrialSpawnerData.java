package io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner.spawning;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.random.WeightedEntry;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class TrialSpawnerData {
    public static final String TAG_SPAWN_DATA = "spawn_data";
    private static final String TAG_NEXT_MOB_SPAWNS_AT = "next_mob_spawns_at";
    private static final int DELAY_BETWEEN_PLAYER_SCANS = 20;
    private static final int TRIAL_OMEN_PER_BAD_OMEN_LEVEL = 18000;
    public static final Codec<Set<UUID>> CODEC_SET = Codec.list(UUIDUtil.CODEC).xmap(Sets::newHashSet, Lists::newArrayList);

    public static MapCodec<TrialSpawnerData> MAP_CODEC = RecordCodecBuilder.mapCodec(
        p_312830_ -> p_312830_.group(
                    CODEC_SET.optionalFieldOf("registered_players", Sets.newHashSet()).forGetter(p_312495_ -> p_312495_.detectedPlayers),
                    CODEC_SET.optionalFieldOf("current_mobs", Sets.newHashSet()).forGetter(p_312798_ -> p_312798_.currentMobs),
                    Codec.LONG.optionalFieldOf("cooldown_ends_at", Long.valueOf(0L)).forGetter(p_312792_ -> p_312792_.cooldownEndsAt),
                    Codec.LONG.optionalFieldOf("next_mob_spawns_at", Long.valueOf(0L)).forGetter(p_311772_ -> p_311772_.nextMobSpawnsAt),
                    Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("total_mobs_spawned", 0).forGetter(p_312862_ -> p_312862_.totalMobsSpawned),
                    SpawnData.CODEC.optionalFieldOf("spawn_data").forGetter(p_312634_ -> p_312634_.nextSpawnData),
                    ResourceLocation.CODEC.optionalFieldOf("ejecting_loot_table").forGetter(p_312388_ -> p_312388_.ejectingLootTable)
                )
                .apply(p_312830_, TrialSpawnerData::new)
    );
    protected final Set<UUID> detectedPlayers = new HashSet<>();
    protected final Set<UUID> currentMobs = new HashSet<>();
    protected long cooldownEndsAt;
    protected long nextMobSpawnsAt;
    protected int totalMobsSpawned;
    protected Optional<SpawnData> nextSpawnData;
    protected Optional<ResourceLocation> ejectingLootTable;
    @Nullable
    protected Entity displayEntity;
    @Nullable
    private SimpleWeightedRandomList<ItemStack> dispensing;
    protected double spin;
    protected double oSpin;

    public TrialSpawnerData() {
        this(Collections.emptySet(), Collections.emptySet(), 0L, 0L, 0, Optional.empty(), Optional.empty());
    }

    public TrialSpawnerData(
        Set<UUID> detectedPlayers,
        Set<UUID> currentMobs,
        long cooldownEndsAt,
        long nextMobSpawnsAt,
        int totalMobsSpawned,
        Optional<SpawnData> nextSpawnData,
        Optional<ResourceLocation> ejectingLootTable
    ) {
        this.detectedPlayers.addAll(detectedPlayers);
        this.currentMobs.addAll(currentMobs);
        this.cooldownEndsAt = cooldownEndsAt;
        this.nextMobSpawnsAt = nextMobSpawnsAt;
        this.totalMobsSpawned = totalMobsSpawned;
        this.nextSpawnData = nextSpawnData;
        this.ejectingLootTable = ejectingLootTable;
    }

    public void reset() {
        this.detectedPlayers.clear();
        this.totalMobsSpawned = 0;
        this.nextMobSpawnsAt = 0L;
        this.cooldownEndsAt = 0L;
        this.currentMobs.clear();
        this.nextSpawnData = Optional.empty();
    }

    public boolean hasMobToSpawn(TrialSpawner trialSpawner, RandomSource random) {
        boolean flag = this.getOrCreateNextSpawnData(trialSpawner, random).getEntityToSpawn().contains("id", 8);
        return flag || !trialSpawner.getConfig().spawnPotentialsDefinition().isEmpty();
    }

    public boolean hasFinishedSpawningAllMobs(TrialSpawnerConfig config, int players) {
        return this.totalMobsSpawned >= config.calculateTargetTotalMobs(players);
    }

    public boolean haveAllCurrentMobsDied() {
        return this.currentMobs.isEmpty();
    }

    public boolean isReadyToSpawnNextMob(ServerLevel level, TrialSpawnerConfig config, int players) {
        return level.getGameTime() >= this.nextMobSpawnsAt && this.currentMobs.size() < config.calculateTargetSimultaneousMobs(players);
    }

    public int countAdditionalPlayers(BlockPos pos) {
        if (this.detectedPlayers.isEmpty()) {
            Util.logAndPauseIfInIde("Trial Spawner at " + pos + " has no detected players");
        }

        return Math.max(0, this.detectedPlayers.size() - 1);
    }

    public void tryDetectPlayers(ServerLevel level, BlockPos pos, TrialSpawner spawner) {
        boolean flag = (pos.asLong() + level.getGameTime()) % 20L != 0L;
        if (!flag) {
            if (!spawner.getState().equals(TrialSpawnerState.COOLDOWN) || !spawner.isOminous()) {
                List<UUID> list = spawner.getPlayerDetector()
                    .detect(level, spawner.getEntitySelector(), pos, (double)spawner.getRequiredPlayerRange(), true);
                boolean flag1;
                if (!spawner.isOminous() && !list.isEmpty()) {
                    Optional<Pair<Player, MobEffect>> optional = findPlayerWithOminousEffect(level, list);
                    optional.ifPresent(p_350233_ -> {
                        Player player = p_350233_.getFirst();
                        if (p_350233_.getSecond() == MobEffects.BAD_OMEN) {
                            transformBadOmenIntoTrialOmen(player);
                        }

                        level.levelEvent(3020, BlockPos.containing(player.getEyePosition()), 0);
                        spawner.applyOminous(level, pos);
                    });
                    flag1 = optional.isPresent();
                } else {
                    flag1 = false;
                }

                if (!spawner.getState().equals(TrialSpawnerState.COOLDOWN) || flag1) {
                    boolean flag2 = spawner.getData().detectedPlayers.isEmpty();
                    List<UUID> list1 = flag2
                        ? list
                        : spawner.getPlayerDetector()
                            .detect(level, spawner.getEntitySelector(), pos, (double)spawner.getRequiredPlayerRange(), false);
                    if (this.detectedPlayers.addAll(list1)) {
                        this.nextMobSpawnsAt = Math.max(level.getGameTime() + 40L, this.nextMobSpawnsAt);
                        if (!flag1) {
//                            int i = spawner.isOminous() ? 3019 : 3013;
//                            level.levelEvent(i, pos, this.detectedPlayers.size());
                            level.playSound(null, pos, SoundRegistry.TRIAL_SPAWNER_DETECT_PLAYER.get(), SoundSource.BLOCKS);

                        }
                    }
                }
            }
        }
    }

    private static Optional<Pair<Player, MobEffect>> findPlayerWithOminousEffect(ServerLevel level, List<UUID> players) {
//        Player player = null;
//
//        for (UUID uuid : players) {
//            Player player1 = level.getPlayerByUUID(uuid);
//            if (player1 != null) {
//                MobEffect holder = MobEffects.TRIAL_OMEN;
//                if (player1.hasEffect(holder)) {
//                    return Optional.of(Pair.of(player1, holder));
//                }
//
//                if (player1.hasEffect(MobEffects.BAD_OMEN)) {
//                    player = player1;
//                }
//            }
//        }
//
//        return Optional.ofNullable(player).map(p_350229_ -> Pair.of(p_350229_, MobEffects.BAD_OMEN));
        // fixme: trial omen impl?
        return Optional.empty();
    }

    public void resetAfterBecomingOminous(TrialSpawner spawner, ServerLevel level) {
        this.currentMobs.stream().map(level::getEntity).forEach(p_351984_ -> {
            if (p_351984_ != null) {
                level.levelEvent(3012, p_351984_.blockPosition(), TrialSpawner.FlameParticle.NORMAL.encode());
                if (p_351984_ instanceof Mob mob) {
                    // fixme: trial mobs matter for this?
//                    mob.dropPreservedEquipment();
                }

                p_351984_.remove(Entity.RemovalReason.DISCARDED);
            }
        });
        if (!spawner.getOminousConfig().spawnPotentialsDefinition().isEmpty()) {
            this.nextSpawnData = Optional.empty();
        }

        this.totalMobsSpawned = 0;
        this.currentMobs.clear();
        this.nextMobSpawnsAt = level.getGameTime() + (long)spawner.getOminousConfig().ticksBetweenSpawn();
        spawner.markUpdated();
        this.cooldownEndsAt = level.getGameTime() + spawner.getOminousConfig().ticksBetweenItemSpawners();
    }

    private static void transformBadOmenIntoTrialOmen(Player player) {
        // fixme: trial omen impl?
//        MobEffectInstance mobeffectinstance = player.getEffect(MobEffects.BAD_OMEN);
//        if (mobeffectinstance != null) {
//            int i = mobeffectinstance.getAmplifier() + 1;
//            int j = 18000 * i;
//            player.removeEffect(MobEffects.BAD_OMEN);
//            player.addEffect(new MobEffectInstance(MobEffects.TRIAL_OMEN, j, 0));
//        }
    }

    public boolean isReadyToOpenShutter(ServerLevel level, float delay, int targetCooldownLength) {
        long i = this.cooldownEndsAt - (long)targetCooldownLength;
        return (float)level.getGameTime() >= (float)i + delay;
    }

    public boolean isReadyToEjectItems(ServerLevel level, float delay, int targetCooldownLength) {
        long i = this.cooldownEndsAt - (long)targetCooldownLength;
        return (float)(level.getGameTime() - i) % delay == 0.0F;
    }

    public boolean isCooldownFinished(ServerLevel level) {
        return level.getGameTime() >= this.cooldownEndsAt;
    }

    public void setEntityId(TrialSpawner spawner, RandomSource random, EntityType<?> entityType) {
        this.getOrCreateNextSpawnData(spawner, random).getEntityToSpawn().putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(entityType).toString());
    }

    protected SpawnData getOrCreateNextSpawnData(TrialSpawner spawner, RandomSource random) {
        if (this.nextSpawnData.isPresent()) {
            return this.nextSpawnData.get();
        } else {
            SimpleWeightedRandomList<SpawnData> simpleweightedrandomlist = spawner.getConfig().spawnPotentialsDefinition();
            Optional<SpawnData> optional = simpleweightedrandomlist.isEmpty()
                ? this.nextSpawnData
                : simpleweightedrandomlist.getRandom(random).map(WeightedEntry.Wrapper::getData);
            this.nextSpawnData = Optional.of(optional.orElseGet(SpawnData::new));
            spawner.markUpdated();
            return this.nextSpawnData.get();
        }
    }

    @Nullable
    public Entity getOrCreateDisplayEntity(TrialSpawner spawner, Level level, TrialSpawnerState spawnerState) {
        if (!spawnerState.hasSpinningMob()) {
            return null;
        } else {
            if (this.displayEntity == null) {
                CompoundTag compoundtag = this.getOrCreateNextSpawnData(spawner, level.getRandom()).getEntityToSpawn();
                if (compoundtag.contains("id", 8)) {
                    this.displayEntity = EntityType.loadEntityRecursive(compoundtag, level, Function.identity());
                }
            }

            return this.displayEntity;
        }
    }

    public CompoundTag getUpdateTag(TrialSpawnerState spawnerState) {
        CompoundTag compoundtag = new CompoundTag();
        if (spawnerState == TrialSpawnerState.ACTIVE) {
            compoundtag.putLong("next_mob_spawns_at", this.nextMobSpawnsAt);
        }

        this.nextSpawnData
            .ifPresent(
                p_338045_ -> compoundtag.put(
                        "spawn_data",
                        SpawnData.CODEC.encodeStart(NbtOps.INSTANCE, p_338045_).result().orElseThrow(() -> new IllegalStateException("Invalid SpawnData"))
                    )
            );
        return compoundtag;
    }

    public double getSpin() {
        return this.spin;
    }

    public double getOSpin() {
        return this.oSpin;
    }

    SimpleWeightedRandomList<ItemStack> getDispensingItems(ServerLevel level, TrialSpawnerConfig config, BlockPos pos) {
        if (this.dispensing != null) {
            return this.dispensing;
        } else {
            LootTable loottable = level.getServer().getLootData().getLootTable(config.itemsToDropWhenOminous());
            LootParams lootparams = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
            long i = lowResolutionPosition(level, pos);
            ObjectArrayList<ItemStack> objectarraylist = loottable.getRandomItems(lootparams, i);
            if (objectarraylist.isEmpty()) {
                return SimpleWeightedRandomList.empty();
            } else {
                SimpleWeightedRandomList.Builder<ItemStack> builder = new SimpleWeightedRandomList.Builder<>();

                for (ItemStack itemstack : objectarraylist) {
                    builder.add(itemstack.copyWithCount(1), itemstack.getCount());
                }

                this.dispensing = builder.build();
                return this.dispensing;
            }
        }
    }

    private static long lowResolutionPosition(ServerLevel level, BlockPos pos) {
        BlockPos blockpos = new BlockPos(
            Mth.floor((float)pos.getX() / 30.0F), Mth.floor((float)pos.getY() / 20.0F), Mth.floor((float)pos.getZ() / 30.0F)
        );
        return level.getSeed() + blockpos.asLong();
    }
}
