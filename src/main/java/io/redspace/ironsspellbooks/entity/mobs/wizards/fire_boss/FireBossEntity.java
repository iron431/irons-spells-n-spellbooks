package io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.network.IClientEventEntity;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.*;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.entity.mobs.IAnimatedAttacker;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.DeadKingBoss;
import io.redspace.ironsspellbooks.entity.mobs.goals.MomentHurtByTargetGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.PatrolNearLocationGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.SpellBarrageGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.melee.AttackAnimationData;
import io.redspace.ironsspellbooks.entity.mobs.goals.melee.AttackKeyframe;
import io.redspace.ironsspellbooks.entity.mobs.keeper.KeeperEntity;
import io.redspace.ironsspellbooks.entity.spells.FireEruptionAoe;
import io.redspace.ironsspellbooks.entity.spells.fireball.MagicFireball;
import io.redspace.ironsspellbooks.network.EntityEventPacket;
import io.redspace.ironsspellbooks.network.particles.FieryExplosionParticlesPacket;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.network.PacketDistributor;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class FireBossEntity extends AbstractSpellCastingMob implements Enemy, IAnimatedAttacker, IEntityWithComplexSpawn, IClientEventEntity {
    public static final byte CLIENT_STOP_TRACKING = 0;
    public static final byte CLIENT_START_TRACKING = 1;
    public static final byte PROC_HALF_HEALTH_TIMER = 2;
    public static final byte STOP_HALF_HEALTH_TIMER = 3;
    public static final byte START_MUSIC = 4;
    public static final byte STOP_MUSIC = 5;
    public static final byte PROC_SPECTRAL_DAGGER = 6;

    /**
     * delay in seconds the boss will wait outside of combat until beginning despawn sequence
     */
    public static final int PROC_DESPAWN_SECONDS = 60;
    /**
     * maximum elapsed time in seconds the boss will last in unloaded chunks before deleting himself
     */
    public static final int UNLOADED_DESPAWN_LIMIT_SECONDS = 300;
    private static final BossbarManager.BossbarSprite BOSSBAR_SPRITE = new BossbarManager.BossbarSprite(IronsSpellbooks.id("boss_bars/tyros_bossbar"), 192, 18, 3, -1);

    @Override
    public void handleClientEvent(byte eventId) {
        switch (eventId) {
            case CLIENT_STOP_TRACKING -> {
                FogManager.stopEvent(this.uuid);
                MusicManager.stopEvent(this.uuid);
                BossbarManager.stopTracking(this.uuid);
            }
            case CLIENT_START_TRACKING -> {
                FogManager.createEvent(this, new FogManager.FogEvent(Optional.empty(), true));
                if (!isSpawning()) {
                    MusicManager.createEvent(this, new FireBossMusicHandler(true));
                }
                BossbarManager.startTracking(this.uuid, BOSSBAR_SPRITE);
            }
            case PROC_HALF_HEALTH_TIMER -> this.halfHealthTimer = HALF_HEALTH_ANIM_DURATION;
            case STOP_HALF_HEALTH_TIMER -> {
                this.halfHealthTimer = 0;
                this.playAnimation("idle");
            }
            case START_MUSIC -> MusicManager.createEvent(this, new FireBossMusicHandler(true));
            case STOP_MUSIC -> MusicManager.stopEvent(this.uuid);
            case PROC_SPECTRAL_DAGGER -> procSpectralDagger();
        }
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        buffer.writeInt(this.spawnTimer);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalData) {
        this.spawnTimer = additionalData.readInt();
        float y = this.getYRot();
        this.yBodyRot = y;
        this.yBodyRotO = y;
        this.yHeadRot = y;
        this.yHeadRotO = y;
        this.yRotO = y;
    }

    private static final EntityDataAccessor<Boolean> DATA_SOUL_MODE = SynchedEntityData.defineId(FireBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_DESPAWNING = SynchedEntityData.defineId(FireBossEntity.class, EntityDataSerializers.BOOLEAN);
    private static final AttributeModifier SOUL_SPEED_MODIFIER = new AttributeModifier(IronsSpellbooks.id("soul_mode"), 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final AttributeModifier SOUL_SCALE_MODIFIER = new AttributeModifier(IronsSpellbooks.id("soul_mode"), 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    private static final AttributeModifier MANA_MODIFIER = new AttributeModifier(IronsSpellbooks.id("mana"), 10000, AttributeModifier.Operation.ADD_VALUE);
    private int despawnAggroDelay;
    private int destroyBlockDelay;
    private int stuckDetectorDelay;
    private int stuckDetector;
    private Vec3 lastStuckPos = Vec3.ZERO;
    /**
     * Amount of non-creative/spectator players within 60 blocks of summoning this entity. Affects attribute scaling and drop count.
     */
    private int playerScale;

    /**
     * Client flag for whether code animations should pause over current animation
     */
    private boolean canAnimateOver;
    /**
     * Client flag for whether the head should stop animating lookat for the current animation
     */
    private boolean stopHeadAnimation;

    /**
     * Client side model control value
     */
    public float isAnimatingDampener;

    private ExtendedServerBossEvent bossEvent;

    public FireBossEntity(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        xpReward = 25;
        this.lookControl = createLookControl();
        this.moveControl = createMoveControl();
        createBossEvent();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(DATA_SOUL_MODE, false);
        pBuilder.define(DATA_IS_DESPAWNING, false);
    }

    protected LookControl createLookControl() {
        return new LookControl(this) {
            //This allows us to more rapidly turn towards our target. Helps to make sure his targets are aligned with his swing animations
            @Override
            protected float rotateTowards(float pFrom, float pTo, float pMaxDelta) {
                return super.rotateTowards(pFrom, pTo, pMaxDelta * 2.5f);
            }

            @Override
            protected boolean resetXRotOnTick() {
                return getTarget() == null;
            }
        };
    }

    protected MoveControl createMoveControl() {
        return new FireBossMoveControl(this);
    }


    public void startSeenByPlayer(ServerPlayer pPlayer) {
        super.startSeenByPlayer(pPlayer);
        this.bossEvent.addPlayer(pPlayer);
        PacketDistributor.sendToPlayer(pPlayer, new EntityEventPacket<FireBossEntity>(this, CLIENT_START_TRACKING));
    }

    public void stopSeenByPlayer(ServerPlayer pPlayer) {
        super.stopSeenByPlayer(pPlayer);
        this.bossEvent.removePlayer(pPlayer);
        PacketDistributor.sendToPlayer(pPlayer, new EntityEventPacket<FireBossEntity>(this, CLIENT_STOP_TRACKING));
    }

    FireBossAttackGoal attackGoal;

    @Override
    public FireBossMoveControl getMoveControl() {
        return (FireBossMoveControl) super.getMoveControl();
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.attackGoal = (FireBossAttackGoal) new FireBossAttackGoal(this, 1.5f, 50, 75)
                .setMoveset(List.of(
                        AttackAnimationData.builder("scythe_dagger_double_horizontal")
                                .length(60)
                                .attacks(
                                        new FireBossAttackKeyframe(15, new Vec3(0, 0, .25), new FireBossAttackKeyframe.SwingData(false, true)),
                                        new InvokeDaggerKeyframe(35),
                                        new FireBossAttackKeyframe(36, new Vec3(0, 0, .75), new FireBossAttackKeyframe.SwingData(false, false)),
                                        new AttackKeyframe(42, new Vec3(0, 0, 0))
                                ).build(),
                        AttackAnimationData.builder("scythe_backpedal")
                                .length(40)
                                .rangeMultiplier(2f)
                                .attacks(
                                        new FireBossAttackKeyframe(20, new Vec3(0, .3, -2), new FireBossAttackKeyframe.SwingData(false, true))
                                ).build(),
                        AttackAnimationData.builder("scythe_sideslash_downslash_sideslash")
                                .length(62)
                                .rangeMultiplier(2f)
                                .attacks(
                                        new FireBossAttackKeyframe(18, new Vec3(0, 0, .45), new FireBossAttackKeyframe.SwingData(false, true)),
                                        new FireBossAttackKeyframe(30, new Vec3(0, 0, .45), new FireBossAttackKeyframe.SwingData(false, false)),
                                        new FireBossAttackKeyframe(50, new Vec3(0, 0.1, 1.25), new Vec3(0, .3, 0.8), new FireBossAttackKeyframe.SwingData(false, false))
                                ).build(),
                        AttackAnimationData.builder("scythe_jump_combo")
                                .length(45)
                                .cancellable()
                                .rangeMultiplier(3f)
                                .attacks(
                                        new FireBossAttackKeyframe(20, new Vec3(0, 1, 0), new Vec3(0, 1.15, .1), new FireBossAttackKeyframe.SwingData(true, false)),
                                        new FireBossAttackKeyframe(35, new Vec3(0, 0, -.2), new Vec3(0, 0, 0.5), new FireBossAttackKeyframe.SwingData(false, false))
                                ).build(),
                        AttackAnimationData.builder("scythe_downslash_sideslash")
                                .length(60)
                                .attacks(
                                        new FireBossAttackKeyframe(22, new Vec3(0, 0, .5f), new Vec3(0, -.2, 0), new FireBossAttackKeyframe.SwingData(true, true)),
                                        new FireBossAttackKeyframe(40, new Vec3(0, .1, 0.8), new FireBossAttackKeyframe.SwingData(false, false))
                                ).build(),
                        AttackAnimationData.builder("scythe_horizontal_slash_spin")
                                .length(45)
                                .area(0.25f)
                                .rangeMultiplier(3f)
                                .attacks(
                                        new FireBossAttackKeyframe(14, new Vec3(0, 0.1, 1.25), new Vec3(0, .1, 0.8), new FireBossAttackKeyframe.SwingData(false, true)),
                                        new FireBossAttackKeyframe(30, new Vec3(0, 0.1, 1.85), new Vec3(0, .3, 0.8), new FireBossAttackKeyframe.SwingData(false, false))
                                ).build()

                ))
                .setComboChance(1f)
                .setMeleeAttackInverval(10, 30)
                .setMeleeBias(1f, 1f)
                .setSpells(
                        List.of(SpellRegistry.FIRE_ARROW_SPELL.get(), SpellRegistry.FIRE_ARROW_SPELL.get(), SpellRegistry.SCORCH_SPELL.get()),
                        List.of(), List.of(), List.of()
                );
        this.goalSelector.addGoal(2, new FieryDaggerSwarmAbilityGoal(this));
        this.goalSelector.addGoal(2, new FieryDaggerZoneAbilityGoal(this));
        this.goalSelector.addGoal(2, new SpellBarrageGoal(this, SpellRegistry.RAISE_HELL_SPELL.get(), 5, 5, 80, 240, 1));
        this.goalSelector.addGoal(3, attackGoal);

        this.goalSelector.addGoal(4, new PatrolNearLocationGoal(this, 30, .75f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.targetSelector.addGoal(1, new MomentHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Pig.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, DeadKingBoss.class, true));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    /*
     * Stance Break Mechanic
     * - In order for a long-form cinematic and serializable ability to take place, we must store a decent bit of data on the entity itself
     * - At 2/3 and 1/3 health, the boss's stance will break, interrupting all actions, and playing a short stun animation
     * - At the end of the stun, he performs 3 strikes of Raise Hell
     * - He goes into Soul Mode on the second break
     */
    int stanceBreakCounter;
    int stanceBreakTimer;
    static final int STANCE_BREAK_ANIM_TIME = (int) (9 * 20);
    static final int STANCE_BREAK_BEGIN_SLAMS_TIMESTAMP = (int) (6.5 * 20);
    static final int STANCE_BREAK_COUNT = 2;

    /*
     * Spawn Animation Handlers
     */
    int spawnTimer;
    private static final int SPAWN_ANIM_TIME = (int) (8.75 * 20);
    private static final int SPAWN_DELAY = 40;

    /*
     * Half Health Ability
     * - Upon reaching half health, the boss performs a pseudo wipe mechanic
     * - He Jumps into the air and beings charging a fireball/meteor
     * - After 10 seconds, he will launch it, which is powerful enough to nearly kill most anything
     * - However, if 10% of his max health is dealt as damage during this phase, the ability is interrupted and blows up the boss instead
     */
    boolean hasPerformedHalfHealthAttack;
    protected int halfHealthTimer;
    protected float halfHealthDamageAccumulated;
    protected static final int HALF_HEALTH_ANIM_DURATION = (int) (11.75 * 20);
    protected static final int HALF_HEALTH_JUMP_TIMESTAMP = (int) (0.58 * 20);
    protected static final int HALF_HEALTH_CAST_TIMESTAMP = (int) (11.50 * 20);
    /*
     * Spectral Dagger
     * client synced timer
     */
    int daggerTime;
    int parryCooldown;
    boolean clientDaggerParticles;

    public void triggerHalfHealthAttack() {
        hasPerformedHalfHealthAttack = true;
        halfHealthTimer = HALF_HEALTH_ANIM_DURATION;
        this.castComplete();
        this.attackGoal.stopMeleeAction();
        this.attackGoal.fireballcooldown = 200; // prevent double fireball by putting our other fireball ability on cooldown
        this.serverTriggerEvent(PROC_HALF_HEALTH_TIMER);
        this.serverTriggerAnimation("fire_boss_half_health_attack");
        this.playSound(SoundRegistry.BOSS_STANCE_BREAK.get(), 5, 2);
    }

    public void stopHalfHealthAttack() {
        halfHealthTimer = 0;
        setNoGravity(false);
        this.serverTriggerEvent(STOP_HALF_HEALTH_TIMER);
    }

    public boolean isHalfHealthAttacking() {
        return halfHealthTimer > 0;
    }

    public void triggerSpawnAnim() {
        this.spawnTimer = SPAWN_ANIM_TIME + SPAWN_DELAY;
    }

    public void triggerStanceBreak() {
        stanceBreakCounter++;
        stanceBreakTimer = STANCE_BREAK_ANIM_TIME;
        this.castComplete(); // interrupt casting
        this.attackGoal.stopMeleeAction(); // interrupt melee action
        this.stopHalfHealthAttack(); // interrupt half health ability
        this.serverTriggerAnimation("fire_boss_break_stance");
        this.playSound(SoundRegistry.BOSS_STANCE_BREAK.get(), 3, 1);
        Vec3 vec3 = this.getBoundingBox().getCenter();
        MagicManager.spawnParticles(level, ParticleRegistry.EMBEROUS_ASH_PARTICLE.get(), vec3.x, vec3.y, vec3.z, 25, 0.2, 0.2, 0.2, 0.12, false);
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundRegistry.FIRE_BOSS_HURT.get();
    }

    public boolean isStanceBroken() {
        return stanceBreakTimer > 0;
    }

    public boolean isSpawning() {
        return spawnTimer > 0;
    }

    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || isStanceBroken() || isSpawning() || isHalfHealthAttacking();
    }

    @Override
    public boolean isInvulnerableTo(DamageSource pSource) {
        return isSpawning() || isDespawning() || super.isInvulnerableTo(pSource);
    }

    @Override
    public boolean requiresCustomPersistence() {
        return true;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData) {
        super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData);
        RandomSource randomsource = Utils.random;
        this.populateDefaultEquipmentSlots(randomsource, pDifficulty);
        this.setLeftHanded(false);
        this.getAttribute(AttributeRegistry.MAX_MANA).addOrReplacePermanentModifier(MANA_MODIFIER);
        this.playerScale = pLevel.players().stream().filter(player -> distanceToSqr(player) < 3600 && !player.isSpectator() && !player.isCreative()).toList().size();
        int extraPlayers = Math.max(0, playerScale - 1);
        double extraHealthPercent = extraPlayers * 0.40 + extraPlayers * extraPlayers * 0.10;
        double extraHealth = ServerConfigs.TYROS_ADDITIONAL_HEALTH.get();
        double extraDamage = ServerConfigs.TYROS_ADDITIONAL_ATTACK_DAMAGE.get();
        double extraPower = ServerConfigs.TYROS_ADDITIONAL_SPELL_POWER.get();
        if (extraHealth != 0) {
            this.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(IronsSpellbooks.id("config"), extraHealth, AttributeModifier.Operation.ADD_VALUE));
        }
        if (extraHealthPercent != 0) {
            this.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(IronsSpellbooks.id("player_scale"), extraHealthPercent, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (extraDamage != 0) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(IronsSpellbooks.id("config"), extraDamage, AttributeModifier.Operation.ADD_VALUE));
        }
        if (extraPower != 0) {
            this.getAttribute(AttributeRegistry.SPELL_POWER).addPermanentModifier(new AttributeModifier(IronsSpellbooks.id("config"), extraPower, AttributeModifier.Operation.ADD_VALUE));
        }
        this.setHealth(this.getMaxHealth());
        return pSpawnData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(isSoulMode() ? ItemRegistry.HELLRAZOR : ItemRegistry.DECREPIT_SCYTHE));
        this.setDropChance(EquipmentSlot.MAINHAND, 0);
    }

    public void procSpectralDagger() {
        if (!level.isClientSide) {
            serverTriggerEvent(PROC_SPECTRAL_DAGGER);
        } else {
            clientDaggerParticles = true;
        }
        this.daggerTime = 15;
    }

    public boolean spectralDaggerActive() {
        return daggerTime > 0;
    }

    @Override
    public void tick() {
        super.tick();
        float maxHealth = this.getMaxHealth();
        float currentHealth = this.getHealth();
        this.bossEvent.setProgress(currentHealth / maxHealth);
        if (daggerTime > 0) {
            daggerTime--;
        }
        if (parryCooldown > 0) {
            parryCooldown--;
        }
        if (isSpawning()) {
            spawnTimer--;
            handleSpawnSequence();
            if (spawnTimer == 0 && !level.isClientSide) {
                spawnKnight(true);
                spawnKnight(false);
            }
        } else if (isDespawning()) {
            // reuse death time for fadeout animations
            deathTime++;
            if (!level.isClientSide) {
                deathParticles();
                if (getTarget() != null) {
                    // stop despawning if we re-aggro
                    setDespawning(false);
                }
                if (deathTime > 160) {
                    doForcedDespawned();
                }
            }
        } else if (deathTime > 0 && !isDeadOrDying()) {
            // quickly fade back in from despawn animation
            deathTime = Math.max(0, deathTime - 3);
        } else if (isHalfHealthAttacking()) {
            halfHealthTimer--;
            if (!level.isClientSide) {
                handleHalfHealthSequence();
            }
        }
        if (!level.isClientSide) {
            // while this is server-only logic, this cannot be in aistep because ai is disabled during stance breaks
            if (isStanceBroken()) {
                stanceBreakTimer--;
                handleStanceBreakSequence();
            }
            if (isSoulMode() && !dead) {
                soulParticles();
            }
        }
        if (destroyBlockDelay > 0) {
            --destroyBlockDelay;
        }
        if (stuckDetectorDelay > 0) {
            --stuckDetectorDelay;
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        float maxHealth = this.getMaxHealth();
        float currentHealth = this.getHealth();
        if (stanceBreakCounter == 0) {
            if (currentHealth < maxHealth * .75f) {
                triggerStanceBreak();
                return;
            }
        } else if (stanceBreakCounter == 1) {
            if (currentHealth < maxHealth * .333f) {
                triggerStanceBreak();
                return;
            }
        }
        if (!hasPerformedHalfHealthAttack && currentHealth < maxHealth * 0.5f) {
            triggerHalfHealthAttack();
        }
        if (tickCount > 400 && !isDespawning() && this.getTarget() == null && this.tickCount - this.getLastHurtByMobTimestamp() > 200) {
            if (tickCount % 20 == 0) {
                this.heal(5);
            }
            if (despawnAggroDelay++ > PROC_DESPAWN_SECONDS * 20) {
                setDespawning(true);
                level.playSound(null, this.blockPosition(), SoundRegistry.FIRE_BOSS_ACCENT.get(), SoundSource.HOSTILE, 4, 0.75f);
            }
        }
        if (this.isAggressive() && this.tickCount % (12 * 20) == 0) {
            int knightCount = level.getEntitiesOfClass(KeeperEntity.class, this.getBoundingBox().inflate(50, 20, 50)).size();
            if (knightCount < 2 + (Math.max(playerScale - 1, 0) / 2)) {
                spawnKnight(this.random.nextBoolean());
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity entity) {
        if (super.doHurtTarget(entity)) {
            entity.igniteForSeconds(2);
            return true;
        }
        return false;
    }

    private void handleHalfHealthSequence() {
        if (level.isClientSide) {
            return;
        }
        // force tick various controls while overall ai is turned off
        targetSelector.tick();
        if (this.getTarget() != null) {
            this.lookControl.setLookAt(this.getTarget());
        }
        lookControl.tick();
        if (halfHealthDamageAccumulated > getMaxHealth() * .10f) {
            PacketDistributor.sendToPlayersTrackingEntity(this, new FieryExplosionParticlesPacket(getBoundingBox().getCenter(), 10));
            // must be below half health already
            // must take 10% of max health as damage
            // distance to 1/3 health therefore is < 10%
            // thats an acceptable amount of damage to proc i think
            // triggering soul mode stance break is gonna be cinematic
            setHealth(Math.max(10, Math.min(getHealth(), getMaxHealth() * .33f - 1)));
            stopHalfHealthAttack();
            return;
        }
        int tick = HALF_HEALTH_ANIM_DURATION - halfHealthTimer;
        this.setDeltaMovement(getDeltaMovement().multiply(.1, 1, .1));
        if (tick == HALF_HEALTH_JUMP_TIMESTAMP) {
            // do jump
            this.setDeltaMovement(0, 0.75, 0);
        } else if (tick > HALF_HEALTH_JUMP_TIMESTAMP && tick < HALF_HEALTH_CAST_TIMESTAMP) {
            if (tick == HALF_HEALTH_JUMP_TIMESTAMP + 20) {
                this.setNoGravity(true);
            }
            // handle floating
            if (tick % 5 == 0) {
                int targetHeight = 8;
                var groundY = Utils.raycastForBlock(level, this.position(), this.position().subtract(0, targetHeight + 1, 0), ClipContext.Fluid.NONE).getLocation().y;
                this.push(0, getY() - groundY > targetHeight ? -0.02 : 0.02, 0);
            }
            //particles
            Vec3 vec3 = this.position().add(0, this.getBoundingBox().getYsize() * 1.25, 0);
            MagicManager.spawnParticles(level, ParticleHelper.FIRE_EMITTER, vec3.x, vec3.y, vec3.z, 1, .1, .1, .1, 0.03, true);
            // sounds
            if (tick % 10 == 0) {
                float pitch = Mth.lerp(tick / (float) HALF_HEALTH_ANIM_DURATION, 0.5f, 1.8f);
                this.playSound(SoundRegistry.SCORCH_PREPARE.get(), 2 + pitch, pitch);
            }
        } else if (tick == HALF_HEALTH_CAST_TIMESTAMP) {
            this.setNoGravity(false);

            MagicFireball fireball = new MagicFireball(level, this);

            fireball.setDamage((float) (getAttributeValue(Attributes.ATTACK_DAMAGE) * 12));
            fireball.setExplosionRadius(30);
            Vec3 origin = position().subtract(0, fireball.getBbHeight() / 2, 0).add(0, this.getBoundingBox().getYsize() * 1.25, 0);
            Vec3 trajectory = getTarget() == null ? this.getForward() : getTarget().position().subtract(origin).normalize();
            fireball.setPos(origin);
            fireball.shoot(trajectory);
            level.addFreshEntity(fireball);
            this.playSound(SoundRegistry.FIRE_BOSS_FIREBALL.get(), 4, 1f);
        }
    }

    private void handleStanceBreakSequence() {
        int tick = STANCE_BREAK_ANIM_TIME - stanceBreakTimer;
        if (stanceBreakCounter == 2) {
            // we will enter soul mode
            if (tick == 80) {
                this.setSoulMode(true);
                Vec3 vec3 = this.getBoundingBox().getCenter();
                MagicManager.spawnParticles(level, ParticleHelper.FIRE, vec3.x, vec3.y, vec3.z, 120, 0.3, 0.3, 0.3, 0.3, true);
                var speed = this.getAttribute(Attributes.MOVEMENT_SPEED);
                speed.removeModifier(SOUL_SPEED_MODIFIER);
                speed.addPermanentModifier(SOUL_SPEED_MODIFIER);
                var scale = this.getAttribute(Attributes.SCALE);
                scale.removeModifier(SOUL_SCALE_MODIFIER);
                scale.addPermanentModifier(SOUL_SCALE_MODIFIER);
                this.playSound(SoundRegistry.FIRE_BOSS_TRANSITION_SOUL.get(), 3, 1);
                if (this.getItemBySlot(EquipmentSlot.MAINHAND).is(ItemRegistry.DECREPIT_SCYTHE)) {
                    this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ItemRegistry.HELLRAZOR, 1, this.getItemBySlot(EquipmentSlot.MAINHAND).getComponentsPatch()));
                }
            } else if (tick < 80) {
                var f = Mth.lerp(tick / 80f, 0.2, 0.4);
                Vec3 vec3 = this.getBoundingBox().getCenter();
                MagicManager.spawnParticles(level, ParticleHelper.FIRE, vec3.x, vec3.y, vec3.z, 12 + (int) (f * 10), f, f, f, 0.02, true);
            }
        }
        if (tick >= STANCE_BREAK_BEGIN_SLAMS_TIMESTAMP) {
            if (tick == STANCE_BREAK_BEGIN_SLAMS_TIMESTAMP) {
                createEruptionEntity(8, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE));
                playSound(SoundRegistry.FIRE_ERUPTION_SLAM.get(), 2, 1.2f);
            } else if (tick == STANCE_BREAK_BEGIN_SLAMS_TIMESTAMP + 25) {
                createEruptionEntity(11, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2);
                playSound(SoundRegistry.FIRE_ERUPTION_SLAM.get(), 3, 1f);
            } else if (tick == STANCE_BREAK_BEGIN_SLAMS_TIMESTAMP + 50) {
                createEruptionEntity(15, (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE) * 3);
                playSound(SoundRegistry.FIRE_ERUPTION_SLAM.get(), 4, 0.9f);
            }
        }
    }

    private void handleSpawnSequence() {
        int animProgress = SPAWN_ANIM_TIME + SPAWN_DELAY - spawnTimer; // counts up to max (whereas timer counts down from max)
        float walkProgress = getSpawnWalkPercent(0); // 0-1f, percent progress of the spawn animation from starting to walk to finishing animation
        float worldZOffset = Mth.lerp(walkProgress, -60 / 16f * getScale(), 0);
        Vec3 position = this.position().add(new Vec3(0, 0, worldZOffset).yRot(-this.getYRot() * Mth.DEG_TO_RAD));
        // timed delay to sync beginning of music with beginning of fight
        if (!level.isClientSide && animProgress == 65) {
            this.serverTriggerEvent(START_MUSIC);
        }
        if (animProgress == SPAWN_DELAY) {
            // begin walking out of puff of smoke
            if (!level.isClientSide) {
                //smoke to step out of
                MagicManager.spawnParticles(level, ParticleTypes.CAMPFIRE_COSY_SMOKE, position.x, position.y + 1.2, position.z, (int) (165 * getScale()), 0.4 * getScale(), 1.0 * getScale(), 0.4 * getScale(), 0.01, true);
                MagicManager.spawnParticles(level, ParticleHelper.FOG_CAMPFIRE_SMOKE, position.x, position.y + 0.1, position.z, 6, 0.6, .1, 0.6, 0.05, true);
                // responding bell toll echo
                MagicManager.spawnParticles(level, new BlastwaveParticleOptions(1, .6f, 0.3f, 8), position.x, position.y, position.z, 0, 0, 0, 0, 0, true);
                serverTriggerAnimation("fire_boss_spawn");
            }
            level.playSound(null, position.x, position.y, position.z, SoundRegistry.SOULCALLER_TOLL_SUCCESS, SoundSource.PLAYERS, 5f, .75f);
        }
        //step sounds
        if (animProgress == SPAWN_DELAY + 20 || animProgress == SPAWN_DELAY + 40 || animProgress == SPAWN_DELAY + 60 || animProgress == SPAWN_DELAY + 80 || animProgress == SPAWN_DELAY + 100 || animProgress == SPAWN_DELAY + 114 || animProgress == SPAWN_DELAY + 128) {
            level.playSound(null, position.x, position.y, position.z, SoundRegistry.KEEPER_STEP, this.getSoundSource(), 0.4f, 1f);
        }
        // summon scythe sound (happens at tick 132, with 17 tick windup)
        if (animProgress == SPAWN_DELAY + 132 - 17) {
            level.playSound(null, position.x, position.y, position.z, SoundRegistry.FIRE_BOSS_SUMMON_SCYTHE, this.getSoundSource(), 3f, 1f);
        }
    }

    /**
     * @return 0-1f, percent progress of the spawn animation from starting to walk to finishing animation
     */
    protected float getSpawnWalkPercent(float partialTick) {
        return Math.clamp((SPAWN_ANIM_TIME - spawnTimer + partialTick) / (float) SPAWN_ANIM_TIME, 0, 1);
    }

    private void doForcedDespawned() {
        // discard
        this.playSound(SoundRegistry.FIRE_BOSS_ACCENT.get(), 5, 1);
        Vec3 vec3 = this.getBoundingBox().getCenter();
        MagicManager.spawnParticles(level, ParticleRegistry.EMBEROUS_ASH_PARTICLE.get(), vec3.x, vec3.y, vec3.z, 25, 0.2, 0.2, 0.2, 0.12, false);
        killNearbySummonedKnights();
        remove(RemovalReason.DISCARDED);
        IronsSpellbooks.LOGGER.info("{} despawned due to inactivity", this);
    }

    public void spawnKnight(boolean left) {
        if (level instanceof ServerLevel serverLevel) {
            KeeperEntity knight = new KeeperEntity(level);
            float angle = (left ? -90 : 90) * Mth.DEG_TO_RAD;
            Vec3 offset = this.getForward().multiply(3, 0, 3).scale(this.getScale()).yRot(angle);
            Vec3 spawn = Utils.moveToRelativeGroundLevel(level, Utils.raycastForBlock(level, this.getEyePosition(), this.position().add(offset), ClipContext.Fluid.NONE).getLocation(), 4);
            knight.moveTo(spawn.add(0, 0.1, 0));
            knight.triggerRise();
            knight.setYRot(this.getYRot());
            knight.setIsSummoned();
            if (isSoulMode()) {
                knight.setIsRestored();
            }
            knight.finalizeSpawn(serverLevel, level.getCurrentDifficultyAt(this.blockPosition()), MobSpawnType.MOB_SUMMONED, null);
            level.addFreshEntity(knight);
            level.playSound(null, spawn.x, spawn.y, spawn.z, SoundRegistry.FIRE_BOSS_ACCENT.get(), this.getSoundSource(), 2, .9f);
        }
    }

    public void soulParticles() {
        Vec3 vec3 = this.getBoundingBox().getCenter();
        MagicManager.spawnParticles(level, ParticleHelper.FIRE, vec3.x, vec3.y, vec3.z, 2, 0.2, 0.6, 0.2, 0.01, true);
    }

    private void createEruptionEntity(float radius, float damage) {
        Vec3 forward = this.getForward().multiply(1, 0, 1).normalize().scale(3);
        Vec3 pos = Utils.moveToRelativeGroundLevel(level, this.position().add(forward).add(0, 1, 0), 4);
        FireEruptionAoe aoe = new FireEruptionAoe(level, radius);
        aoe.setOwner(this);
        aoe.setDamage(damage);
        aoe.moveTo(pos);
        level.addFreshEntity(aoe);
        CameraShakeManager.addCameraShake(new CameraShakeData(level, 20 + (int) radius, pos, radius * 2 + 5));
    }

    SimpleContainer deathLoot = null;

    @Override
    public void kill() {
        if (this.isDeadOrDying() || this.isSpawning()) {
            discard();
        } else {
            super.kill();
        }
    }

    @Override
    public void die(DamageSource pDamageSource) {
        super.die(pDamageSource);
        if (this.isDeadOrDying() && !this.level.isClientSide) {
            this.stanceBreakTimer = 0;
            this.castComplete();
            this.attackGoal.stop();
            this.serverTriggerAnimation("fire_boss_death");
            this.serverTriggerEvent(STOP_MUSIC);
            this.playSound(SoundRegistry.FIRE_BOSS_DEATH.get(), 5, 1);
            Vec3 vec3 = this.getBoundingBox().getCenter();
            MagicManager.spawnParticles(level, ParticleRegistry.EMBEROUS_ASH_PARTICLE.get(), vec3.x, vec3.y, vec3.z, 25, 0.2, 0.2, 0.2, 0.12, false);
            killNearbySummonedKnights();
        }
    }

    private void killNearbySummonedKnights() {
        level.getEntitiesOfClass(KeeperEntity.class, this.getBoundingBox().inflate(50, 20, 50)).stream().filter(KeeperEntity::isSummoned).forEach(LivingEntity::kill);
    }

    @Override
    protected void dropAllDeathLoot(ServerLevel pLevel, DamageSource pDamageSource) {
        // prevent drops from appearing before death animation, just store them
        this.dropEquipment();
        this.dropExperience(pDamageSource.getEntity());
        boolean playerDeath = this.lastHurtByPlayerTime > 0;
        this.dropCustomDeathLoot(pLevel, pDamageSource, playerDeath);
        ResourceKey<LootTable> resourcekey = this.getLootTable();
        LootTable mainLoot = this.level.getServer().reloadableRegistries().getLootTable(resourcekey);
        LootTable lootPerPlayer = this.level.getServer().reloadableRegistries().getLootTable(ResourceKey.create(resourcekey.registryKey(), resourcekey.location().withSuffix("_per_player")));
        LootParams.Builder lootparams$builder = new LootParams.Builder(pLevel)
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .withParameter(LootContextParams.ORIGIN, this.position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, pDamageSource)
                .withOptionalParameter(LootContextParams.ATTACKING_ENTITY, pDamageSource.getEntity())
                .withOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, pDamageSource.getDirectEntity());
        if (playerDeath && this.lastHurtByPlayer != null) {
            lootparams$builder = lootparams$builder.withParameter(LootContextParams.LAST_DAMAGE_PLAYER, this.lastHurtByPlayer)
                    .withLuck(this.lastHurtByPlayer.getLuck());
        }

        LootParams lootparams = lootparams$builder.create(LootContextParamSets.ENTITY);
        ObjectArrayList<ItemStack> objectarraylist = new ObjectArrayList<>();
        mainLoot.getRandomItems(lootparams, this.getLootTableSeed(), objectarraylist::add);
        for (int i = 0; i < playerScale; i++) {
            lootPerPlayer.getRandomItems(lootparams, this.getLootTableSeed(), objectarraylist::add);
        }
        this.deathLoot = new SimpleContainer(objectarraylist.size());
        objectarraylist.forEach(deathLoot::addItem);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (!level.isClientSide) {
            float scale = getScale();
            Vec3 vec3 = this.position();
            deathParticles();
            if (this.deathTime >= 160 && !this.level().isClientSide() && !this.isRemoved()) {
                if (this.deathLoot != null) {
                    deathLoot.getItems().forEach(this::spawnAtLocation);
                }
                this.remove(Entity.RemovalReason.KILLED);
                MagicManager.spawnParticles(level, ParticleRegistry.EMBEROUS_ASH_PARTICLE.get(), vec3.x, vec3.y + 1, vec3.z, 50, 0.3, 0.3, 0.3, 0.2 * scale, true);
                this.playSound(SoundRegistry.FIRE_BOSS_ACCENT.get(), 4, .9f);
            }
        }
    }

    private void deathParticles() {
        float scale = getScale();
        Vec3 vec3 = this.position();
        int particles = (int) Mth.lerp(Math.clamp((deathTime - 20) / 60f, 0, 1), 0, 5 * scale);
        float range = Mth.lerp(Math.clamp((deathTime - 20) / 80f, 0, 1), 0, 0.4f * scale);
        if (particles > 0) {
            MagicManager.spawnParticles(level, ParticleRegistry.EMBEROUS_ASH_PARTICLE.get(), vec3.x, vec3.y + 1, vec3.z, particles, range, range, range, 100, false);
        }
    }

    @Override
    public void calculateEntityAnimation(boolean pIncludeHeight) {
        super.calculateEntityAnimation(false);
    }

    @Override
    protected void updateWalkAnimation(float f) {
        //reduce walk animation swing if we are floating or meleeing
        super.updateWalkAnimation(f * (!this.onGround() ? .5f : (this.isSoulMode() ? .7f : .9f)));
    }

    @Override
    public boolean bobBodyWhileWalking() {
        return !isAnimating();
    }

    @Override
    protected void playStepSound(BlockPos pPos, BlockState pState) {
        this.playSound(SoundRegistry.KEEPER_STEP.get(), .25f, .9f);
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(AttributeRegistry.SPELL_POWER, 1.25)
                .add(Attributes.ARMOR, 15)
                .add(AttributeRegistry.SPELL_RESIST, 1.25)
                .add(AttributeRegistry.FIRE_MAGIC_RESIST, 1.5)
                .add(Attributes.MAX_HEALTH, 1000)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.ATTACK_KNOCKBACK, .6)
                .add(Attributes.FOLLOW_RANGE, 48.0)
                .add(Attributes.SCALE, 1.75)
                .add(Attributes.GRAVITY, 0.03)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
                .add(Attributes.STEP_HEIGHT, 1)
                .add(Attributes.MOVEMENT_SPEED, .21);
    }

    @Override
    public void push(Entity pEntity) {
        if (!isSpawning()) {
            super.push(pEntity);
        }
    }

    @Override
    public void knockback(double pStrength, double pX, double pZ) {
        if (isStanceBroken()) {
            return;
        }
        super.knockback(pStrength, pX, pZ);
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && !isImmobile();
    }

    RawAnimation animationToPlay = null;
    private final AnimationController<FireBossEntity> meleeController = new AnimationController<>(this, "melee_animations", 0, this::predicate);

    @Override
    public void playAnimation(String animationId) {
        animationToPlay = RawAnimation.begin().thenPlay(animationId);
        canAnimateOver = animationId.equals("fire_boss_spawn") || animationId.equals("summon_fiery_daggers");
        stopHeadAnimation = animationId.equals("fire_boss_break_stance") || animationId.equals("fire_boss_death");
    }

    @Override
    public boolean shouldAlwaysAnimateHead() {
        return !stopHeadAnimation;
    }

    private PlayState predicate(AnimationState<FireBossEntity> animationEvent) {
        var controller = animationEvent.getController();

        if (this.animationToPlay != null) {
            controller.forceAnimationReset();
            controller.setAnimation(animationToPlay);
            animationToPlay = null;
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(meleeController);
        super.registerControllers(controllerRegistrar);
    }

    @Override
    public boolean isAnimating() {
        return (meleeController.getAnimationState() == AnimationController.State.RUNNING && !canAnimateOver) || super.isAnimating();
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (level.isClientSide) {
            return false;
        }
        /*
        can parry:
        - serverside
        - in combat
        - we aren't in melee attack anim or spell cast
        - the damage source is caused by an entity (ie not fall damage)
        - the damage is caused within our rough field of vision (117 degrees)
        - the damage is not /kill
         */
        boolean canParry = this.isAggressive() &&
                parryCooldown <= 0 &&
                !isImmobile() &&
                !attackGoal.isActing() &&
                pSource.getEntity() != null &&
                pSource.getSourcePosition() != null && pSource.getSourcePosition().subtract(this.position()).normalize().dot(this.getForward()) >= 0.35
                && !pSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
        if (canParry && this.random.nextFloat() < 0.5) {
            //todo: dynamic parry chance (recent hits, ominious mode, damage type, etc)
            serverTriggerAnimation("offhand_parry");
            procSpectralDagger();
            this.parryCooldown = 100;
            this.playSound(SoundRegistry.FIRE_DAGGER_PARRY.get());
            return false;
        }
        if (isStanceBroken()) {
            pAmount *= 0.60f;
        }
        if (isSoulMode()) {
            pAmount *= 0.50f;
        }
        if (isHalfHealthAttacking()) {
            pAmount *= 0.80f;
        }
        // damage limiter
        var limit = getMaxHealth() * 0.025f;
        if (pAmount > limit) {
            pAmount = limit + (pAmount - limit) * .3f; // damage about limit has .3x multiplier applied
        }
        if (pSource.is(DamageTypes.IN_WALL) && this.destroyBlockDelay <= 0) {
            Utils.doMobBreakSuffocatingBlocks(this);
            destroyBlockDelay = 40;
        }
        return super.hurt(pSource, pAmount);
    }

    @Override
    protected void actuallyHurt(DamageSource damageSource, float damageAmount) {
        super.actuallyHurt(damageSource, damageAmount);
        if (isHalfHealthAttacking()) {
            halfHealthDamageAccumulated += damageAmount;
        }
        Vec3 oldStuckPos = this.lastStuckPos;
        this.lastStuckPos = this.position();
        if (stuckDetectorDelay <= 0) {
            if (oldStuckPos.distanceToSqr(lastStuckPos) < 3 * 3 && !isImmobile()) {
                stuckDetectorDelay = 20;
                if (horizontalCollision) {
                    stuckDetector++;
                }
            } else {
                stuckDetector = 0;
            }
        }
        if (stuckDetector >= 3 && this.destroyBlockDelay <= 0) {
            Utils.doMobBreakSuffocatingBlocks(this, this.getForward().scale(1.5));
            stuckDetector = 0;
            destroyBlockDelay = 40;
        }
    }

    public boolean isSoulMode() {
        return entityData.get(DATA_SOUL_MODE);
    }

    public void setSoulMode(boolean soulMode) {
        entityData.set(DATA_SOUL_MODE, soulMode);
    }

    public boolean isDespawning() {
        return entityData.get(DATA_IS_DESPAWNING);
    }

    public void setDespawning(boolean despawning) {
        entityData.set(DATA_IS_DESPAWNING, despawning);
        if (!despawning) {
            despawnAggroDelay = 0;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("stanceBreakCount", stanceBreakCounter);
        pCompound.putInt("playerScale", playerScale);
        if (stanceBreakTimer > 0) {
            pCompound.putInt("stanceBreakTime", stanceBreakTimer);
        }
        pCompound.putBoolean("soulMode", isSoulMode());
        if (deathLoot != null) {
            pCompound.put("deathLootItems", deathLoot.createTag(this.registryAccess()));
        }
        pCompound.putLong("unloadedGametime", level.getGameTime());
        pCompound.putInt("halfHealthTimer", halfHealthTimer);
        pCompound.putFloat("halfHealthDamage", halfHealthDamageAccumulated);
        pCompound.putBoolean("halfHealthAttack", hasPerformedHalfHealthAttack);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.stanceBreakCounter = pCompound.getInt("stanceBreakCount");
        this.playerScale = pCompound.getInt("playerScale");
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
        int stanceTime = pCompound.getInt("stanceBreakTime");
        if (stanceTime > 0) {
            this.stanceBreakTimer = stanceTime;
            if (level.isClientSide) {
                //todo: sync anim to completed time
                this.animationToPlay = RawAnimation.begin().thenPlay("fire_boss_break_stance");
            }
        }

        this.setSoulMode(pCompound.getBoolean("soulMode"));
        if (pCompound.contains("deathLootItems", 9)) { // 9 for list tag
            var tag = pCompound.getList("deathLootItems", 10);
            this.deathLoot = new SimpleContainer(tag.size());
            this.deathLoot.fromTag(tag, this.registryAccess());
        }
        this.halfHealthTimer = pCompound.getInt("halfHealthTimer");
        this.halfHealthDamageAccumulated = pCompound.getFloat("halfHealthDamage");
        this.hasPerformedHalfHealthAttack = pCompound.getBoolean("halfHealthAttack");
    }

    @Override
    public void load(CompoundTag pCompound) {
        if (pCompound.contains("unloadedGametime", 99)) {
            var unloadTimestamp = pCompound.getLong("unloadedGametime");
            var delta = level.getGameTime() - unloadTimestamp;
            if (delta > UNLOADED_DESPAWN_LIMIT_SECONDS * 20) {
                this.setRemoved(RemovalReason.DISCARDED);
                IronsSpellbooks.LOGGER.info("Refusing to load {}, elapsed time {} greater than limit {}", this, delta, UNLOADED_DESPAWN_LIMIT_SECONDS * 20);
                return;
            }
        }
        super.load(pCompound);
        if (!level.isClientSide) {
            // re-sync uuid if we are loading from file rather than creating new entity (uuid is loaded in super.load)
            createBossEvent();
        }
    }

    @Override
    public boolean isAlliedTo(Entity pEntity) {
        return super.isAlliedTo(pEntity) || pEntity.getType().is(ModTags.INFERNAL_ALLIES);
    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        return new NotIdioticNavigation(this, pLevel);
    }

    protected void createBossEvent() {
        this.bossEvent = (ExtendedServerBossEvent) (new ExtendedServerBossEvent(this.getUUID(), this.getDisplayName().copy().withStyle(ChatFormatting.RED/*, ChatFormatting.BOLD*/), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS)).setCreateWorldFog(true);
    }
}
