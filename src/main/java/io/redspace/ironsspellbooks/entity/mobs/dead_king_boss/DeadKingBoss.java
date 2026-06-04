package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.backwards_compat.AttributeHelper;
import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.api.entity.IOminousEntity;
import io.redspace.ironsspellbooks.api.events.SetSummonOwnerEvent;
import io.redspace.ironsspellbooks.api.network.IClientEventEntity;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.util.BossbarManager;
import io.redspace.ironsspellbooks.api.util.MusicManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.config.ServerConfigs;
import io.redspace.ironsspellbooks.entity.mobs.IAnimatedAttacker;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.audio.DeadKingMusicHandler;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.goals.CreateFangSwirlGoal;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.goals.CreateUndeadRiftGoal;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.goals.DeadKingAnimatedWarlockAttackGoal;
import io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.goals.NotIdioticFlyingMoveControl;
import io.redspace.ironsspellbooks.entity.mobs.goals.MomentHurtByTargetGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.PatrolNearLocationGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.SpellBarrageGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.melee.AttackAnimationData;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.ExtendedServerBossEvent;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.FireBossEntity;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.NotIdioticNavigation;
import io.redspace.ironsspellbooks.loot.BossLootHandler;
import io.redspace.ironsspellbooks.network.EntityEventPacket;
import io.redspace.ironsspellbooks.particle.SwirlingParticleOptions;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.ItemRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import io.redspace.ironsspellbooks.util.NBT;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

import javax.annotation.Nullable;
import java.util.List;

@EventBusSubscriber
public class DeadKingBoss extends AbstractSpellCastingMob implements Enemy, IAnimatedAttacker, IClientEventEntity, IOminousEntity {
    private static final AttributeModifier OMINOUS_DAMAGE_MODIFIER = new AttributeModifier(AttributeHelper.uuidFromId(IronsSpellbooks.id("ominous_mode")), "ominous_mode", 0.20, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier OMINOUS_SPEED_MODIFIER = new AttributeModifier(AttributeHelper.uuidFromId(IronsSpellbooks.id("ominous_mode")), "ominous_mode", 0.10, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier OMINOUS_SUMMON_MODIFIER = new AttributeModifier(AttributeHelper.uuidFromId(IronsSpellbooks.id("ominous_mode")), "ominous_mode", 0.50, AttributeModifier.Operation.MULTIPLY_TOTAL);
    private static final AttributeModifier OMINOUS_ARMOR_MODIFIER = new AttributeModifier(AttributeHelper.uuidFromId(IronsSpellbooks.id("ominous_mode")), "ominous_mode", 30, AttributeModifier.Operation.ADDITION);

    public static final byte CLIENT_STOP_TRACKING = 0;
    public static final byte CLIENT_START_TRACKING = 1;

    @SubscribeEvent
    public static void sacrificialMarkHandler(SetSummonOwnerEvent event) {
        if (event.getOwner() instanceof DeadKingBoss boss && event.getSummon() instanceof LivingEntity living) {
            if (!boss.isOminous()) {
                return;
            }
            float f = Mth.lerp(1 - boss.getHealth() / boss.getMaxHealth(), .2f, .75f);
            if (boss.getRandom().nextFloat() < f) {
                int maxLevel = SpellRegistry.SACRIFICE_SPELL.get().getMaxLevel();
                int spellLevel = Mth.clamp(Mth.floor(f * (maxLevel - 1) + 1), 1, maxLevel);
                living.addEffect(new MobEffectInstance(MobEffectRegistry.SACRIFICIAL_MARK.get(), Integer.MAX_VALUE, spellLevel - 1, false, false, true));
            }
        }
    }

    @Override
    public void onOminousTrigger() {
        this.setIsOminous(true);
        this.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(OMINOUS_DAMAGE_MODIFIER);
        this.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(OMINOUS_DAMAGE_MODIFIER);
        this.getAttribute(AttributeRegistry.SPELL_POWER.get()).removeModifier(OMINOUS_DAMAGE_MODIFIER);
        this.getAttribute(AttributeRegistry.SPELL_POWER.get()).addPermanentModifier(OMINOUS_DAMAGE_MODIFIER);
        this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(OMINOUS_SPEED_MODIFIER);
        this.getAttribute(Attributes.MOVEMENT_SPEED).addPermanentModifier(OMINOUS_SPEED_MODIFIER);
        this.getAttribute(Attributes.FLYING_SPEED).removeModifier(OMINOUS_SPEED_MODIFIER);
        this.getAttribute(Attributes.FLYING_SPEED).addPermanentModifier(OMINOUS_SPEED_MODIFIER);
        this.getAttribute(AttributeRegistry.SUMMON_DAMAGE.get()).removeModifier(OMINOUS_SUMMON_MODIFIER);
        this.getAttribute(AttributeRegistry.SUMMON_DAMAGE.get()).addPermanentModifier(OMINOUS_SUMMON_MODIFIER);
        this.getAttribute(Attributes.ARMOR).removeModifier(OMINOUS_ARMOR_MODIFIER);
        this.getAttribute(Attributes.ARMOR).addPermanentModifier(OMINOUS_ARMOR_MODIFIER);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        this.setHealth(getMaxHealth());
    }

    @Override
    public boolean isOminous() {
        return entityData.get(IS_OMINOUS);
    }

    public void setIsOminous(boolean isOminous) {
        this.entityData.set(IS_OMINOUS, isOminous);
    }

    @Override
    public void handleClientEvent(byte eventId) {
        switch (eventId) {
            case CLIENT_STOP_TRACKING -> {
                MusicManager.stopEvent(this.getUUID());
                BossbarManager.stopTracking(this.uuid);
            }
            case CLIENT_START_TRACKING -> {
                BossbarManager.startTracking(this.uuid, BOSSBAR_SPRITE);
                MusicManager.createEvent(this, new DeadKingMusicHandler(this));
            }
        }
    }

    private static final BossbarManager.BossbarSprite BOSSBAR_SPRITE = new BossbarManager.BossbarSprite(IronsSpellbooks.id("boss_bars/dead_king_bossbar"), 192, 21, 3, -2);

    public DeadKingBoss(Level pLevel) {
        this(EntityRegistry.DEAD_KING.get(), pLevel);
        setPersistenceRequired();
    }


    public enum Phases {
        FirstPhase(0),
        Transitioning(1),
        FinalPhase(2);
        final int value;

        Phases(int value) {
            this.value = value;
        }
    }

    public enum AttackType {
        DOUBLE_SWING(51, "dead_king_double_swing", 16, 36),
        SLAM(48, "dead_king_slam", 30);

        AttackType(int lengthInTicks, String animationId, int... attackTimestamps) {
            this.data = new AttackAnimationData(lengthInTicks, animationId, attackTimestamps);
        }

        public final AttackAnimationData data;
    }

    private static final AttributeModifier MANA_MODIFIER = new AttributeModifier(AttributeHelper.uuidFromId(IronsSpellbooks.id("mana")), "mana", 2000, AttributeModifier.Operation.ADDITION);
    private final static EntityDataAccessor<Integer> PHASE = SynchedEntityData.defineId(DeadKingBoss.class, EntityDataSerializers.INT);
    private final static EntityDataAccessor<Boolean> IS_OMINOUS = SynchedEntityData.defineId(DeadKingBoss.class, EntityDataSerializers.BOOLEAN);
    private int transitionAnimationTime = 139; // Animation Length in ticks
    private boolean isCloseToGround;
    public boolean isMeleeing;
    private int destroyBlockDelay;
    private ExtendedServerBossEvent bossEvent;
    private int playerScale;
    private final BossLootHandler bossLoot = new BossLootHandler();

    @Nullable
    private Vec3 spawnPos;

    public DeadKingBoss(EntityType<? extends AbstractSpellCastingMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setPersistenceRequired();
        xpReward = 60;
        this.lookControl = createLookControl();
        this.moveControl = createMoveControl();
        createBossEvent();
    }

    private DeadKingAnimatedWarlockAttackGoal getCombatGoal() {
        return (DeadKingAnimatedWarlockAttackGoal) new DeadKingAnimatedWarlockAttackGoal(this, 1f, 55, 85).setMeleeAttackInverval(0, 20).setSpellQuality(.3f, .5f).setSpells(
                List.of(
                        SpellRegistry.RAY_OF_SIPHONING_SPELL.get(),
                        SpellRegistry.BLOOD_SLASH_SPELL.get(), SpellRegistry.BLOOD_SLASH_SPELL.get(),
                        SpellRegistry.WITHER_SKULL_SPELL.get(), SpellRegistry.WITHER_SKULL_SPELL.get(), SpellRegistry.WITHER_SKULL_SPELL.get(),
                        SpellRegistry.FANG_STRIKE_SPELL.get(), SpellRegistry.FANG_STRIKE_SPELL.get(),
                        SpellRegistry.POISON_ARROW_SPELL.get(), SpellRegistry.POISON_ARROW_SPELL.get(),
                        SpellRegistry.BLIGHT_SPELL.get(),
                        SpellRegistry.ACID_ORB_SPELL.get()
                ),
                List.of(SpellRegistry.FANG_WARD_SPELL.get(), SpellRegistry.BLOOD_STEP_SPELL.get()),
                List.of(/*SpellType.BLOOD_STEP_SPELL*/),
                List.of()
        ).setMeleeBias(0.8f, 0.8f).setAllowFleeing(false);
    }

    @Override
    protected void registerGoals() {
        setFirstPhaseGoals();
        this.targetSelector.addGoal(1, new MomentHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, Villager.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, AbstractIllager.class, true));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, FireBossEntity.class, true));
    }

    protected void setGenericGoals() {
        this.goalSelector.addGoal(0, new CreateUndeadRiftGoal(this));
        this.goalSelector.addGoal(0, new CreateFangSwirlGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(5, new PatrolNearLocationGoal(this, 32, 0.9f));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));

        // Use root in ominous mode
        this.goalSelector.addGoal(3, new DeadKingBarrageGoal(this, SpellRegistry.ROOT_SPELL.get(), 1, 1, 200, 400, 1) {
            @Override
            public boolean canUse() {
                return isOminous() && super.canUse();
            }
        });
    }

    protected void setFirstPhaseGoals() {
        this.goalSelector.getAvailableGoals().forEach(WrappedGoal::stop);
        this.goalSelector.removeAllGoals((x) -> true);
        this.goalSelector.addGoal(1, new DeadKingBarrageGoal(this, SpellRegistry.WITHER_SKULL_SPELL.get(), 3, 4, 70, 140, 3));
        this.goalSelector.addGoal(2, new DeadKingBarrageGoal(this, SpellRegistry.RAISE_DEAD_SPELL.get(), 4, 4, 400, 600, 1));
        this.goalSelector.addGoal(3, new DeadKingBarrageGoal(this, SpellRegistry.BLOOD_STEP_SPELL.get(), 1, 1, 100, 180, 1));
        this.goalSelector.addGoal(4, getCombatGoal().setSingleUseSpell(SpellRegistry.RAISE_DEAD_SPELL.get(), 20, 20, 8, 8));
        setGenericGoals();
    }

    protected void setFinalPhaseGoals() {
        this.goalSelector.getAvailableGoals().forEach(WrappedGoal::stop);
        this.goalSelector.removeAllGoals((x) -> true);
        this.goalSelector.addGoal(1, new DeadKingBarrageGoal(this, SpellRegistry.WITHER_SKULL_SPELL.get(), 5, 5, 60, 140, 4));
        this.goalSelector.addGoal(2, new DeadKingBarrageGoal(this, SpellRegistry.SUMMON_VEX_SPELL.get(), 2, 4, 200, 400, 1));
        this.goalSelector.addGoal(3, new DeadKingBarrageGoal(this, SpellRegistry.BLOOD_STEP_SPELL.get(), 1, 1, 100, 180, 1));
        this.goalSelector.addGoal(4, getCombatGoal().setIsFlying().setSingleUseSpell(SpellRegistry.BLAZE_STORM_SPELL.get(), 10, 30, 10, 10));
        this.hasUsedSingleAttack = false;
        this.moveControl = new NotIdioticFlyingMoveControl(this, 30, true);
        setGenericGoals();
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundRegistry.DEAD_KING_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return SoundRegistry.DEAD_KING_DEATH.get();
    }

    @Override
    public void handleEntityEvent(byte pId) {
        if (pId == 3) {
            //play death sound event
        } else {
            super.handleEntityEvent(pId);
        }
    }

    @Override
    public float getVoicePitch() {
        return 1f;
    }

    @Override
    public boolean isPushable() {
        return false;//!isPhaseTransitioning();
    }

    @Override
    public @org.jetbrains.annotations.Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @org.jetbrains.annotations.Nullable SpawnGroupData pSpawnData, @org.jetbrains.annotations.Nullable CompoundTag pDataTag) {
        RandomSource randomsource = Utils.random;
        this.populateDefaultEquipmentSlots(randomsource, pDifficulty);
        this.getAttribute(AttributeRegistry.MAX_MANA.get()).addPermanentModifier(MANA_MODIFIER);
        List<? extends Player> nearbyPlayers = pLevel.players().stream()
                .filter(player -> distanceToSqr(player) < 3600 && !player.isSpectator() && !player.isCreative())
                .toList();
        this.playerScale = nearbyPlayers.size();
        this.bossLoot.setParticipantsFromPlayers(nearbyPlayers);
        int extraPlayers = Math.max(0, playerScale - 1);
        double extraHealthPercent = extraPlayers * 0.40 + extraPlayers * extraPlayers * 0.10;
        double extraHealth = ServerConfigs.DEAD_KING_ADDITIONAL_HEALTH.get();
        double extraDamage = ServerConfigs.DEAD_KING_ADDITIONAL_ATTACK_DAMAGE.get();
        double extraPower = ServerConfigs.DEAD_KING_ADDITIONAL_SPELL_POWER.get();
        if (extraHealth != 0) {
            this.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(AttributeHelper.uuidFromId(IronsSpellbooks.id("config")), "config", extraHealth, AttributeModifier.Operation.ADDITION));
        }
        if (extraHealthPercent != 0) {
            this.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier(AttributeHelper.uuidFromId(IronsSpellbooks.id("player_scale")), "player_scale", extraHealthPercent, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        if (extraDamage != 0) {
            this.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier(AttributeHelper.uuidFromId(IronsSpellbooks.id("config")), "config", extraDamage, AttributeModifier.Operation.ADDITION));
        }
        if (extraPower != 0) {
            this.getAttribute(AttributeRegistry.SPELL_POWER.get()).addPermanentModifier(new AttributeModifier(AttributeHelper.uuidFromId(IronsSpellbooks.id("config")), "config", extraPower, AttributeModifier.Operation.ADDITION));
        }
        this.setHealth(this.getMaxHealth());
        return pSpawnData;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource pRandom, DifficultyInstance pDifficulty) {
        this.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ItemRegistry.BLOOD_STAFF.get()));
        this.setDropChance(EquipmentSlot.OFFHAND, 0f);
    }

    @Override
    public boolean isAlliedTo(Entity pEntity) {
        return super.isAlliedTo(pEntity) || (pEntity instanceof IMagicSummon summon && summon.getSummoner() == this);
    }

    //Instead of being undead (smite is ridiculous)
    @Override
    public boolean isInvertedHealAndHarm() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) {
            clientAmbientParticles();
        } else {
            float halfHealth = this.getMaxHealth() / 2;
            if (isPhase(Phases.FirstPhase)) {
                this.bossEvent.setProgress((this.getHealth() - halfHealth) / (this.getMaxHealth() - halfHealth));
                if (this.getHealth() <= halfHealth) {
                    setPhase(Phases.Transitioning);
                    if (!isDeadOrDying()) {
                        setHealth(halfHealth);
                    }
                    playSound(SoundRegistry.DEAD_KING_FAKE_DEATH.get());
                    setInvulnerable(true);
                    this.getCombatGoal().stop();
                    this.cancelCast();
                }
            } else if (isPhase(Phases.Transitioning)) {
                if (--transitionAnimationTime <= 0) {
                    setPhase(Phases.FinalPhase);
                    var particle = this.isOminous() ? ParticleHelper.SOUL_FIRE : ParticleHelper.FIRE;
                    MagicManager.spawnParticles(level, particle, position().x, position().y + 2.5, position().z, 80, .2, .2, .2, .25, true);
                    setFinalPhaseGoals();
                    setNoGravity(true);
                    playSound(SoundRegistry.DEAD_KING_EXPLODE.get());
                    level.getEntities(this, this.getBoundingBox().inflate(5), (entity) -> entity instanceof LivingEntity && entity.isPickable() && entity.distanceToSqr(position()) < 5 * 5).forEach(super::doHurtTarget);
                    setInvulnerable(false);
                }
            } else if (isPhase(Phases.FinalPhase)) {
                this.bossEvent.setProgress(this.getHealth() / (this.getMaxHealth() - halfHealth));
            }
        }
        if (destroyBlockDelay > 0) {
            --destroyBlockDelay;
        }
    }

    private void clientAmbientParticles() {
        if (this.isInvisible()) {
            return;
        }
        if (isPhase(Phases.FinalPhase)) {
            float radius = .35f;
            for (int i = 0; i < 5; i++) {
                float rotation = (Mth.sin(tickCount * .05f) * 20 - 20 - 30) * Mth.DEG_TO_RAD / 2f;
                float torsoHeight = 18;
                float z = 1 - torsoHeight * Mth.sin(Mth.PI - rotation);
                float y = torsoHeight * (Mth.cos(Mth.PI - rotation) + 1);
                Vec3 offset = new Vec3(0, y / 16f, z / 16f).yRot((180 - this.yBodyRot) * Mth.DEG_TO_RAD);
                Vec3 random = position().add(new Vec3(
                        (this.random.nextFloat() * 2 - 1) * radius,
                        (this.random.nextFloat() * 2 - 1) * radius + 1.4,
                        (this.random.nextFloat() * 2 - 1) * radius
                )).add(offset);
                level.addParticle(ParticleTypes.SMOKE, random.x, random.y, random.z, 0, -.1, 0);
            }
        }
        if (isOminous()) {
            // ominous indicator particles
            if (random.nextFloat() < 0.25f) {
                float f = tickCount * .3f;
                float wobble = .75f;
                float radius = 6 * this.getScale();
                Vec3 normal = new Vec3(Mth.sin(f) * wobble, 1, Mth.cos(f) * wobble).normalize();
                Vec3 up = new Vec3(1, 0, 0);
                Vec3 pos = this.getBoundingBox().getCenter().add(Utils.getRandomVec3(0.2)).add(0, 0.5, 0);
                Vec3 motion = this.getDeltaMovement().add(0, 0.04, 0).scale(0.25).add(Utils.getRandomVec3(0.01));
                float shrink = -radius / 20;
                float speed = random.nextIntBetweenInclusive(8, 12);
                level.addParticle(new SwirlingParticleOptions(ParticleHelper.TRIAL_OMEN, normal, up, new Vec3(radius, radius, speed), new Vec3(shrink, shrink, 0.1f)),
                        pos.x, pos.y, pos.z,
                        motion.x, motion.y, motion.z
                );
            }
        }
    }


    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        // immune to fall damage
    }

    public boolean isPhase(Phases phase) {
        return phase.value == getPhase();
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource == level.damageSources().lava()) {
            return false;
        }
        if (pSource.is(DamageTypes.IN_WALL) && this.destroyBlockDelay <= 0) {
            Utils.doMobBreakSuffocatingBlocks(this);
            destroyBlockDelay = 40;
        }
        var entity = pSource.getEntity();
        if (entity != null) {
            float distance = entity.distanceTo(this);
            float damageReduction = Mth.clampedLerp(1, 0.5f, (distance - 8) / 16); // reduce damage from 8-24 blocks away, down to 50%
            pAmount *= damageReduction;
        }
        return super.hurt(pSource, pAmount);
    }

    @Override
    protected boolean isImmobile() {
        return isPhase(Phases.Transitioning) || super.isImmobile();
    }

    public boolean isPhaseTransitioning() {
        return isPhase(Phases.Transitioning);
    }

    public void startSeenByPlayer(ServerPlayer pPlayer) {
        super.startSeenByPlayer(pPlayer);
        this.bossEvent.addPlayer(pPlayer);
        PacketDistributor.sendToPlayer(pPlayer, new EntityEventPacket<DeadKingBoss>(this, CLIENT_START_TRACKING));
    }

    public void stopSeenByPlayer(ServerPlayer pPlayer) {
        super.stopSeenByPlayer(pPlayer);
        this.bossEvent.removePlayer(pPlayer);
        PacketDistributor.sendToPlayer(pPlayer, new EntityEventPacket<DeadKingBoss>(this, CLIENT_STOP_TRACKING));
    }

    @Override
    protected void dropFromLootTable(DamageSource damageSource, boolean attackedRecently) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ServerPlayer lastDamagingPlayer = attackedRecently && this.lastHurtByPlayer instanceof ServerPlayer serverPlayer ? serverPlayer : null;
        bossLoot.prepareDrops(this, serverLevel, damageSource, attackedRecently, isOminous(), lastDamagingPlayer);
        bossLoot.spawnPreparedDrops(this);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 10.0)
                .add(AttributeRegistry.SPELL_POWER.get(), 1.15)
                .add(Attributes.ARMOR, 15)
                .add(AttributeRegistry.SPELL_RESIST.get(), 1)
                .add(Attributes.MAX_HEALTH, 500.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.ATTACK_KNOCKBACK, .6)
                .add(ForgeMod.ENTITY_REACH.get(), 4)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.FLYING_SPEED, .325)
                .add(Attributes.MOVEMENT_SPEED, .155);
    }

    @Override
    public void setCustomName(@Nullable Component pName) {
        super.setCustomName(pName);
        this.bossEvent.setName(this.getDisplayName());
    }

    private void setPhase(int phase) {
        this.entityData.set(PHASE, phase);
    }

    private void setPhase(Phases phase) {
        this.setPhase(phase.value);
    }

    public int getPhase() {
        return this.entityData.get(PHASE);
    }

    @Nullable
    public Vec3 getSpawnPos() {
        return spawnPos;
    }

    public void setSpawnPos(@Nullable Vec3 spawnPos) {
        this.spawnPos = spawnPos;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("phase", getPhase());
        pCompound.putInt("playerScale", playerScale);
        bossLoot.save(pCompound);
        if (isOminous()) {
            pCompound.putBoolean("ominous", true);
        }
        if (spawnPos != null) {
            pCompound.put("SpawnPos", NBT.writeVec3Pos(spawnPos));
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
        setPhase(pCompound.getInt("phase"));
        if (isPhase(Phases.FinalPhase)) {
            setFinalPhaseGoals();
        }
        entityData.set(IS_OMINOUS, pCompound.getBoolean("ominous"));
        this.playerScale = pCompound.getInt("playerScale");
        bossLoot.load(pCompound);
        if (pCompound.contains("SpawnPos", Tag.TAG_COMPOUND)) {
            this.spawnPos = NBT.readVec3(pCompound.getCompound("SpawnPos"));
        } else {
            this.spawnPos = null;
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PHASE, 0);
        this.entityData.define(IS_OMINOUS, false);
    }

    private final RawAnimation phase_transition_animation = RawAnimation.begin().thenPlay("dead_king_die");
    private final RawAnimation melee = RawAnimation.begin().thenPlay("dead_king_melee");
    private final RawAnimation slam = RawAnimation.begin().thenPlay("dead_king_slam");

    private final AnimationController<DeadKingBoss> transitionController = new AnimationController<>(this, "dead_king_transition", 0, this::transitionPredicate);
    private final AnimationController<DeadKingBoss> meleeController = new AnimationController<>(this, "dead_king_animations", 0, this::meleePredicate);
    RawAnimation animationToPlay = null;

    @Override
    public void playAnimation(String animationId) {
        try {
            var attackType = AttackType.valueOf(animationId);
            animationToPlay = RawAnimation.begin().thenPlay(attackType.data.animationId);
        } catch (Exception ignored) {
            IronsSpellbooks.LOGGER.error("Entity {} Failed to play animation: {}", this, animationId);
        }
    }

    private PlayState meleePredicate(software.bernie.geckolib.core.animation.AnimationState<DeadKingBoss> animationEvent) {
        var controller = animationEvent.getController();
        if (this.animationToPlay != null) {
            controller.forceAnimationReset();
            controller.setAnimation(animationToPlay);
            animationToPlay = null;
        }
        return transitionController.getAnimationState() == AnimationController.State.STOPPED ? PlayState.CONTINUE : PlayState.STOP;
    }

    private PlayState transitionPredicate(software.bernie.geckolib.core.animation.AnimationState animationEvent) {
        var controller = animationEvent.getController();
        if (isPhaseTransitioning()) {
            controller.setAnimation(phase_transition_animation);
            return PlayState.CONTINUE;
        }
        return PlayState.STOP;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(transitionController);
        controllerRegistrar.add(meleeController);
        super.registerControllers(controllerRegistrar);
    }

    @Override
    public boolean shouldAlwaysAnimateHead() {
        return !isPhaseTransitioning();
    }

    @Override
    public boolean bobBodyWhileWalking() {
        return this.isPhase(Phases.FirstPhase);
    }

    @Override
    public boolean isAnimating() {
        return transitionController.getAnimationState() != AnimationController.State.STOPPED || meleeController.getAnimationState() != AnimationController.State.STOPPED || super.isAnimating();
    }

    @Override
    public boolean doHurtTarget(Entity pEntity) {
        level.playSound(null, getX(), getY(), getZ(), SoundRegistry.DEAD_KING_HIT.get(), SoundSource.HOSTILE, 1, 1);
        return super.doHurtTarget(pEntity);
    }

    @Override
    public boolean shouldAlwaysAnimateLegs() {
        return this.isPhase(Phases.FirstPhase);
    }

    private class DeadKingBarrageGoal extends SpellBarrageGoal {
        public DeadKingBarrageGoal(IMagicEntity abstractSpellCastingMob, AbstractSpell spell, int minLevel, int maxLevel, int pAttackIntervalMin, int pAttackIntervalMax, int projectileCount) {
            super(abstractSpellCastingMob, spell, minLevel, maxLevel, pAttackIntervalMin, pAttackIntervalMax, projectileCount);
        }

        @Override
        public boolean canUse() {
            return !isMeleeing && super.canUse();
        }
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
                return !isCasting();
            }
        };
    }

    protected MoveControl createMoveControl() {
        return new MoveControl(this) {
            //This fixes a bug where a mob tries to path into the block it's already standing, and spins around trying to look "forward"
            //We nullify our rotation calculation if we are close to block we are trying to get to
            @Override
            protected float rotlerp(float pSourceAngle, float pTargetAngle, float pMaximumChange) {
                double d0 = this.wantedX - this.mob.getX();
                double d1 = this.wantedZ - this.mob.getZ();
                if (d0 * d0 + d1 * d1 < .5f) {
                    return pSourceAngle;
                } else {
                    return super.rotlerp(pSourceAngle, pTargetAngle, pMaximumChange * .25f);
                }
            }
        };
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (!level.isClientSide) {
            createBossEvent();
        }
    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        return new NotIdioticNavigation(this, pLevel);
    }

    protected void createBossEvent() {
        this.bossEvent = (ExtendedServerBossEvent) (new ExtendedServerBossEvent(this.getUUID(), this.getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(true).setCreateWorldFog(true);
    }

    @Override
    protected void tickDeath() {
        this.deathTime++;
        if (this.deathTime >= 20 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(Entity.RemovalReason.KILLED);
            Vec3 spawnPos = getSpawnPos();
            if (spawnPos != null) {
                var soul = new DeadKingSoulEntity(level, Vec3.ZERO, spawnPos);
                soul.setRespawnPos(spawnPos);
                soul.moveTo(this.getBoundingBox().getCenter());
                level.addFreshEntity(soul);
            }
        }
    }
}
