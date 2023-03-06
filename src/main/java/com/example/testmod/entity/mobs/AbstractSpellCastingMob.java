package com.example.testmod.entity.mobs;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.capabilities.magic.SyncedSpellData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.CastSource;
import com.example.testmod.spells.CastType;
import com.example.testmod.spells.SpellType;
import com.example.testmod.spells.ender.TeleportSpell;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
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

import static com.example.testmod.capabilities.magic.SyncedSpellData.SYNCED_SPELL_DATA;

public abstract class AbstractSpellCastingMob extends Monster implements IAnimatable {
    public static final ResourceLocation modelResource = new ResourceLocation(TestMod.MODID, "geo/abstract_casting_mob.geo.json");
    public static final ResourceLocation textureResource = new ResourceLocation(TestMod.MODID, "textures/entity/abstract_casting_mob/abstract_casting_mob.png");
    public static final ResourceLocation animationInstantCast = new ResourceLocation(TestMod.MODID, "animations/casting_animations.json");
    private static final EntityDataAccessor<SyncedSpellData> DATA_SPELL = SynchedEntityData.defineId(AbstractSpellCastingMob.class, SYNCED_SPELL_DATA);

    private final EnumMap<SpellType, AbstractSpell> spells = new EnumMap<>(SpellType.class);
    private final PlayerMagicData playerMagicData = new PlayerMagicData();

    private @Nullable AbstractSpell castingSpell;

    //Client-side only
    private boolean animationFlag;
    private int animTimestamp = -1;

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
        //TestMod.LOGGER.debug("ASCM.defineSynchedData DATA_SPELL:{}", DATA_SPELL);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> pKey) {
        //TestMod.LOGGER.debug("ASCM.onSyncedDataUpdated ENTER level.isClientSide:{} {}", level.isClientSide, pKey);
        super.onSyncedDataUpdated(pKey);

        if (!level.isClientSide) {
            return;
        }

        if (pKey.getId() == DATA_SPELL.getId()) {
            var isCasting = playerMagicData.isCasting();
            var syncedSpellData = entityData.get(DATA_SPELL);
            //TestMod.LOGGER.debug("ASCM.onSyncedDataUpdated(DATA_SPELL) {} {}", level.isClientSide, syncedSpellData);
            playerMagicData.setSyncedData(syncedSpellData);

            if (!syncedSpellData.isCasting() && isCasting) {
                castComplete();
                return;
            } else/* if (syncedSpellData.getCastingSpellType().getCastType() == CastType.CONTINUOUS)*/ {
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
        //TestMod.LOGGER.debug("ASCM.doSyncSpellData {} {}", level.isClientSide, playerMagicData.getSyncedData());
        //Need a deep clone of the object because set does a basic object ref compare to trigger the update. Do not remove this
        entityData.set(DATA_SPELL, playerMagicData.getSyncedData().deepClone());
    }

    private void castComplete() {
        //TestMod.LOGGER.debug("ASCM.castComplete isClientSide:{}", level.isClientSide);
        if (!level.isClientSide) {
            castingSpell.onServerCastComplete(level, this, playerMagicData);
        }

        playerMagicData.resetCastingState();
        castingSpell = null;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        //TestMod.LOGGER.debug("AbstractSpellCastingMob.aiStep");

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
        //TestMod.LOGGER.debug("AbstractSpellCastingMob.customServerAiStep");


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
                //TestMod.LOGGER.debug("ASCM.customServerAiStep: onCast.1 {}", castingSpell.getSpellType());
                castingSpell.onCast(level, this, playerMagicData);
            }
            castComplete();
        } else if (castingSpell.getCastType() == CastType.CONTINUOUS) {
            if ((playerMagicData.getCastDurationRemaining() + 1) % 10 == 0) {
                forceLookAtTarget(getTarget());
                //TestMod.LOGGER.debug("ASCM.customServerAiStep: onCast.2 {}", castingSpell.getSpellType());
                castingSpell.onCast(level, this, playerMagicData);
            }
        }
    }

    public void initiateCastSpell(SpellType spellType, int spellLevel) {
        if (spellType == SpellType.NONE_SPELL) {
            castingSpell = null;
            return;
        }

        //TestMod.LOGGER.debug("ASCM.initiateCastSpell: {} {} isClientSide:{}", spellType, spellLevel, level.isClientSide);

        castingSpell = spells.computeIfAbsent(spellType, key -> AbstractSpell.getSpell(spellType, spellLevel));
        playerMagicData.initiateCast(castingSpell.getID(), castingSpell.getLevel(), castingSpell.getCastTime(), CastSource.MOB);

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

            int y = target.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, (int) teleportPos.x, (int) teleportPos.z);

            if (Math.abs(teleportPos.y - y) > 3) {
                rotation = target.getLookAngle().normalize().scale(-((float) distance / 2));
                teleportPos = rotation.add(pos);
                y = target.level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, (int) teleportPos.x, (int) teleportPos.z);

                if (Math.abs(teleportPos.y - y) > 3) {
                    rotation = target.getLookAngle().normalize().scale(-1);
                    teleportPos = rotation.add(pos);
                }
            }

            playerMagicData.setAdditionalCastData(new TeleportSpell.TeleportData(teleportPos));
        }
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
     * GeckoLib
     **/

    private final AnimationFactory factory = GeckoLibUtil.createFactory(this);


    @Override
    public AnimationFactory getFactory() {
        return factory;
    }


    private final AnimationBuilder instantCast = new AnimationBuilder().addAnimation("instant_projectile", ILoopType.EDefaultLoopTypes.PLAY_ONCE);//.addAnimation("instant_projectile", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder continuous = new AnimationBuilder().addAnimation("continuous_thrust", ILoopType.EDefaultLoopTypes.HOLD_ON_LAST_FRAME);
    private final AnimationBuilder charged_throw = new AnimationBuilder().addAnimation("charged_throw", ILoopType.EDefaultLoopTypes.PLAY_ONCE);
    private final AnimationBuilder idle = new AnimationBuilder().addAnimation("blank", ILoopType.EDefaultLoopTypes.LOOP);

    private final AnimationController animationController = new AnimationController(this, "casting", 0, this::castingPredicate);

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(animationController);
        data.addAnimationController(new AnimationController(this, "default", 0, this::predicate));
    }

    private PlayState predicate(AnimationEvent event) {
        //if (event.getController().getAnimationState().equals(AnimationState.Stopped))
        event.getController().setAnimation(idle);
        return PlayState.STOP;
    }


    private PlayState castingPredicate(AnimationEvent event) {
        var controller = event.getController();
        if (isCasting() && castingSpell != null && controller.getAnimationState() == AnimationState.Stopped) {
            TestMod.LOGGER.debug("ASCM.castingPredicate castingSpell:{}", castingSpell);

            if (castingSpell.getCastType() == CastType.INSTANT) {
                controller.markNeedsReload();
                controller.setAnimation(instantCast);
            } else if (castingSpell.getCastType() == CastType.CONTINUOUS) {
                controller.markNeedsReload();
                controller.setAnimation(continuous);
            } else if (castingSpell.getCastType() == CastType.CHARGE) {
                controller.markNeedsReload();
                controller.setAnimation(charged_throw);
            }

        }

        if(isCasting()){
            return PlayState.CONTINUE;

        }else{
            return PlayState.STOP;
        }




//        var controller = event.getController();
//
//        if (isCasting()) {
//
//            //controller.markNeedsReload();
//
//            var spell = AbstractSpell.getSpell(entityData.get(DATA_SPELL).getCastingSpellType(), 1);
//            controller.setAnimation(new AnimationBuilder().addAnimation(spell.getCastAnimation(null).getPath()));
//
//            //event.getController().setAnimation(continuous);
//            var anim = controller.getCurrentAnimation();
////            if (anim != null) {
////                TestMod.LOGGER.debug("Anim Duration: {}", anim.animationLength);
////                animTimestamp = tickCount + (int) anim.animationLength;
////            } else {
////                TestMod.LOGGER.debug("Anim is null");
////            }
//            animationFlag = false;
//        }
//        if (controller.getAnimationState() == AnimationState.Stopped) {
//            event.getController().setAnimation(idle);
//        }

    }

    public boolean isAnimating() {

        return isCasting() || !(animationController.getAnimationState() == AnimationState.Stopped);
        //return true;
    }
}
