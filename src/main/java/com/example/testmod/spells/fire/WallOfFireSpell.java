package com.example.testmod.spells.fire;

import com.example.testmod.TestMod;
import com.example.testmod.capabilities.magic.CastData;
import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.wall_of_fire.WallOfFireCastTracker;
import com.example.testmod.entity.wall_of_fire.WallOfFireEntity;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;


public class WallOfFireSpell extends AbstractSpell {
    public WallOfFireSpell() {
        this(1);
    }

    public WallOfFireSpell(int level) {
        super(SpellType.WALL_OF_FIRE_SPELL);
        this.level = level;
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 0;
        this.castTime = 100;
        this.baseManaCost = 5;
        this.cooldown = 100;
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        if (playerMagicData.isCasting() && playerMagicData.getCastingSpellId() == this.getID() && playerMagicData.getAdditionalCastData() == null) {
            TestMod.LOGGER.debug("WallOfFireSpell: creating new data");
            playerMagicData.setAdditionalCastData(new FireWallData(getWallLength()));
            WallOfFireCastTracker wallOfFireCastTracker = new WallOfFireCastTracker(world, entity, (FireWallData) playerMagicData.getAdditionalCastData(), 2);
            wallOfFireCastTracker.setPos(entity.position());
            world.addFreshEntity(wallOfFireCastTracker);
            playerMagicData.setCastingEntity(wallOfFireCastTracker);
        }
        TestMod.LOGGER.debug(playerMagicData.toString());
        //if (playerMagicData.getAdditionalCastData() instanceof FireWallData fireWallData)

    }

    @Override
    public void onCastComplete(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
//        for (Vec3 vec : spawnAnchors) {
//            TestMod.LOGGER.debug(vec.toString());
//
//        }
        super.onCastComplete(level, entity, playerMagicData);
        TestMod.LOGGER.debug("WallOfFireSpell.onCastComplete");
        if (playerMagicData.getAdditionalCastData() instanceof FireWallData fireWallData) {
            if (fireWallData.anchors.size() == 1)
                if (playerMagicData.castingEntity instanceof WallOfFireCastTracker wallOfFireCastTracker)
                    wallOfFireCastTracker.addAnchor(fireWallData, level, entity);
            WallOfFireEntity fireWall = new WallOfFireEntity(level, entity, fireWallData.anchors, getDamage(entity));
            fireWall.setPos(fireWallData.getFirstAnchorSafe());
            level.addFreshEntity(fireWall);
        }

    }

    private float getWallLength() {
        return 7 + level * 2;
    }

    private float getDamage(Entity sourceEntity) {
        return getSpellPower(sourceEntity);
    }

//    private Vec3 raycastForAnchor(Level level, LivingEntity entity) {
//        return Utils.getTargetBlock(level, entity, ClipContext.Fluid.ANY, 10 + getWallLength() / 2).getLocation();
//
//    }
//
//    private void addAnchor(Vec3 anchor, FireWallData fireWallData, Level level, LivingEntity entity) {
//        anchor = setOnGround(anchor, level);
//        var anchorPoints = fireWallData.anchors;
//        if (anchorPoints.size() == 0) {
//            anchorPoints.add(anchor);
//
//        } else {
//            int i = anchorPoints.size();
//            float distance = (float) anchorPoints.get(i - 1).distanceTo(anchor);
//            float maxDistance = fireWallData.maxTotalDistance - fireWallData.accumulatedDistance;
//            if (distance <= maxDistance) {
//                //point fits, continue
//                fireWallData.accumulatedDistance += distance;
//                anchorPoints.add(anchor);
//                //TestMod.LOGGER.debug("WallOfFire: this anchor fits (length {})", distance);
//
//            } else {
//                //too long, clip and cancel spell
//                anchor = anchorPoints.get(i - 1).add(anchor.subtract(anchorPoints.get(i - 1)).normalize().scale(maxDistance));
//                anchor = setOnGround(anchor, level);
//                anchorPoints.add(anchor);
//                if (entity instanceof ServerPlayer serverPlayer) {
//                    var playerMagicData = PlayerMagicData.getPlayerMagicData(serverPlayer);
//                    boolean triggerCooldown = playerMagicData.getCastSource() != CastSource.Scroll;
//                    if (!triggerCooldown)
//                        onCastComplete(serverPlayer.level, serverPlayer, playerMagicData);
//                    ServerboundCancelCast.cancelCast(serverPlayer, triggerCooldown);
//                }
//            }
//            //TestMod.LOGGER.debug("WallOfFire.maxDistance: {}", this.maxTotalDistance);
//            //TestMod.LOGGER.debug("WallOfFire.currentDistance: {}", this.accumulatedDistance);
//        }
//        MagicManager.spawnParticles(level, ParticleTypes.FLAME, anchor.x, anchor.y + 1.5, anchor.z, 10, 0, .5, 0, 0, true);
//        TestMod.LOGGER.debug("WallOfFireSpell: adding anchor");
//    }
//
//    private Vec3 setOnGround(Vec3 in, Level level) {
//        if (level.getBlockState(new BlockPos(in.x, in.y + .5f, in.z)).isAir()) {
//            for (int i = 0; i < 15; i++) {
//                if (!level.getBlockState(new BlockPos(in.x, in.y - i, in.z)).isAir()) {
//                    return new Vec3(in.x, in.y - i + 1, in.z);
//                }
//            }
//            return new Vec3(in.x, in.y - 15, in.z);
//        } else {
//            double y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, (int) in.x, (int) in.z);
//            return new Vec3(in.x, y, in.z);
//        }
//    }

    public class FireWallData implements CastData {
        public List<Vec3> anchors = new ArrayList<>();
        public float maxTotalDistance;
        public float accumulatedDistance;

        FireWallData(float maxTotalDistance) {
            this.maxTotalDistance = maxTotalDistance;
        }

        Vec3 getFirstAnchorSafe() {
            if (anchors.size() > 0)
                return anchors.get(0);
            else
                return Vec3.ZERO;
        }
    }
}
