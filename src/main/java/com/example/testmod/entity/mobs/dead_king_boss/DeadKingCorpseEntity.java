package com.example.testmod.entity.mobs.dead_king_boss;

import com.example.testmod.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class DeadKingCorpseEntity extends AbstractSpellCastingMob {
    private final static EntityDataAccessor<Boolean> TRIGGERED = SynchedEntityData.defineId(DeadKingCorpseEntity.class, EntityDataSerializers.BOOLEAN);
    private int animTime;

    public DeadKingCorpseEntity(EntityType<? extends Monster> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
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
    public void tick() {
        super.tick();
        if (triggered()) {

            if (!level.isClientSide) {
                if (++animTime > 15 * 20) {
                    DeadKingBoss boss = new DeadKingBoss(level);
                    boss.moveTo(this.position().add(0, 1, 0));
                    level.addFreshEntity(boss);

                    discard();
                }
            } else {
                resurrectParticles();
            }
        }
    }

    private void resurrectParticles() {

    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (pSource == DamageSource.OUT_OF_WORLD) {
            kill();
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
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.mobInteract(pPlayer, pHand);
    }

    private void trigger() {
        this.entityData.set(TRIGGERED, true);
    }

    private boolean triggered() {
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

    private final AnimationBuilder idle = new AnimationBuilder().addAnimation("dead_king_rest", ILoopType.EDefaultLoopTypes.LOOP);
    private final AnimationBuilder rise = new AnimationBuilder().addAnimation("dead_king_rise", ILoopType.EDefaultLoopTypes.PLAY_ONCE);

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "idle", 0, this::idlePredicate));
    }

    private PlayState idlePredicate(AnimationEvent event) {
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
