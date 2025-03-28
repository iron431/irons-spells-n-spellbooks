package io.redspace.ironsspellbooks.entity.mobs.ice_spider;

import io.redspace.ironsspellbooks.entity.mobs.abstract_spell_casting_mob.AbstractSpellCastingMob;
import io.redspace.ironsspellbooks.entity.mobs.goals.GenericFollowOwnerGoal;
import io.redspace.ironsspellbooks.entity.mobs.wizards.fire_boss.NotIdioticNavigation;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;

import javax.annotation.Nullable;

public class IceSpiderEntity extends AbstractSpellCastingMob {
    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_KNOCKBACK, 1.0)
                .add(Attributes.ATTACK_DAMAGE, 8.0)
                .add(Attributes.MAX_HEALTH, 50)
                .add(Attributes.ARMOR, 20)
                .add(Attributes.KNOCKBACK_RESISTANCE, 8.0)
                .add(Attributes.FOLLOW_RANGE, 24)
                .add(Attributes.ENTITY_INTERACTION_RANGE, 4)
                .add(Attributes.STEP_HEIGHT, 1.5)
                .add(Attributes.MOVEMENT_SPEED, .4);

    }

    public static final Vec3 TORSO_OFFSET = new Vec3(0, 16, 0);
    IceSpiderPartEntity[] subEntities;

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
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new NotIdioticNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new LookAtPlayerGoal(this, Player.class, 32, 0.08f));
        this.goalSelector.addGoal(0, new GenericFollowOwnerGoal(this,
                () -> level.getNearestPlayer(TargetingConditions.forNonCombat().ignoreLineOfSight().range(40), this),
                1, 6, 4, false, 999));
    }

    @Override
    protected LookControl createLookControl() {
        return super.createLookControl();
    }

    @Override
    protected BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this){
            @Override
            public void rotateHeadTowardsFront() {
                float rot = mob.yBodyRot;
                super.rotateHeadTowardsFront();
                if(rot != mob.yBodyRot){
                    IceSpiderEntity.this.updateWalkAnimation(1);
                }
            }
        };
    }

    @Override
    public void tick() {
        super.tick();
        for (IceSpiderPartEntity part : subEntities) {
            part.positionSelf();
        }
    }

    public boolean hurt(IceSpiderPartEntity bodypart, DamageSource source, float amount) {
        //todo: can do cool damage manipulations based on bodypart (ie headshots)
        return super.hurt(source, amount);
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
        //Allows sub entities to transfer melee damage
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
}
