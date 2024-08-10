package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss;

import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.MinecraftInstanceHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;

public class DeadKingCorpseEntity extends AbstractSpellCastingMob {
    DeadKingAmbienceSoundManager ambienceSoundManager;

    private final static EntityDataAccessor<Boolean> TRIGGERED = SynchedEntityData.defineId(DeadKingCorpseEntity.class, EntityDataSerializers.BOOLEAN);
    private int currentAnimTime;
    private final int animLength = 20 * 15;

    public DeadKingCorpseEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setPersistenceRequired();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (triggered()) {
            ++currentAnimTime;
            if (!level().isClientSide) {
                if (currentAnimTime > animLength) {
                    DeadKingBoss boss = new DeadKingBoss(level());
                    boss.moveTo(this.position().add(0, 1, 0));
                    boss.finalizeSpawn((ServerLevel) level(), level().getCurrentDifficultyAt(boss.getOnPos()), MobSpawnType.TRIGGERED, null, null);
                    int playerCount = Math.max(level().getEntitiesOfClass(Player.class, boss.getBoundingBox().inflate(32)).size(), 1);
                    boss.getAttributes().getInstance(Attributes.MAX_HEALTH).addPermanentModifier(new AttributeModifier("Gank Health Bonus", (playerCount - 1) * .5, AttributeModifier.Operation.MULTIPLY_BASE));
                    boss.setHealth(boss.getMaxHealth());
                    boss.getAttributes().getInstance(Attributes.ATTACK_DAMAGE).addPermanentModifier(new AttributeModifier("Gank Damage Bonus", (playerCount - 1) * .25, AttributeModifier.Operation.MULTIPLY_BASE));
                    boss.getAttributes().getInstance(AttributeRegistry.SPELL_RESIST.get()).addPermanentModifier(new AttributeModifier("Gank Spell Resist Bonus", (playerCount - 1) * .1, AttributeModifier.Operation.MULTIPLY_BASE));
                    boss.setPersistenceRequired();
                    level().addFreshEntity(boss);
                    MagicManager.spawnParticles(level(), ParticleTypes.SCULK_SOUL, position().x, position().y + 2.5, position().z, 80, .2, .2, .2, .25, true);
                    level().playSound(null, getX(), getY(), getZ(), SoundRegistry.DEAD_KING_SPAWN.get(), SoundSource.MASTER, 20, 1);
                    discard();
                }
            } else {
                resurrectParticles();
            }
        } else if (level.isClientSide) {
            //Ambience sound handling
            if (tickCount % 40 == 0) {
                MinecraftInstanceHelper.ifPlayerPresent(player -> {
                    //Local player who we want to play music to
                    float yRot = this.getYRot();
                    Vec3 musicCenter = this.position().add(-15 * Mth.sin(yRot * Mth.DEG_TO_RAD), 0, 15 * Mth.cos(yRot * Mth.DEG_TO_RAD));
                    if (musicCenter.distanceToSqr(player.position()) < DeadKingAmbienceSoundInstance.SOUND_RANGE_SQR) {
                        if (ambienceSoundManager == null) {
                            ambienceSoundManager = new DeadKingAmbienceSoundManager(musicCenter);
                        }
                        ambienceSoundManager.trigger();
                    }
                });
            }
        }
    }

    private void resurrectParticles() {
        float f = (currentAnimTime / (float) animLength);
        float rot = currentAnimTime * 12 + (1 + f * 15);
        float height = f * 4 + (.4f * Mth.sin(currentAnimTime * 30 * Mth.DEG_TO_RAD) * f * f);
        float distance = Mth.clamp(Utils.smoothstep(0, 1.15f, f * 3), 0, 1.15f);
        Vec3 pos = new Vec3(0, 0, distance).yRot(rot * Mth.DEG_TO_RAD).add(0, height, 0).add(position());

        level.addParticle(ParticleTypes.SCULK_SOUL, pos.x, pos.y, pos.z, 0, 0, 0);
        float radius = 4;
        if (random.nextFloat() < f * 1.5f) {
            Vec3 random = position().add(new Vec3(
                    (this.random.nextFloat() * 2 - 1) * radius,
                    3.5 + (this.random.nextFloat() * 2 - 1) * radius,
                    (this.random.nextFloat() * 2 - 1) * radius
            ));
            Vec3 motion = position().subtract(random).scale(.04f);
            level.addParticle(ParticleTypes.SCULK_SOUL, random.x, random.y, random.z, motion.x, motion.y, motion.z);
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            discard();
            return true;
        } else {
            Player player = level.getNearestPlayer(this, 8);
            if (player != null) {
                trigger();
            }
            return false;
        }
    }

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (!triggered()) {
            trigger();
            return InteractionResult.sidedSuccess(level().isClientSide);
        }
        return super.mobInteract(pPlayer, pHand);
    }

    private void trigger() {
        if (!triggered()) {
            level.playSound(null, getX(), getY(), getZ(), SoundRegistry.DEAD_KING_RESURRECT.get(), SoundSource.AMBIENT, 2, 1);
            this.entityData.set(TRIGGERED, true);
            if(this.ambienceSoundManager != null){
                ambienceSoundManager.triggerStop();
            }
        }
    }

    public boolean triggered() {
        return this.entityData.get(TRIGGERED);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TRIGGERED, false);
    }

    /**
     * GeckoLib
     **/

    private final RawAnimation idle = RawAnimation.begin().thenLoop("dead_king_rest");
    private final RawAnimation rise = RawAnimation.begin().thenPlay("dead_king_rise");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController(this, "idle", 0, this::idlePredicate));
    }

    private PlayState idlePredicate(AnimationState event) {
        if (triggered()) {
            event.getController().setAnimation(rise);
        } else {
            event.getController().setAnimation(idle);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public boolean shouldBeExtraAnimated() {
        return false;
    }

    @Override
    public boolean shouldAlwaysAnimateHead() {
        return false;
    }
}
