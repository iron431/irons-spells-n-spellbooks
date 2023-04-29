package io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.CastSource;
import io.redspace.ironsspellbooks.spells.CastType;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.spells.ender.TeleportSpell;
import io.redspace.ironsspellbooks.spells.fire.BurningDashSpell;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib3.core.AnimationState;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import software.bernie.geckolib3.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.EnumMap;

public abstract class AbstractSpellCastingMob extends Monster implements IAnimatable {
    public static final ResourceLocation modelResource = new ResourceLocation(IronsSpellbooks.MODID, "geo/abstract_casting_mob.geo.json");
    public static final ResourceLocation textureResource = new ResourceLocation(IronsSpellbooks.MODID, "textures/entity/abstract_casting_mob/abstract_casting_mob.png");
    public static final ResourceLocation animationInstantCast = new ResourceLocation(IronsSpellbooks.MODID, "animations/casting_animations.json");
    private static final EntityDataAccessor<SyncedSpellData> DATA_SPELL = SynchedEntityData.defineId(AbstractSpellCastingMob.class, SyncedSpellData.SYNCED_SPELL_DATA);
    private static final EntityDataAccessor<Boolean> DATA_CANCEL_CAST = SynchedEntityData.defineId(AbstractSpellCastingMob.class, EntityDataSerializers.BOOLEAN);

    private final EnumMap<SpellType, AbstractSpell> spells = new EnumMap<>(SpellType.class);
    private final PlayerMagicData playerMagicData = new PlayerMagicData(true);

    private @Nullable AbstractSpell castingSpell;

    protected AbstractSpellCastingMob(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        playerMagicData.setSyncedData(new SyncedSpellData(this));
    }

    public PlayerMagicData getPlayerMagicData() {
        return playerMagicData;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SPELL, new SyncedSpellData(-1));
        this.entityData.define(DATA_CANCEL_CAST, false);
        //irons_spellbooks.LOGGER.debug("ASCM.defineSynchedData DATA_SPELL:{}", DATA_SPELL);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        //irons_spellbooks.LOGGER.debug("ASCM.onSyncedDataUpdated ENTER level.isClientSide:{} {}", level.isClientSide, pKey);
        super.onSyncedDataUpdated(pKey);

        if (!level.isClientSide) {
            return;
        }

        if (pKey.getId() == DATA_CANCEL_CAST.getId()) {
            //IronsSpellbooks.LOGGER.debug("onSyncedDataUpdated DATA_CANCEL_CAST");
            cancelCast();
        }

        if (pKey.getId() == DATA_SPELL.getId()) {
            //IronsSpellbooks.LOGGER.debug("onSyncedDataUpdated DATA_SPELL");
            var isCasting = playerMagicData.isCasting();
            var syncedSpellData = entityData.get(DATA_SPELL);
            //irons_spellbooks.LOGGER.debug("ASCM.onSyncedDataUpdated(DATA_SPELL) {} {}", level.isClientSide, syncedSpellData);
            playerMagicData.setSyncedData(syncedSpellData);

            if (!syncedSpellData.isCasting() && isCasting) {
                castComplete();
            } else if (syncedSpellData.isCasting() && !isCasting)/* if (syncedSpellData.getCastingSpellType().getCastType() == CastType.CONTINUOUS)*/ {
                var spellType = SpellType.getTypeFromValue(syncedSpellData.getCastingSpellId());
                initiateCastSpell(spellType, syncedSpellData.getCastingSpellLevel());
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        playerMagicData.getSyncedData().saveNBTData(pCompound);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        var syncedSpellData = new SyncedSpellData(this);
        syncedSpellData.loadNBTData(pCompound);
        playerMagicData.setSyncedData(syncedSpellData);
    }

    public void doSyncSpellData() {
        //Need a deep clone of the object because set does a basic object ref compare to trigger the update. Do not remove the deepClone
        entityData.set(DATA_SPELL, playerMagicData.getSyncedData().deepClone());
    }

    public void cancelCast() {
        if(isCasting()){
            if (level.isClientSide) {
                cancelCastAnimation = true;
            } else {
                //Need to ensure we pass a different value if we want the data to sync
                entityData.set(DATA_CANCEL_CAST, !entityData.get(DATA_CANCEL_CAST));
            }

            castComplete();
        }

    }

    private void castComplete() {
        //irons_spellbooks.LOGGER.debug("ASCM.castComplete isClientSide:{}", level.isClientSide);
        if (!level.isClientSide) {
            castingSpell.onServerCastComplete(level, this, playerMagicData, false);
        }else{
            playerMagicData.resetCastingState();
        }

        castingSpell = null;
    }

    public void startAutoSpinAttack(int pAttackTicks) {
        this.autoSpinAttackTicks = pAttackTicks;
        if (!this.level.isClientSide) {
            this.setLivingEntityFlag(4, true);
        }
        //Lil trick
        this.setYRot((float) (Math.atan2(getDeltaMovement().x, getDeltaMovement().z) * Mth.RAD_TO_DEG));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        //irons_spellbooks.LOGGER.debug("AbstractSpellCastingMob.aiStep");

        //Should basically be only used for client stuff

        if (!level.isClientSide || castingSpell == null) {
            return;
        }

        if (playerMagicData.getCastDurationRemaining() <= 0) {
            if (castingSpell.getCastType() == CastType.INSTANT) {
                castingSpell.onClientPreCast(level, this, InteractionHand.MAIN_HAND, playerMagicData);
                castComplete();
            }
        } else {
            //Actively casting a long cast or continuous cast

        }

    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        //irons_spellbooks.LOGGER.debug("AbstractSpellCastingMob.customServerAiStep");


        if (castingSpell == null || entityData.isDirty()) {
            return;
        }

        playerMagicData.handleCastDuration();

        if (playerMagicData.isCasting()) {
            castingSpell.onServerCastTick(level, this, playerMagicData);
        }

        if (playerMagicData.getCastDurationRemaining() <= 0) {
            if (castingSpell.getCastType() == CastType.LONG || castingSpell.getCastType() == CastType.CHARGE || castingSpell.getCastType() == CastType.INSTANT) {
                forceLookAtTarget(getTarget());
                //irons_spellbooks.LOGGER.debug("ASCM.customServerAiStep: onCast.1 {}", castingSpell.getSpellType());
                castingSpell.onCast(level, this, playerMagicData);
            }
            castComplete();
        } else if (castingSpell.getCastType() == CastType.CONTINUOUS) {
            if ((playerMagicData.getCastDurationRemaining() + 1) % 10 == 0) {
                forceLookAtTarget(getTarget());
                //irons_spellbooks.LOGGER.debug("ASCM.customServerAiStep: onCast.2 {}", castingSpell.getSpellType());
                castingSpell.onCast(level, this, playerMagicData);
            }
        }
    }

    public void initiateCastSpell(SpellType spellType, int spellLevel) {
        if (spellType == SpellType.NONE_SPELL) {
            castingSpell = null;
            return;
        }

        if (level.isClientSide) {
            cancelCastAnimation = false;
        }

        //irons_spellbooks.LOGGER.debug("ASCM.initiateCastSpell: {} {} isClientSide:{}", spellType, spellLevel, level.isClientSide);
        castingSpell = spells.computeIfAbsent(spellType, key -> AbstractSpell.getSpell(spellType, spellLevel));
        playerMagicData.initiateCast(castingSpell.getID(), castingSpell.getLevel(), castingSpell.getEffectiveCastTime(this), CastSource.MOB);

        if (!level.isClientSide) {
            castingSpell.onServerPreCast(level, this, playerMagicData);
        }
    }

    public boolean isCasting() {
        return entityData.get(DATA_SPELL).isCasting();
    }

    public void setTeleportLocationBehindTarget(int distance) {
        var target = getTarget();
        if (target != null) {
            var rotation = target.getLookAngle().normalize().scale(-distance);
            var pos = target.position();
            var teleportPos = rotation.add(pos);

            boolean valid = false;
            for (int i = 0; i < 24; i++) {
                teleportPos = target.position().subtract(new Vec3(0, 0, distance / (float) (i / 7 + 1)).yRot(-(target.getYRot() + i * 45) * Mth.DEG_TO_RAD));
                int y = Utils.findRelativeGroundLevel(target.level, teleportPos, 3);
                teleportPos = new Vec3(teleportPos.x, y, teleportPos.z);
                var bb = this.getBoundingBox();
                var reposBB = bb.move(teleportPos.subtract(target.position()));
                if (!level.collidesWithSuffocatingBlock(this, reposBB)) {
                    valid = true;
                    break;
                }

            }
            if (valid)
                playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(teleportPos));
        }
    }

    public void setBurningDashDirectionData() {
        playerMagicData.setAdditionalCastData(new BurningDashSpell.BurningDashDirectionOverrideCastData());
    }

    private void forceLookAtTarget(LivingEntity target) {
        if (target != null) {
            lookAt(target, 360, 360);
            setOldPosAndRot();
        }
    }

    private void addClientSideParticles() {
        double d0 = .4d;
        double d1 = .3d;
        double d2 = .35d;
        float f = this.yBodyRot * ((float) Math.PI / 180F) + Mth.cos((float) this.tickCount * 0.6662F) * 0.25F;
        float f1 = Mth.cos(f);
        float f2 = Mth.sin(f);
        this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() + (double) f1 * 0.6D, this.getY() + 1.8D, this.getZ() + (double) f2 * 0.6D, d0, d1, d2);
        this.level.addParticle(ParticleTypes.ENTITY_EFFECT, this.getX() - (double) f1 * 0.6D, this.getY() + 1.8D, this.getZ() - (double) f2 * 0.6D, d0, d1, d2);
    }

    /**
     * GeckoLib Animations
     **/

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);
    private SpellType lastCastSpellType = SpellType.NONE_SPELL;
    private boolean cancelCastAnimation = false;
    private final AnimationBuilder idle = new AnimationBuilder().addAnimation("blank", ILoopType.EDefaultLoopTypes.LOOP);
    private final AnimationController animationControllerOtherCast = new AnimationController(this, "other_casting", 0, this::otherCastingPredicate);
    private final AnimationController animationControllerInstantCast = new AnimationController(this, "instant_casting", 0, this::instantCastingPredicate);
    private final AnimationController animationControllerLongCast = new AnimationController(this, "long_casting", 0, this::longCastingPredicate);

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(animationControllerOtherCast);
        data.addAnimationController(animationControllerInstantCast);
        data.addAnimationController(animationControllerLongCast);
        data.addAnimationController(new AnimationController(this, "idle", 0, this::idlePredicate));
    }

    private PlayState idlePredicate(AnimationEvent event) {
        event.getController().setAnimation(idle);
        return PlayState.STOP;
    }

    private PlayState instantCastingPredicate(AnimationEvent event) {
        if (cancelCastAnimation) {
            return PlayState.STOP;
        }

        var controller = event.getController();
        if (isCasting() && castingSpell != null && castingSpell.getCastType() == CastType.INSTANT && controller.getAnimationState() == AnimationState.Stopped) {
            setStartAnimationFromSpell(controller, castingSpell);
        }
        return PlayState.CONTINUE;
    }

    private PlayState longCastingPredicate(AnimationEvent event) {
        if (cancelCastAnimation) {
            return PlayState.STOP;
        }

        var controller = event.getController();
        if (isCasting() && castingSpell != null && castingSpell.getCastType() == CastType.LONG && controller.getAnimationState() == AnimationState.Stopped) {
            setStartAnimationFromSpell(controller, castingSpell);
        }

        if (!isCasting() && lastCastSpellType.getCastType() == CastType.LONG) {
            setFinishAnimationFromSpell(controller, lastCastSpellType);
        }

        return PlayState.CONTINUE;
    }

    private PlayState otherCastingPredicate(AnimationEvent event) {
        if (cancelCastAnimation) {
            return PlayState.STOP;
        }

        var controller = event.getController();
        if (isCasting() && castingSpell != null && controller.getAnimationState() == AnimationState.Stopped) {
            if (castingSpell.getCastType() == CastType.CONTINUOUS || castingSpell.getCastType() == CastType.CHARGE) {
                setStartAnimationFromSpell(controller, castingSpell);
            }
            return PlayState.CONTINUE;
        }

        if (isCasting()) {
            return PlayState.CONTINUE;
        } else {
            return PlayState.STOP;
        }
    }

    private void setStartAnimationFromSpell(AnimationController controller, AbstractSpell spell) {
        spell.getCastStartAnimation(null).left().ifPresent(animationBuilder -> {
            controller.markNeedsReload();
            controller.setAnimation(animationBuilder);
            lastCastSpellType = spell.getSpellType();
            cancelCastAnimation = false;
        });
    }

    private void setFinishAnimationFromSpell(AnimationController controller, SpellType spellType) {
        var spell = AbstractSpell.getSpell(spellType, 1);
        spell.getCastFinishAnimation(null).left().ifPresent(animationBuilder -> {
            controller.markNeedsReload();
            controller.setAnimation(animationBuilder);
            lastCastSpellType = SpellType.NONE_SPELL;
            cancelCastAnimation = false;
        });
    }

    public boolean isAnimating() {
        return isCasting()
                || (animationControllerOtherCast.getAnimationState() != AnimationState.Stopped)
                || (animationControllerInstantCast.getAnimationState() != AnimationState.Stopped);
    }

    public boolean shouldBeExtraAnimated() {
        return true;
    }

    public boolean shouldAlwaysAnimateHead() {
        return true;
    }

    public boolean shouldAlwaysAnimateLegs() {
        return true;
    }

    public boolean shouldPointArmsWhileCasting() {
        return true;
    }
}
