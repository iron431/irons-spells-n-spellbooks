package io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner.spawning;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner.TrialSpawnerBlock;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobSpawnEvent;

import java.util.Optional;
import java.util.UUID;

public final class TrialSpawner {
    public static final String NORMAL_CONFIG_TAG_NAME = "normal_config";
    public static final String OMINOUS_CONFIG_TAG_NAME = "ominous_config";
    public static final int DETECT_PLAYER_SPAWN_BUFFER = 40;
    private static final int DEFAULT_TARGET_COOLDOWN_LENGTH = 36000;
    private static final int DEFAULT_PLAYER_SCAN_RANGE = 14;
    private static final int MAX_MOB_TRACKING_DISTANCE = 47;
    private static final int MAX_MOB_TRACKING_DISTANCE_SQR = Mth.square(47);
    private static final float SPAWNING_AMBIENT_SOUND_CHANCE = 0.02F;
    private final TrialSpawnerConfig normalConfig;
    private final TrialSpawnerConfig ominousConfig;
    private final TrialSpawnerData data;
    private final int requiredPlayerRange;
    private final int targetCooldownLength;
    private final TrialSpawner.StateAccessor stateAccessor;
    private PlayerDetector playerDetector;
    private final PlayerDetector.EntitySelector entitySelector;
    private boolean overridePeacefulAndMobSpawnRule;
    private boolean isOminous;

    public Codec<TrialSpawner> codec() {
        return RecordCodecBuilder.create(
                p_338040_ -> p_338040_.group(
                                TrialSpawnerConfig.CODEC.optionalFieldOf("normal_config", TrialSpawnerConfig.DEFAULT).forGetter(TrialSpawner::getNormalConfig),
                                TrialSpawnerConfig.CODEC
                                        .optionalFieldOf("ominous_config", TrialSpawnerConfig.DEFAULT)
                                        .forGetter(TrialSpawner::getOminousConfigForSerialization),
                                TrialSpawnerData.MAP_CODEC.forGetter(TrialSpawner::getData),
                                Codec.intRange(0, Integer.MAX_VALUE).optionalFieldOf("target_cooldown_length", 36000).forGetter(TrialSpawner::getTargetCooldownLength),
                                Codec.intRange(1, 128).optionalFieldOf("required_player_range", 14).forGetter(TrialSpawner::getRequiredPlayerRange)
                        )
                        .apply(
                                p_338040_,
                                (p_338035_, p_338036_, p_338037_, p_338038_, p_338039_) -> new TrialSpawner(
                                        p_338035_, p_338036_, p_338037_, p_338038_, p_338039_, this.stateAccessor, this.playerDetector, this.entitySelector
                                )
                        )
        );
    }

    public TrialSpawner(TrialSpawner.StateAccessor stateAccessor, PlayerDetector playerDetector, PlayerDetector.EntitySelector entitySelector) {
        this(TrialSpawnerConfig.DEFAULT, TrialSpawnerConfig.DEFAULT, new TrialSpawnerData(), 36000, 14, stateAccessor, playerDetector, entitySelector);
    }

    public TrialSpawner(
            TrialSpawnerConfig normalConfig,
            TrialSpawnerConfig ominousConfig,
            TrialSpawnerData data,
            int targetCooldownLength,
            int requiredPlayerRange,
            TrialSpawner.StateAccessor stateAccessor,
            PlayerDetector playerDetector,
            PlayerDetector.EntitySelector entitySelector
    ) {
        this.normalConfig = normalConfig;
        this.ominousConfig = ominousConfig;
        this.data = data;
        this.targetCooldownLength = targetCooldownLength;
        this.requiredPlayerRange = requiredPlayerRange;
        this.stateAccessor = stateAccessor;
        this.playerDetector = playerDetector;
        this.entitySelector = entitySelector;
    }

    public TrialSpawnerConfig getConfig() {
        return this.isOminous ? this.ominousConfig : this.normalConfig;
    }

    @VisibleForTesting
    public TrialSpawnerConfig getNormalConfig() {
        return this.normalConfig;
    }

    @VisibleForTesting
    public TrialSpawnerConfig getOminousConfig() {
        return this.ominousConfig;
    }

    private TrialSpawnerConfig getOminousConfigForSerialization() {
        return !this.ominousConfig.equals(this.normalConfig) ? this.ominousConfig : TrialSpawnerConfig.DEFAULT;
    }

    public void applyOminous(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, level.getBlockState(pos).setValue(TrialSpawnerBlock.OMINOUS, Boolean.valueOf(true)), 3);
        level.levelEvent(3020, pos, 1);
        this.isOminous = true;
        this.data.resetAfterBecomingOminous(this, level);
    }

    public void removeOminous(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, level.getBlockState(pos).setValue(TrialSpawnerBlock.OMINOUS, Boolean.valueOf(false)), 3);
        this.isOminous = false;
    }

    public boolean isOminous() {
        return this.isOminous;
    }

    public TrialSpawnerData getData() {
        return this.data;
    }

    public int getTargetCooldownLength() {
        return this.targetCooldownLength;
    }

    public int getRequiredPlayerRange() {
        return this.requiredPlayerRange;
    }

    public TrialSpawnerState getState() {
        return this.stateAccessor.getState();
    }

    public void setState(Level level, TrialSpawnerState state) {
        this.stateAccessor.setState(level, state);
    }

    public void markUpdated() {
        this.stateAccessor.markUpdated();
    }

    public PlayerDetector getPlayerDetector() {
        return this.playerDetector;
    }

    public PlayerDetector.EntitySelector getEntitySelector() {
        return this.entitySelector;
    }

    public boolean canSpawnInLevel(Level level) {
        if (this.overridePeacefulAndMobSpawnRule) {
            return true;
        } else {
            return level.getDifficulty() == Difficulty.PEACEFUL ? false : level.getGameRules().getBoolean(GameRules.RULE_DOMOBSPAWNING);
        }
    }

    public Optional<UUID> spawnMob(ServerLevel level, BlockPos pos) {
        RandomSource randomsource = level.getRandom();
        SpawnData spawndata = this.data.getOrCreateNextSpawnData(this, level.getRandom());
        CompoundTag compoundtag = spawndata.entityToSpawn();
        ListTag listtag = compoundtag.getList("Pos", 6);
        Optional<EntityType<?>> optional = EntityType.by(compoundtag);
        if (optional.isEmpty()) {
            return Optional.empty();
        } else {
            int i = listtag.size();
            double d0 = i >= 1
                    ? listtag.getDouble(0)
                    : (double) pos.getX() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double) this.getConfig().spawnRange() + 0.5;
            double d1 = i >= 2 ? listtag.getDouble(1) : (double) (pos.getY() + randomsource.nextInt(3) - 1);
            double d2 = i >= 3
                    ? listtag.getDouble(2)
                    : (double) pos.getZ() + (randomsource.nextDouble() - randomsource.nextDouble()) * (double) this.getConfig().spawnRange() + 0.5;
            if (!level.noCollision(optional.get().getAABB(d0, d1, d2))) {
                return Optional.empty();
            } else {
                Vec3 vec3 = new Vec3(d0, d1, d2);
                if (!inLineOfSight(level, pos.getCenter(), vec3)) {
                    return Optional.empty();
                } else {
                    BlockPos blockpos = BlockPos.containing(vec3);
                    if (!SpawnPlacements.checkSpawnRules(optional.get(), level, MobSpawnType.MOB_SUMMONED, blockpos, level.getRandom())) {
                        return Optional.empty();
                    } else {
                        if (spawndata.getCustomSpawnRules().isPresent()) {
                            SpawnData.CustomSpawnRules spawndata$customspawnrules = spawndata.getCustomSpawnRules().get();
                            if (!(spawndata$customspawnrules.blockLightLimit().isValueInRange(level.getBrightness(LightLayer.BLOCK, pos)) &&
                                    spawndata$customspawnrules.skyLightLimit().isValueInRange(level.getBrightness(LightLayer.SKY, pos)))) {
                                return Optional.empty();
                            }
                        }

                        Entity entity = EntityType.loadEntityRecursive(compoundtag, level, p_312375_ -> {
                            p_312375_.moveTo(d0, d1, d2, randomsource.nextFloat() * 360.0F, 0.0F);
                            return p_312375_;
                        });
                        if (entity == null) {
                            return Optional.empty();
                        } else {
                            if (entity instanceof Mob mob) {
                                if (!mob.checkSpawnObstruction(level)) {
                                    return Optional.empty();
                                }

                                boolean flag = spawndata.getEntityToSpawn().size() == 1 && spawndata.getEntityToSpawn().contains("id", 8);
                                // Neo: Patch in FinalizeSpawn for spawners so it may be fired unconditionally, instead of only when vanilla would normally call it.
                                // The local flag is the conditions under which the spawner will normally call Mob#finalizeSpawn.
//                                net.neoforged.neoforge.event.EventHooks.finalizeMobSpawnSpawner(mob, level, level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.SPAWNER, null, this, flag);
                                var event = new MobSpawnEvent.FinalizeSpawn(mob, level, mob.getX(), mob.getY(), mob.getZ(), level.getCurrentDifficultyAt(mob.blockPosition()), MobSpawnType.SPAWNER, null, null, null);
                                MinecraftForge.EVENT_BUS.post(event);
                                mob.finalizeSpawn(level, event.getDifficulty(), event.getSpawnType(), event.getSpawnData(), event.getSpawnTag());
                                mob.setPersistenceRequired();
                                //todo: is this necessary for our strict usage?
//                                spawndata.getEquipment().ifPresent(mob::equip);
                            }

                            if (!level.tryAddFreshEntityWithPassengers(entity)) {
                                return Optional.empty();
                            } else {
                                TrialSpawner.FlameParticle trialspawner$flameparticle = this.isOminous
                                        ? TrialSpawner.FlameParticle.OMINOUS
                                        : TrialSpawner.FlameParticle.NORMAL;
                                level.playSound(null, pos, SoundRegistry.TRIAL_SPAWNER_SPAWN_MOB.get(), SoundSource.BLOCKS);
                                MagicManager.spawnParticles(level, trialspawner$flameparticle.particleType, entity.getBoundingBox().getCenter().x, entity.getBoundingBox().getCenter().y, entity.getBoundingBox().getCenter().z, 25, 0.5, 1, 0.5, 0.08, false);
//                                level.levelEvent(3011, pos, trialspawner$flameparticle.encode());
//                                level.levelEvent(3012, blockpos, trialspawner$flameparticle.encode());
                                level.gameEvent(entity, GameEvent.ENTITY_PLACE, blockpos);
                                return Optional.of(entity.getUUID());
                            }
                        }
                    }
                }
            }
        }
    }

    public void ejectReward(ServerLevel level, BlockPos pos, ResourceLocation lootTable) {
        LootTable loottable = level.getServer().getLootData().getLootTable(lootTable);
        LootParams lootparams = new LootParams.Builder(level).create(LootContextParamSets.EMPTY);
        ObjectArrayList<ItemStack> objectarraylist = loottable.getRandomItems(lootparams);
        if (!objectarraylist.isEmpty()) {
            for (ItemStack itemstack : objectarraylist) {
                DefaultDispenseItemBehavior.spawnItem(level, itemstack, 2, Direction.UP, Vec3.atBottomCenterOf(pos).relative(Direction.UP, 1.2));
            }

//            level.levelEvent(3014, pos, 0);
            level.playSound(null, pos, SoundRegistry.TRIAL_SPAWNER_EJECT_ITEM.get(), SoundSource.BLOCKS);

        }
    }

    public void tickClient(Level level, BlockPos pos, boolean isOminous) {
        TrialSpawnerState trialspawnerstate = this.getState();
        trialspawnerstate.emitParticles(level, pos, isOminous);
        if (trialspawnerstate.hasSpinningMob()) {
            double d0 = (double) Math.max(0L, this.data.nextMobSpawnsAt - level.getGameTime());
            this.data.oSpin = this.data.spin;
            this.data.spin = (this.data.spin + trialspawnerstate.spinningMobSpeed() / (d0 + 200.0)) % 360.0;
        }

        if (trialspawnerstate.isCapableOfSpawning()) {
            RandomSource randomsource = level.getRandom();
            if (randomsource.nextFloat() <= 0.02F) {
                SoundEvent soundevent = isOminous ? SoundRegistry.TRIAL_SPAWNER_AMBIENT_OMINOUS.get() : SoundRegistry.TRIAL_SPAWNER_AMBIENT.get();
                level.playLocalSound(
                        pos, soundevent, SoundSource.BLOCKS, randomsource.nextFloat() * 0.25F + 0.75F, randomsource.nextFloat() + 0.5F, false
                );
            }
        }
    }

    public void tickServer(ServerLevel level, BlockPos pos, boolean isOminous) {
        this.isOminous = isOminous;
        TrialSpawnerState trialspawnerstate = this.getState();
        if (this.data.currentMobs.removeIf(p_312870_ -> shouldMobBeUntracked(level, pos, p_312870_))) {
            this.data.nextMobSpawnsAt = level.getGameTime() + (long) this.getConfig().ticksBetweenSpawn();
        }

        TrialSpawnerState trialspawnerstate1 = trialspawnerstate.tickAndGetNext(pos, this, level);
        if (trialspawnerstate1 != trialspawnerstate) {
            this.setState(level, trialspawnerstate1);
        }
    }

    private static boolean shouldMobBeUntracked(ServerLevel level, BlockPos pos, UUID uuid) {
        Entity entity = level.getEntity(uuid);
        return entity == null
                || !entity.isAlive()
                || !entity.level().dimension().equals(level.dimension())
                || entity.blockPosition().distSqr(pos) > (double) MAX_MOB_TRACKING_DISTANCE_SQR;
    }

    private static boolean inLineOfSight(Level level, Vec3 spawnerPos, Vec3 mobPos) {
        BlockHitResult blockhitresult = level.clip(
                new ClipContext(mobPos, spawnerPos, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, null)
        );
        return blockhitresult.getBlockPos().equals(BlockPos.containing(spawnerPos)) || blockhitresult.getType() == HitResult.Type.MISS;
    }

    public static void addSpawnParticles(Level level, BlockPos pos, RandomSource random, SimpleParticleType particleType) {
        for (int i = 0; i < 20; i++) {
            double d0 = (double) pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double d1 = (double) pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double d2 = (double) pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0, 0.0, 0.0);
            level.addParticle(particleType, d0, d1, d2, 0.0, 0.0, 0.0);
        }
    }

    public static void addBecomeOminousParticles(Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 20; i++) {
            double d0 = (double) pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double d1 = (double) pos.getY() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double d2 = (double) pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 2.0;
            double d3 = random.nextGaussian() * 0.02;
            double d4 = random.nextGaussian() * 0.02;
            double d5 = random.nextGaussian() * 0.02;
//            level.addParticle(ParticleTypes.TRIAL_OMEN, d0, d1, d2, d3, d4, d5);
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, d0, d1, d2, d3, d4, d5);
        }
    }

    public static void addDetectPlayerParticles(Level level, BlockPos pos, RandomSource random, int type, ParticleOptions particle) {
        for (int i = 0; i < 30 + Math.min(type, 10) * 5; i++) {
            double d0 = (double) (2.0F * random.nextFloat() - 1.0F) * 0.65;
            double d1 = (double) (2.0F * random.nextFloat() - 1.0F) * 0.65;
            double d2 = (double) pos.getX() + 0.5 + d0;
            double d3 = (double) pos.getY() + 0.1 + (double) random.nextFloat() * 0.8;
            double d4 = (double) pos.getZ() + 0.5 + d1;
            level.addParticle(particle, d2, d3, d4, 0.0, 0.0, 0.0);
        }
    }

    public static void addEjectItemParticles(Level level, BlockPos pos, RandomSource random) {
        for (int i = 0; i < 20; i++) {
            double d0 = (double) pos.getX() + 0.4 + random.nextDouble() * 0.2;
            double d1 = (double) pos.getY() + 0.4 + random.nextDouble() * 0.2;
            double d2 = (double) pos.getZ() + 0.4 + random.nextDouble() * 0.2;
            double d3 = random.nextGaussian() * 0.02;
            double d4 = random.nextGaussian() * 0.02;
            double d5 = random.nextGaussian() * 0.02;
            level.addParticle(ParticleTypes.SMALL_FLAME, d0, d1, d2, d3, d4, d5 * 0.25);
            level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, d3, d4, d5);
        }
    }

    @Deprecated(
            forRemoval = true
    )
    @VisibleForTesting
    public void setPlayerDetector(PlayerDetector playerDetector) {
        this.playerDetector = playerDetector;
    }

    @Deprecated(
            forRemoval = true
    )
    @VisibleForTesting
    public void overridePeacefulAndMobSpawnRule() {
        this.overridePeacefulAndMobSpawnRule = true;
    }

    public static enum FlameParticle {
        NORMAL(ParticleTypes.FLAME),
        OMINOUS(ParticleTypes.SOUL_FIRE_FLAME);

        public final SimpleParticleType particleType;

        private FlameParticle(SimpleParticleType particleType) {
            this.particleType = particleType;
        }

        public static TrialSpawner.FlameParticle decode(int id) {
            TrialSpawner.FlameParticle[] atrialspawner$flameparticle = values();
            return id <= atrialspawner$flameparticle.length && id >= 0 ? atrialspawner$flameparticle[id] : NORMAL;
        }

        public int encode() {
            return this.ordinal();
        }
    }

    public interface StateAccessor {
        void setState(Level level, TrialSpawnerState state);

        TrialSpawnerState getState();

        void markUpdated();
    }

    /*
    1.21.1 IOwnedSpawner implementation. basically useless?
     */
//    @Override
//    @org.jetbrains.annotations.Nullable
//    public com.mojang.datafixers.util.Either<net.minecraft.world.level.block.entity.BlockEntity, Entity> getOwner() {
//        if (this.stateAccessor instanceof net.minecraft.world.level.block.entity.TrialSpawnerBlockEntity be) {
//            return com.mojang.datafixers.util.Either.left(be);
//        }
//        return null;
//    }
}