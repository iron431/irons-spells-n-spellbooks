package io.redspace.ironsspellbooks.entity.mobs.raise_dead_summon_entity;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.builder.ILoopType;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;

public class RaiseDeadSummonEntity extends AbstractSpellCastingMob {
    private static final EntityDataAccessor<Boolean> DATA_ZOMBIE = SynchedEntityData.defineId(RaiseDeadSummonEntity.class, EntityDataSerializers.BOOLEAN);
    private Monster monster;

    int animTime;

    public RaiseDeadSummonEntity(EntityType<? extends RaiseDeadSummonEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.monster = null;
        animTime = 100;
    }

    public RaiseDeadSummonEntity(Level pLevel, Monster monster, boolean isZombie) {
        this(EntityRegistry.RAISE_DEAD_SUMMONER.get(), pLevel);
        this.monster = monster;
        equip();
        entityData.set(DATA_ZOMBIE, isZombie);
    }

    private void equip() {
        setItemSlot(EquipmentSlot.FEET, monster.getItemBySlot(EquipmentSlot.FEET));
        setItemSlot(EquipmentSlot.LEGS, monster.getItemBySlot(EquipmentSlot.LEGS));
        setItemSlot(EquipmentSlot.CHEST, monster.getItemBySlot(EquipmentSlot.CHEST));
        setItemSlot(EquipmentSlot.HEAD, monster.getItemBySlot(EquipmentSlot.HEAD));
        setItemSlot(EquipmentSlot.MAINHAND, monster.getItemBySlot(EquipmentSlot.MAINHAND));
        setItemSlot(EquipmentSlot.OFFHAND, monster.getItemBySlot(EquipmentSlot.OFFHAND));
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    private void finishSummoning() {
        if (monster != null) {
            monster.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
            level.addFreshEntity(monster);
        }

        this.discard();
    }

    @Override
    public void tick() {
        if (--animTime < 0)
            finishSummoning();
    }

    private final AnimationBuilder rise_animation = new AnimationBuilder().addAnimation("dead_king_slam", ILoopType.EDefaultLoopTypes.LOOP);

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "rise", 0, this::risePredicate));
    }

    private PlayState risePredicate(AnimationEvent event) {
        event.getController().setAnimation(rise_animation);
        return PlayState.CONTINUE;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DATA_ZOMBIE, false);
    }

    public boolean isZombie() {
        return entityData.get(DATA_ZOMBIE);
    }

    @Override
    public boolean shouldBeExtraAnimated() {
        return false;
    }

}
