package io.redspace.ironsspellbooks.api.backwards_compat.blocks.trial_spawner.spawning;

import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

public enum TrialSpawnerState implements StringRepresentable {
    INACTIVE("inactive", 0, ParticleEmission.NONE, -1.0, false),
    WAITING_FOR_PLAYERS("waiting_for_players", 4, ParticleEmission.SMALL_FLAMES, 200.0, true),
    ACTIVE("active", 8, ParticleEmission.FLAMES_AND_SMOKE, 1000.0, true),
    WAITING_FOR_REWARD_EJECTION("waiting_for_reward_ejection", 8, ParticleEmission.SMALL_FLAMES, -1.0, false),
    EJECTING_REWARD("ejecting_reward", 8, ParticleEmission.SMALL_FLAMES, -1.0, false),
    COOLDOWN("cooldown", 0, ParticleEmission.SMOKE_INSIDE_AND_TOP_FACE, -1.0, false);

    private static final float DELAY_BEFORE_EJECT_AFTER_KILLING_LAST_MOB = 40.0F;
    private static final int TIME_BETWEEN_EACH_EJECTION = Mth.floor(30.0F);
    private final String name;
    private final int lightLevel;
    private final double spinningMobSpeed;
    private final ParticleEmission particleEmission;
    private final boolean isCapableOfSpawning;

    private TrialSpawnerState(String name, int lightLevel, ParticleEmission particleEmission, double spinningMobSpeed, boolean isCapableOfSpawning) {
        this.name = name;
        this.lightLevel = lightLevel;
        this.particleEmission = particleEmission;
        this.spinningMobSpeed = spinningMobSpeed;
        this.isCapableOfSpawning = isCapableOfSpawning;
    }

    TrialSpawnerState tickAndGetNext(BlockPos pos, TrialSpawner spawner, ServerLevel level) {
        TrialSpawnerData trialspawnerdata = spawner.getData();
        TrialSpawnerConfig trialspawnerconfig = spawner.getConfig();

        return switch (this) {
            case INACTIVE -> trialspawnerdata.getOrCreateDisplayEntity(spawner, level, WAITING_FOR_PLAYERS) == null ? this : WAITING_FOR_PLAYERS;
            case WAITING_FOR_PLAYERS -> {
                if (!spawner.canSpawnInLevel(level)) {
                    trialspawnerdata.reset();
                    yield this;
                } else if (!trialspawnerdata.hasMobToSpawn(spawner, level.random)) {
                    yield INACTIVE;
                } else {
                    trialspawnerdata.tryDetectPlayers(level, pos, spawner);
                    yield trialspawnerdata.detectedPlayers.isEmpty() ? this : ACTIVE;
                }
            }
            case ACTIVE -> {
                if (!spawner.canSpawnInLevel(level)) {
                    trialspawnerdata.reset();
                    yield WAITING_FOR_PLAYERS;
                } else if (!trialspawnerdata.hasMobToSpawn(spawner, level.random)) {
                    yield INACTIVE;
                } else {
                    int i = trialspawnerdata.countAdditionalPlayers(pos);
                    trialspawnerdata.tryDetectPlayers(level, pos, spawner);
                    if (spawner.isOminous()) {
                        this.spawnOminousOminousItemSpawner(level, pos, spawner);
                    }

                    if (trialspawnerdata.hasFinishedSpawningAllMobs(trialspawnerconfig, i)) {
                        if (trialspawnerdata.haveAllCurrentMobsDied()) {
                            trialspawnerdata.cooldownEndsAt = level.getGameTime() + (long)spawner.getTargetCooldownLength();
                            trialspawnerdata.totalMobsSpawned = 0;
                            trialspawnerdata.nextMobSpawnsAt = 0L;
                            yield WAITING_FOR_REWARD_EJECTION;
                        }
                    } else if (trialspawnerdata.isReadyToSpawnNextMob(level, trialspawnerconfig, i)) {
                        spawner.spawnMob(level, pos).ifPresent(p_340800_ -> {
                            trialspawnerdata.currentMobs.add(p_340800_);
                            trialspawnerdata.totalMobsSpawned++;
                            trialspawnerdata.nextMobSpawnsAt = level.getGameTime() + (long)trialspawnerconfig.ticksBetweenSpawn();
                            trialspawnerconfig.spawnPotentialsDefinition().getRandom(level.getRandom()).ifPresent(p_338048_ -> {
                                trialspawnerdata.nextSpawnData = Optional.of(p_338048_.getData());
                                spawner.markUpdated();
                            });
                        });
                    }

                    yield this;
                }
            }
            case WAITING_FOR_REWARD_EJECTION -> {
                if (trialspawnerdata.isReadyToOpenShutter(level, 40.0F, spawner.getTargetCooldownLength())) {
                    level.playSound(null, pos, SoundRegistry.TRIAL_SPAWNER_OPEN_SHUTTER.get(), SoundSource.BLOCKS);
                    yield EJECTING_REWARD;
                } else {
                    yield this;
                }
            }
            case EJECTING_REWARD -> {
                if (!trialspawnerdata.isReadyToEjectItems(level, (float)TIME_BETWEEN_EACH_EJECTION, spawner.getTargetCooldownLength())) {
                    yield this;
                } else if (trialspawnerdata.detectedPlayers.isEmpty()) {
                    level.playSound(null, pos, SoundRegistry.TRIAL_SPAWNER_CLOSE_SHUTTER.get(), SoundSource.BLOCKS);
                    trialspawnerdata.ejectingLootTable = Optional.empty();
                    yield COOLDOWN;
                } else {
                    if (trialspawnerdata.ejectingLootTable.isEmpty()) {
                        trialspawnerdata.ejectingLootTable = trialspawnerconfig.lootTablesToEject().getRandomValue(level.getRandom());
                    }

                    trialspawnerdata.ejectingLootTable.ifPresent(p_335304_ -> spawner.ejectReward(level, pos, p_335304_));
                    trialspawnerdata.detectedPlayers.remove(trialspawnerdata.detectedPlayers.iterator().next());
                    yield this;
                }
            }
            case COOLDOWN -> {
                trialspawnerdata.tryDetectPlayers(level, pos, spawner);
                if (!trialspawnerdata.detectedPlayers.isEmpty()) {
                    trialspawnerdata.totalMobsSpawned = 0;
                    trialspawnerdata.nextMobSpawnsAt = 0L;
                    yield ACTIVE;
                } else if (trialspawnerdata.isCooldownFinished(level)) {
                    spawner.removeOminous(level, pos);
                    trialspawnerdata.reset();
                    yield WAITING_FOR_PLAYERS;
                } else {
                    yield this;
                }
            }
        };
    }

    private void spawnOminousOminousItemSpawner(ServerLevel level, BlockPos pos, TrialSpawner spawner) {
        TrialSpawnerData trialspawnerdata = spawner.getData();
        TrialSpawnerConfig trialspawnerconfig = spawner.getConfig();
        ItemStack itemstack = trialspawnerdata.getDispensingItems(level, trialspawnerconfig, pos)
            .getRandomValue(level.random)
            .orElse(ItemStack.EMPTY);
        if (!itemstack.isEmpty()) {
            if (this.timeToSpawnItemSpawner(level, trialspawnerdata)) {
                //fixme: trial ominous spawner... implement or nah?
//                calculatePositionToSpawnSpawner(level, pos, spawner, trialspawnerdata).ifPresent(p_338064_ -> {
//                    OminousItemSpawner ominousitemspawner = OminousItemSpawner.create(level, itemstack);
//                    ominousitemspawner.moveTo(p_338064_);
//                    level.addFreshEntity(ominousitemspawner);
//                    float f = (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F;
//                    level.playSound(null, BlockPos.containing(p_338064_), SoundEvents.TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, SoundSource.BLOCKS, 1.0F, f);
//                    trialspawnerdata.cooldownEndsAt = level.getGameTime() + spawner.getOminousConfig().ticksBetweenItemSpawners();
//                });
            }
        }
    }

    private static Optional<Vec3> calculatePositionToSpawnSpawner(ServerLevel level, BlockPos pos, TrialSpawner spawner, TrialSpawnerData spawnerData) {
        List<Player> list = spawnerData.detectedPlayers
            .stream()
            .map(level::getPlayerByUUID)
            .filter(Objects::nonNull)
            .filter(
                p_350236_ -> !p_350236_.isCreative()
                        && !p_350236_.isSpectator()
                        && p_350236_.isAlive()
                        && p_350236_.distanceToSqr(pos.getCenter()) <= (double)Mth.square(spawner.getRequiredPlayerRange())
            )
            .toList();
        if (list.isEmpty()) {
            return Optional.empty();
        } else {
            Entity entity = selectEntityToSpawnItemAbove(list, spawnerData.currentMobs, spawner, pos, level);
            return entity == null ? Optional.empty() : calculatePositionAbove(entity, level);
        }
    }

    private static Optional<Vec3> calculatePositionAbove(Entity entity, ServerLevel level) {
        Vec3 vec3 = entity.position();
        Vec3 vec31 = vec3.relative(Direction.UP, (double)(entity.getBbHeight() + 2.0F + (float)level.random.nextInt(4)));
        BlockHitResult blockhitresult = level.clip(new ClipContext(vec3, vec31, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, null));
        Vec3 vec32 = blockhitresult.getBlockPos().getCenter().relative(Direction.DOWN, 1.0);
        BlockPos blockpos = BlockPos.containing(vec32);
        return !level.getBlockState(blockpos).getCollisionShape(level, blockpos).isEmpty() ? Optional.empty() : Optional.of(vec32);
    }

    @Nullable
    private static Entity selectEntityToSpawnItemAbove(
        List<Player> player, Set<UUID> currentMobs, TrialSpawner spawner, BlockPos pos, ServerLevel level
    ) {
        Stream<Entity> stream = currentMobs.stream()
            .map(level::getEntity)
            .filter(Objects::nonNull)
            .filter(
                p_338051_ -> p_338051_.isAlive() && p_338051_.distanceToSqr(pos.getCenter()) <= (double)Mth.square(spawner.getRequiredPlayerRange())
            );
        List<? extends Entity> list = level.random.nextBoolean() ? stream.toList() : player;
        if (list.isEmpty()) {
            return null;
        } else {
            return list.size() == 1 ? list.get(0) : Util.getRandom(list, level.random);
        }
    }

    private boolean timeToSpawnItemSpawner(ServerLevel level, TrialSpawnerData spawnerData) {
        return level.getGameTime() >= spawnerData.cooldownEndsAt;
    }

    public int lightLevel() {
        return this.lightLevel;
    }

    public double spinningMobSpeed() {
        return this.spinningMobSpeed;
    }

    public boolean hasSpinningMob() {
        return this.spinningMobSpeed >= 0.0;
    }

    public boolean isCapableOfSpawning() {
        return this.isCapableOfSpawning;
    }

    public void emitParticles(Level level, BlockPos pos, boolean isOminous) {
        this.particleEmission.emit(level, level.getRandom(), pos, isOminous);
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    static class LightLevel {
        private static final int UNLIT = 0;
        private static final int HALF_LIT = 4;
        private static final int LIT = 8;

        private LightLevel() {
        }
    }

    interface ParticleEmission {
        ParticleEmission NONE = (p_311998_, p_311983_, p_312351_, p_338371_) -> {
        };
        ParticleEmission SMALL_FLAMES = (p_338069_, p_338070_, p_338071_, p_338072_) -> {
            if (p_338070_.nextInt(2) == 0) {
                Vec3 vec3 = p_338071_.getCenter().offsetRandom(p_338070_, 0.9F);
                addParticle(p_338072_ ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, vec3, p_338069_);
            }
        };
        ParticleEmission FLAMES_AND_SMOKE = (p_338065_, p_338066_, p_338067_, p_338068_) -> {
            Vec3 vec3 = p_338067_.getCenter().offsetRandom(p_338066_, 1.0F);
            addParticle(ParticleTypes.SMOKE, vec3, p_338065_);
            addParticle(p_338068_ ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, vec3, p_338065_);
        };
        ParticleEmission SMOKE_INSIDE_AND_TOP_FACE = (p_311899_, p_311762_, p_312096_, p_338301_) -> {
            Vec3 vec3 = p_312096_.getCenter().offsetRandom(p_311762_, 0.9F);
            if (p_311762_.nextInt(3) == 0) {
                addParticle(ParticleTypes.SMOKE, vec3, p_311899_);
            }

            if (p_311899_.getGameTime() % 20L == 0L) {
                Vec3 vec31 = p_312096_.getCenter().add(0.0, 0.5, 0.0);
                int i = p_311899_.getRandom().nextInt(4) + 20;

                for (int j = 0; j < i; j++) {
                    addParticle(ParticleTypes.SMOKE, vec31, p_311899_);
                }
            }
        };

        private static void addParticle(SimpleParticleType particleType, Vec3 pos, Level level) {
            level.addParticle(particleType, pos.x(), pos.y(), pos.z(), 0.0, 0.0, 0.0);
        }

        void emit(Level level, RandomSource random, BlockPos pos, boolean isOminous);
    }

    static class SpinningMob {
        private static final double NONE = -1.0;
        private static final double SLOW = 200.0;
        private static final double FAST = 1000.0;

        private SpinningMob() {
        }
    }
}
