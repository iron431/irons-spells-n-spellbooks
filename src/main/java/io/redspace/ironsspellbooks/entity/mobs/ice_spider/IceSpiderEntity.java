package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.mobs.IAnimatedAttacker;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.goals.MomentHurtByTargetGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.melee.AttackAnimationData;
import io.redspace.ironsspellbooks.entity.mobs.goals.melee.AttackKeyframe;
import io.redspace.ironsspellbooks.entity.spells.ice_tomb.IceTombEntity;
import io.redspace.ironsspellbooks.entity.spells.root.PreventDismount;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import org.joml.Vector3f;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.*;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class IceSpiderEntity extends AbstractSpellCastingMob implements Enemy, IAnimatedAttacker, PreventDismount {

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        float y = getYRot();
        this.yRotO = y;
        this.yBodyRot = y;
        this.yBodyRotO = y;
        this.yHeadRot = y;
        this.yHeadRotO = y;
    }

    private static final EntityDataAccessor<Boolean> DATA_IS_CLIMBING = SynchedEntityData.defineId(IceSpiderEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CROUCHING = SynchedEntityData.defineId(IceSpiderEntity.class, EntityDataSerializers.BOOLEAN);
    protected static final EntityDataAccessor<Optional<UUID>> DATA_GRAPPLE_UUID = SynchedEntityData.defineId(
            IceSpiderEntity.class, EntityDataSerializers.OPTIONAL_UUID
    );

    private static final AttributeModifier CROUCH_SPEED_MODIFIER = new AttributeModifier(IronsSpellbooks.id("crouching"), -0.30, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    public static final Vec3 TORSO_OFFSET = new Vec3(0, 18, 0);
    private static final int EMERGE_TIME = 45;
    public final Vec3[] cornerPins = {Vec3.ZERO, Vec3.ZERO, Vec3.ZERO, Vec3.ZERO};

    public Vec3 normal = Vec3.ZERO, lastNormal = Vec3.ZERO;
    private int emergeTick;
    int crouchTick;
    public boolean wantsToLeapBack;
    public boolean wantsToCastSpells;
    IceSpiderPartEntity[] subEntities;
    IceSpiderAttackGoal attackGoal;
    @Nullable
    int grappleTime;
    @Nullable
    Entity cachedGrappleTarget = null;

    public IceSpiderEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.noCulling = true;
        subEntities = new IceSpiderPartEntity[]{
                //head
                new IceSpiderPartEntity(this, TORSO_OFFSET.add(0, 0, 16), 1.2f, .8f),
                //torso
                new IceSpiderPartEntity(this, TORSO_OFFSET, 0.75f, 0.75f),
                //abdomen
                new IceSpiderPartEntity(this, TORSO_OFFSET.add(0, 0, -20), 1.75f, 1.5f)
        };
        this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1); // Copy of forge fix to sub entity id's
        this.moveControl = createMoveControl();
    }

    public IceSpiderEntity(Level level) {
        this(EntityRegistry.ICE_SPIDER.get(), level);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK, 1.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MAX_HEALTH, 50)
                .add(Attributes.ARMOR, 20)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.6)
                .add(Attributes.FOLLOW_RANGE, 32)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 4)
                .add(Attributes.STEP_HEIGHT, 1.5)
                .add(Attributes.MOVEMENT_SPEED, .35);
    }

    @Override
    public void tick() {
        super.tick();
        float scalar = getScale() * 4;
        Vec3 worldpos = this.position();
        // 1 -- 3
        // |    |  <- index map relative to forward
        // 0 -- 2
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                Vec3 vec = rotateWithBody(new Vec3((x - 0.5) * scalar, 0, (y - 0.5) * scalar));
                int maxStep = 2;
                int climbOffset = isClimbing() ? 4 * Mth.sign(y - 0.5) : 0;
                cornerPins[x * 2 + y] = Utils.moveToRelativeGroundLevel(level, worldpos.add(vec), maxStep + climbOffset, maxStep - climbOffset).subtract(worldpos);
            }
        }
        Vec3[] vx = cornerPins;
        Vec3 n0 = vx[1].subtract(vx[0]).cross(vx[2].subtract(vx[0]));
        Vec3 n1 = vx[3].subtract(vx[1]).cross(vx[0].subtract(vx[1]));
        Vec3 n2 = vx[0].subtract(vx[2]).cross(vx[3].subtract(vx[2]));
        Vec3 n3 = vx[2].subtract(vx[3]).cross(vx[1].subtract(vx[3]));
        Vec3 targetNormal = n0.add(n1).add(n2).add(n3).normalize();
        this.lastNormal = normal;
        this.normal = Utils.lerp(.2f, normal, targetNormal);
        var quat = Utils.rotationBetweenVectors(new Vector3f(0, 1, 0), Utils.v3f(normal));
        for (IceSpiderPartEntity part : subEntities) {
            part.positionSelf(quat);
        }
        if (emergeTick > 0) {
            emergeTick--;
            if (!level.isClientSide) {
                if (emergeTick == 0) {
                    this.setPose(Pose.STANDING);
                }
            } else {
                updateWalkAnimation(emergeTick / (float) EMERGE_TIME);
            }
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        tickGrapple();
        handleCrouchStatus();
        handleClimbingStatus();
    }

    private void handleCrouchStatus() {
        if (level.isClientSide) {
            return;
        }
        if (isCrouching()) {
            var projection = this.getDefaultDimensions(Pose.STANDING).makeBoundingBox(this.position());
            if (level.noCollision(this, projection.deflate(1.0E-7))) {
                stopCrouching();
            }
        } else {
            if (horizontalCollision) {
                var projection = this.getDefaultDimensions(Pose.CROUCHING).makeBoundingBox(this.position().add(getForward().scale(0.15)));
                if (level.noCollision(this, projection.deflate(1.0E-7))
                    /*&& !level.noCollision(this, this.getBoundingBox().deflate(1.0E-7))*/) {
                    startCrouching();
                }
            }
        }
    }

    private void handleClimbingStatus() {
        if (level.isClientSide || isCrouching()) {
            return;
        }
        if (verticalCollision && !verticalCollisionBelow) {
            // try to unstuck ourselves
            var leftprojection = this.getBoundingBox().deflate(0.2).move(getForward().scale(0.5).yRot(-Mth.HALF_PI));
            boolean strafeLeft = level.noCollision(this, leftprojection);
            this.getMoveControl().strafe(0, strafeLeft ? 1 : -1);
            return;
        }
        if (isClimbing()) {
            if (!horizontalCollision) {
                setIsClimbing(false);
            }
        } else {
            if (horizontalCollision) {
                float deflate = 0.75f;
                var projection = this.getBoundingBox().deflate(deflate).move(getForward().scale(0.25 + deflate / 2));
                if (!level.noCollision(this, projection)) {
                    setIsClimbing(true);
                }
            }
        }
    }

    public void setEmergeFromGround() {
        if (!level.isClientSide) {
            this.setPose(Pose.EMERGING);
            emergeTick = EMERGE_TIME;
        }
    }

    public void setIsClimbing(boolean climbing) {
        this.entityData.set(DATA_IS_CLIMBING, climbing);
    }

    public boolean isClimbing() {
        return entityData.get(DATA_IS_CLIMBING);
    }

    public void setIsCrouching(boolean climbing) {
        this.entityData.set(DATA_IS_CROUCHING, climbing);
    }

    public boolean isCrouching() {
        return entityData.get(DATA_IS_CROUCHING);
    }

    @Override
    public Vec3 getDeltaMovement() {
        return this.isClimbing() ? super.getDeltaMovement().multiply(1, 0, 1).add(0, .275f, 0) : super.getDeltaMovement();
    }

    public float getCrouchHeightMultiplier(float partialTick) {
        return Mth.lerp(crouchTweenPercent(partialTick), 0.5f, 1f);
    }

    public void startCrouching() {
        this.setPose(Pose.CROUCHING);
        this.getAttribute(Attributes.MOVEMENT_SPEED).addOrUpdateTransientModifier(CROUCH_SPEED_MODIFIER);
        setIsCrouching(true);
    }

    public void stopCrouching() {
        this.setPose(Pose.STANDING);
        this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(CROUCH_SPEED_MODIFIER);
        setIsCrouching(false);
    }

    public float getCrouchHeightMultiplier() {
        return isCrouching() ? 0.5f : 1f;
    }

    @Override
    public void castComplete() {
        super.castComplete();
        wantsToCastSpells = false;
    }

    @Override
    public void initiateCastSpell(AbstractSpell spell, int spellLevel) {
        if (!wantsToCastSpells) {
            return;
        }
        if (spell.getCastType() == CastType.INSTANT) {
            serverTriggerAnimation("attack_fang_basic");
        } else {
            serverTriggerAnimation("long_cast");
        }
        super.initiateCastSpell(spell, spellLevel);
    }


    @Override
    public float maxUpStep() {
        return Math.max(1, super.maxUpStep() * getScale());
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder pBuilder) {
        super.defineSynchedData(pBuilder);
        pBuilder.define(DATA_IS_CLIMBING, false);
        pBuilder.define(DATA_IS_CROUCHING, false);
        pBuilder.define(DATA_GRAPPLE_UUID, Optional.empty());
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
    protected PathNavigation createNavigation(Level level) {
        return new IceSpiderNavigation(this, level);
    }

    @Override
    protected void checkFallDamage(double y, boolean onGround, BlockState state, BlockPos pos) {
        return;
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new LeapBackGoal(this));
        this.goalSelector.addGoal(1, new PounceGrappleGoal(this));
        attackGoal = (IceSpiderAttackGoal) new IceSpiderAttackGoal(this, 1.1, 0, 40)
                .setMoveset(List.of(
                        new AttackAnimationData.Builder("attack_bite").length(22).attacks(new AttackKeyframe(14, new Vec3(0, 0, 1))).build(),
                        new AttackAnimationData.Builder("attack_fang_basic").length(20).attacks(new AttackKeyframe(12, new Vec3(0, 0, 1))).build(),
                        new AttackAnimationData.Builder("attack_right_swipe").length(14).attacks(new AttackKeyframe(10, new Vec3(0, 0.1, -1), new Vec3(0, 0, 1))).build()
                ))
                .setMeleeBias(1f, 1f)
                .setSpells(List.of(SpellRegistry.SNOWBALL_SPELL.get(), SpellRegistry.ICE_SPIKES_SPELL.get()), List.of(), List.of(), List.of()).setSpellQuality(.75f, .75f);
        this.goalSelector.addGoal(2, attackGoal
        );
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 32, 0.08f));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new MomentHurtByTargetGoal(this, IceSpiderEntity.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true, livingEntity ->
                livingEntity instanceof Player
                        || livingEntity instanceof IronGolem));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, true, livingEntity ->
                livingEntity instanceof Animal
                        || livingEntity instanceof AbstractVillager
                        || livingEntity instanceof Raider));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundRegistry.ICE_SPIDER_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.ICE_SPIDER_DEATH.get();
    }

    @org.jetbrains.annotations.Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.ICE_SPIDER_AMBIENT.get();
    }

    @Override
    protected LookControl createLookControl() {
        return super.createLookControl();
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this) {
            @Override
            public void rotateHeadTowardsFront() {
                float rot = mob.yBodyRot;
                super.rotateHeadTowardsFront();
                if (rot != mob.yBodyRot) {
                    IceSpiderEntity.this.updateWalkAnimation(1);
                }
            }
        };
    }

    @Override
    public void makeStuckInBlock(BlockState state, Vec3 motionMultiplier) {
        if (!state.is(Blocks.COBWEB)) {
            super.makeStuckInBlock(state, motionMultiplier);
        }
    }

    /**
     * @return continuous value from 0 if not crouching, to 1 if crouching, accounting for tween time when crouch status changes
     */
    public float crouchTweenPercent(float partialTick) {
        float tick = tickCount + partialTick - crouchTick;
        float tweenTime = 10f;
        float f;
        if (tick > tweenTime) {
            f = 1;
        } else {
            f = tick / tweenTime;
        }
        if (isCrouching()) {
            f = 1 - f;
        }
        return f;
    }


    @Override
    protected boolean isImmobile() {
        return super.isImmobile() || getPose().equals(Pose.EMERGING);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState block) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return super.isInvulnerableTo(source) || getPose().equals(Pose.EMERGING);
    }

    public boolean hurt(IceSpiderPartEntity bodypart, DamageSource source, float amount) {
        //todo: can do cool damage manipulations based on bodypart (ie headshots)
        return hurt(source, amount);
    }

    @Override
    protected void actuallyHurt(DamageSource damageSource, float damageAmount) {
        // give ourselves resistance against our own grappled target
        if (damageSource.getEntity() != null && damageSource.getEntity().getUUID().equals(getGrappleTargetUUID())) {
            damageAmount *= .20f;
        }
        if (damageSource.getEntity() instanceof IronGolem) {
            damageAmount *= 0.5f;
        }
        // potentially attempt to leap back if incoming melee damage is severe
        if (isAggressive() && !isCrouching() && !isGrappling() && !wantsToLeapBack && damageSource.isDirect()) {
            float f = Mth.lerp(Math.clamp(damageAmount / 12f, 0, 1), 0.02f, .7f);
            if (random.nextFloat() < f) {
                wantsToCastSpells = true;
                wantsToLeapBack = true;
            }
        }
        super.actuallyHurt(damageSource, damageAmount);
    }

    /**
     * @param vec3 relative vector
     * @return transformation of given vector to align with entity's body rotation
     */
    public Vec3 rotateWithBody(Vec3 vec3) {
        float y = -this.yBodyRot + Mth.HALF_PI;
        return vec3.yRot(y * Mth.DEG_TO_RAD);
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public void setId(int id) {
        super.setId(id);
        for (int i = 0; i < this.subEntities.length; i++) {
            this.subEntities[i].setId(id + i + 1);
        }
    }

    @Override
    public @Nullable PartEntity<?>[] getParts() {
        return subEntities;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public void refreshDimensions() {
        super.refreshDimensions();
        for (IceSpiderPartEntity part : this.subEntities) {
            part.refreshDimensions();
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        super.onSyncedDataUpdated(pKey);
        if (pKey == DATA_IS_CROUCHING) {
            refreshDimensions();
            crouchTick = tickCount;
        } else if (pKey == Entity.DATA_POSE) {
            if (this.getPose() == Pose.EMERGING) {
                playAnimation("emerge_from_ground");
                emergeTick = EMERGE_TIME;
            }
        }
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pose) {
        var dimensions = super.getDefaultDimensions(pose);
        if (pose == Pose.CROUCHING) {
            dimensions = dimensions.scale(1, 0.5f);
        }
        return dimensions;
    }

    @Nullable
    public UUID getGrappleTargetUUID() {
        return this.entityData.get(DATA_GRAPPLE_UUID).orElse(null);
    }

    public boolean isGrappling() {
        return getGrappleTargetUUID() != null;
    }

    public void setGrappleTargetUUID(@Nullable UUID uuid) {
        this.entityData.set(DATA_GRAPPLE_UUID, Optional.ofNullable(uuid));
        if (uuid == null) {
            cachedGrappleTarget = null;
        }
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        if (passenger.getUUID().equals(getGrappleTargetUUID())) {
            Vec3 vec = position().add(rotateWithBody(new Vec3(0, 0, getScale() * 2)));
            callback.accept(passenger, vec.x, vec.y, vec.z);
        } else {
            super.positionRider(passenger, callback);
        }
    }

    @Override
    public boolean shouldRiderFaceForward(Player player) {
        return false;
    }

    @Override
    public boolean canFreeze() {
        return false;
    }

    public void startGrapple(Entity entity) {
        if (getGrappleTargetUUID() == null && !entity.isPassenger()) {
            if (entity.startRiding(this)) {
                grappleTime = 0;
                setGrappleTargetUUID(entity.getUUID());
            }
        }
        wantsToCastSpells = false;
        wantsToLeapBack = false;
    }

    public void tickGrapple() {
        UUID uuid = getGrappleTargetUUID();
        if (uuid == null) {
            return;
        }
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (cachedGrappleTarget == null) {
            var entity = serverLevel.getEntity(uuid);
            if (entity == null) {
                setGrappleTargetUUID(null);
                return;
            } else {
                cachedGrappleTarget = entity;
            }
        }
        if (cachedGrappleTarget.isRemoved()) {
            stopGrappling();
            return;
        }
        if (grappleTime % 20 == 0) {
            this.heal(this.getMaxHealth() * .08f);
        }
        if (grappleTime++ > 40) {
            var entity = cachedGrappleTarget;
            stopGrappling();
            entomb(entity);
        } else {
            cachedGrappleTarget.setTicksFrozen(Math.min(cachedGrappleTarget.getTicksRequiredToFreeze() * 3, cachedGrappleTarget.getTicksFrozen() + 10));
            yHeadRot = yBodyRot;
        }
    }

    public void stopGrappling() {
        if (this.cachedGrappleTarget != null) {
            if (isPassengerOfSameVehicle(cachedGrappleTarget)) {
                cachedGrappleTarget.stopRiding();
            }
        }
        cachedGrappleTarget = null;
        setGrappleTargetUUID(null);
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (passenger.getUUID().equals(getGrappleTargetUUID())) {
            stopGrappling();
        }
    }

    @Override
    public boolean canEntityDismount(Entity entity) {
        return !entity.getUUID().equals(getGrappleTargetUUID());
    }

    public IceTombEntity entomb(Entity entity) {
        IceTombEntity iceTombEntity = new IceTombEntity(level, this);
        iceTombEntity.moveTo(entity.position());
        iceTombEntity.setDeltaMovement(entity.getDeltaMovement().add(this.getForward().add(0, 1, 0).scale(0.5)));
        iceTombEntity.setEvil();
        iceTombEntity.setLifetime(20 * 5);
        level.addFreshEntity(iceTombEntity);
        entity.startRiding(iceTombEntity, true);
        playSound(SoundRegistry.ICE_SPIDER_GRAPPLE_SPIT.get());
        return iceTombEntity;
    }

    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity != null && entity.getUUID().equals(getGrappleTargetUUID())) {
            return null;
        }
        if (entity instanceof Mob) {
            return (Mob) entity;
        } else {
            entity = this.getFirstPassenger();
            if (entity instanceof Player) {
                return (Player) entity;
            }
            return null;
        }
    }

    @Override
    protected void tickRidden(Player player, Vec3 p_275242_) {
        super.tickRidden(player, p_275242_);
        this.yRotO = this.getYRot();
        this.setYRot(player.getYRot());
        this.setXRot(player.getXRot());
        this.setRot(this.getYRot(), this.getXRot());
        this.yBodyRot = this.yRotO;
        this.yHeadRot = this.getYRot();
    }

    @Override
    protected Vec3 getRiddenInput(Player player, Vec3 p_275300_) {
        float f = player.xxa * 0.5F;
        float f1 = player.zza;
        if (f1 <= 0.0F) {
            f1 *= 0.25F;
        }
        if (this.isInWater()) {
            f *= .3f;
            f1 *= .3f;
        }
        return new Vec3(f, 0.0D, f1);
    }

    @Override
    protected float getRiddenSpeed(Player p_278336_) {
        return (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * .8f;
    }

    @Override
    public boolean hasIndirectPassenger(Entity pEntity) {
        // this flag seems to primarily control whether the "press [] to dismount" message occurs
        // make it so that we only get that message if we can dismount
        return pEntity.getUUID().equals(getGrappleTargetUUID());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (getGrappleTargetUUID() != null) {
            pCompound.putInt("grappleTime", grappleTime);
            pCompound.putUUID("grappleTarget", getGrappleTargetUUID());
        }
        pCompound.putBoolean("crouching", isCrouching());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.hasUUID("grappleTarget")) {
            setGrappleTargetUUID(pCompound.getUUID("grappleTarget"));
            grappleTime = pCompound.getInt("grappleTime");
        }
        if (pCompound.getBoolean("crouching")) {
            startCrouching();
        }
    }

    RawAnimation animationToPlay = null;
    private final AnimationController<IceSpiderEntity> meleeController = new AnimationController<>(this, "melee_animations", 0, this::predicate);

    @Override
    public void playAnimation(String animationId) {
        animationToPlay = RawAnimation.begin().thenPlay(animationId);
    }

    private PlayState predicate(AnimationState<IceSpiderEntity> animationEvent) {
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
    }

    @Override
    public boolean isAnimating() {
        return meleeController.getAnimationState() == AnimationController.State.RUNNING;
    }

}
