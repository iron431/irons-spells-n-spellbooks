package io.redspace.ironsspellbooks.entity.dragon;

import io.redspace.ironsspellbooks.api.network.IClientEventEntity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.dragon.control.DragonBodyRotationControl;
import io.redspace.ironsspellbooks.entity.dragon.control.DragonMoveControl;
import io.redspace.ironsspellbooks.entity.dragon.control.DragonNavigation;
import io.redspace.ironsspellbooks.entity.mobs.goals.GenericFollowOwnerGoal;
import io.redspace.ironsspellbooks.network.mob.DragonSyncWalkStatePacket;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockCollisions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DragonEntity extends PathfinderMob implements IClientEventEntity {

    DragonPartEntity[] subEntities;
    DragonPartEntity leftLeg;
    DragonPartEntity rightLeg;
    DragonPartEntity stomach;
    DragonPartEntity chest;
    Vec3 leftHipOffset, rightHipOffset; //in pixels
//    public final WalkAnimationState dragonWalkAnimationState = new WalkAnimationState();

    @Override
    public void move(MoverType pType, Vec3 pPos) {
        super.move(pType, pPos);
    }

    public DragonEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        leftHipOffset = new Vec3(12, 0, -16);
        rightHipOffset = new Vec3(-12, 0, -16);
        this.leftLeg = new DragonPartEntity(this, leftHipOffset, .5f, 2f, ((offsets) -> new Vec3(0, offsets.leftLegY() * 0.0625f, 0)));
        this.rightLeg = new DragonPartEntity(this, rightHipOffset, .5f, 2f, ((offsets) -> new Vec3(0, offsets.rightLegY() * 0.0625f, 0)));
        this.stomach = new DragonPartEntity(this, new Vec3(0, 20, -10), 1.3f, 1.25f);
        this.chest = new DragonPartEntity(this, new Vec3(0, 20, 10), 1.3f, 1.25f);
        this.subEntities = new DragonPartEntity[]{
                stomach,
                chest,
                leftLeg,
                rightLeg
        };
        this.noCulling = true;
        this.setId(ENTITY_COUNTER.getAndAdd(this.subEntities.length + 1) + 1); // Copy of forge fix to sub entity id's
        this.walkAnimation = new WalkAnimationState() {
            @Override
            public void setSpeed(float pSpeed) {
                //ignore speed adjustment caused by damage impulse
                if (pSpeed != 1.5f) {
                    super.setSpeed(pSpeed);
                }
            }
        };

        this.moveControl = new DragonMoveControl(this);
//        this.lookControl = new DragonLookControl(this);
    }

    /**
     * @return code based offsets in pixels. Used for client and server to keep model visuals and hitboxes in sync
     */
    public BodyVisualOffsets calculatePartOffest() {
        float entityScale = this.getScale() / 16f;
        Vec3 rightFootWorldPos = this.position().add(this.rotateWithBody(this.rightHipOffset.scale(entityScale)));
        float rightFootTarget = (float) (Utils.moveToRelativeGroundLevel(this.level, rightFootWorldPos, 2).y - this.getY());
        rightFootTarget = Mth.clamp(rightFootTarget, -1, 1) / entityScale;

        Vec3 leftFootWorldPos = this.position().add(this.rotateWithBody(this.leftHipOffset.scale(entityScale)));
        float leftFootTarget = (float) (Utils.moveToRelativeGroundLevel(this.level, leftFootWorldPos, 2).y - this.getY());
        leftFootTarget = Mth.clamp(leftFootTarget, -1, 1) / entityScale;
        float bodyOffset = (leftFootTarget + rightFootTarget) * .5f;
        return new BodyVisualOffsets(rightFootTarget, leftFootTarget, bodyOffset);
    }

    public float groundOffset(Vec3 worldPosition, float radius) {
        float down = 5 * this.getScale();
        worldPosition = worldPosition.add(0, -down * .5f, 0);
        AABB collider = AABB.ofSize(worldPosition, radius, radius + down, radius);
        BlockCollisions<BlockPos> collisions = new BlockCollisions<>(level, this, collider, false, (p_286213_, p_286214_) -> p_286213_);
        BlockPos blockpos = null;
        double d0 = Double.MAX_VALUE;
        while (collisions.hasNext()) {
            BlockPos blockpos1 = collisions.next();
            double d1 = blockpos1.distToCenterSqr(worldPosition);
            if (d1 < d0 || d1 == d0 && (blockpos == null || blockpos.compareTo(blockpos1) < 0)) {
                blockpos = blockpos1.immutable();
                d0 = d1;
            }
        }
        return (blockpos == null) ? 0 : (float) (blockpos.getY() - this.getY());
    }

    @Override
    public void travel(Vec3 pTravelVector) {
        //Force dragon to move forward relative to his body rotation, not his entity rotation
        float bodyOffset = Mth.degreesDifference(yBodyRot, getYRot());
        super.travel(pTravelVector.yRot(bodyOffset * Mth.DEG_TO_RAD));
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
    protected BodyRotationControl createBodyControl() {
        return new DragonBodyRotationControl(this);
    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        return new DragonNavigation(this, pLevel);
    }

    public static AttributeSupplier.Builder prepareAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.ATTACK_KNOCKBACK, 0.0)
                .add(Attributes.MAX_HEALTH, 60.0)
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 10)
                .add(Attributes.SCALE, 1.6)
                .add(Attributes.STEP_HEIGHT, 1.2)
                .add(Attributes.MOVEMENT_SPEED, .25);
    }

    public float getScale() {
        return (float) this.getAttributeValue(Attributes.SCALE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new GenericFollowOwnerGoal(this, () -> level.getNearestPlayer(TargetingConditions.forNonCombat().ignoreLineOfSight().range(16), this), 1, 6, 5, false, 999));
    }


    //    @Override
//    protected void updateWalkAnimation(float pPartialTick) {
//        float f = Math.min(pPartialTick * 4.0F, 1.0F);
//        this.dragonWalkAnimationState.update(f, 0.4F);
//    }

    //    @Override
//    public boolean isPushable() {
//        return false;
//    }

    @Override
    public void setId(int id) {
        //Allows sub entities to transfer melee damage
        super.setId(id);
        for (int i = 0; i < this.subEntities.length; i++) {
            this.subEntities[i].setId(id + i + 1);
        }
    }

    @Override
    public boolean isMultipartEntity() {
        return true;
    }

    @Override
    public @Nullable PartEntity<?>[] getParts() {
        return subEntities;
    }


    @Override
    public void refreshDimensions() {
        super.refreshDimensions();
        for (DragonPartEntity part : this.subEntities) {
            part.refreshDimensions();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (tickCount % 80 == 0) {
            if (!level.isClientSide) {
                //need to keep client in sync of walk state because walk state (will probably) drive hitbox motion
                PacketDistributor.sendToPlayersTrackingEntity(this, new DragonSyncWalkStatePacket(this));
            }
        }
        var path = this.getNavigation().getPath();
        if (path != null) {
            var data = path.nodes;
            for (Node node : data) {
                Vec3 vec = node.asVec3();
                MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, vec.x, vec.y + 0.5, vec.z, 1, 0, 0, 0, 0, true);
            }
        }
        /**
         Animation Notes:
         #start will override current animation state and start the animation from the beginning
         #startIfStopped only starts if the animation state is stopped
         animation states do not automatically stop when the animation is over
         animation states will hold their last pose until #stop is explicitly called
         */
        if (level.isClientSide) {
            //testAnimationState.startIfStopped(this.tickCount);
//            if (!this.onGround()) {
//                testAnimationState.start(this.tickCount);
//            }

            //if (testAnimationState.isStarted() && testAnimationState.getAccumulatedTime() > TestDragonAnimation.test_animation.lengthInSeconds() * 1000f) {
            //    testAnimationState.stop();
            //}
        }

        float scale = 1f;
        Vec3 pos = this.position();
        var offsets = calculatePartOffest();
        for (int i = 0; i < subEntities.length; i++) {
            var subEntity = subEntities[i];

            double distance = 1 + (i * scale * subEntity.getDimensions(null).width() / 2);
            subEntity.positionSelf(offsets);
        }
    }

    @Override
    public boolean hurt(DamageSource pSource, float pAmount) {
        return !this.level.isClientSide && this.hurt(subEntities[0], pSource, pAmount);
    }

    public boolean hurt(DragonPartEntity bodypart, DamageSource source, float amount) {
        //todo: can do cool damage manipulations based on bodypart (ie headshots)
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return List.of();
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot pSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {

    }

//    @Override
//    public boolean hurt(DamageSource pSource, float pAmount) {
//        IronsSpellbooks.LOGGER.debug("{}", this.tickCount);
//        if(level.isClientSide){
//            testAnimationState.start(this.tickCount);
//        }
//        return super.hurt(pSource, pAmount);
//    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public final AnimationState testAnimationState = new AnimationState();

    @Override
    public void handleClientEvent(byte eventId) {

    }

    public record BodyVisualOffsets(float rightLegY, float leftLegY, float torsoY) {
    }
}
