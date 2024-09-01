package io.redspace.ironsspellbooks.entity.dragon.control;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.phys.Vec3;

public class DragonLookControl extends LookControl {
    private static final float NECK_MAX_LENGTH = 22 / 16f;
    Vec3 headPosition = new Vec3(0, 2, -58 / 16f);
    Vec3 neckPosition = new Vec3(0, 2, -32 / 16f);
    Vec3 collarPosition = new Vec3(0, 2, -21 / 16f);
    Vec3[] headFabrikNodes = {collarPosition, neckPosition, headPosition};
    float[] nodeLengths = {11 / 16f, 22 / 16f, 0f};

    public DragonLookControl(Mob pMob) {
        super(pMob);
        collarPosition = applyToMobSpace(new Vec3(0, 2, -21 / 16f));
        neckPosition = applyToMobSpace(new Vec3(0, 2, -32 / 16f));
        headPosition = applyToMobSpace(new Vec3(0, 2, -58 / 16f));
    }


    @Override
    public void tick() {
        IronsSpellbooks.LOGGER.debug("DragonLookControl.tick");
        if (this.resetXRotOnTick()) {
            this.mob.setXRot(0.0F);
        }
        Vec3 neckVector = applyToMobSpace(new Vec3(0, 2, -1.5f));
        if (mob.level.isClientSide) {
            mob.level.addParticle(ParticleHelper.SNOWFLAKE, mob.getX() + neckVector.x, mob.getY() + neckVector.y + 1, mob.getZ() + neckVector.z, 0, 0, 0);
        } else {
            MagicManager.spawnParticles(mob.level, ParticleHelper.SNOWFLAKE, mob.getX() + neckVector.x, mob.getY() + neckVector.y + 1, mob.getZ() + neckVector.z, 1, 0, 0, 0, 0, true);
        }
        if (this.lookAtCooldown > 0) {
            float neckLength = NECK_MAX_LENGTH * mob.getScale();
            Vec3 lookAt = new Vec3(this.wantedX, wantedY, wantedZ).subtract(mob.position());

            Vec3 headTargetPos = lookAt.subtract(neckVector).normalize().scale(neckLength); //TODO: better head placement algorithm
            //headPosition = headTargetPos;
            //neckPosition = headPosition.subtract(neckPosition).normalize().scale(neckLength).add(headPosition);
            handleFabrik(headTargetPos);
            for (int i = 0; i < 2; i++) {
                var l = nodeLengths[i];
                for (int j = 0; j < l * 16; j++) {
                    Vec3 vec = headFabrikNodes[i + 1].subtract(headFabrikNodes[i]).normalize().scale(1 / 16f).scale(j).add(headFabrikNodes[i]).add(mob.position());
                    if (mob.level.isClientSide) {
                        mob.level.addParticle(ParticleTypes.BUBBLE, vec.x, vec.y + 1, vec.z, 0, 0, 0);
                    } else {
                        MagicManager.spawnParticles(mob.level, ParticleTypes.BUBBLE, vec.x, vec.y + 1, vec.z, 1, 0, 0, 0, 0, true);
                    }
                }
            }
            headTargetPos = headTargetPos.add(mob.position());
            if (mob.level.isClientSide) {
                mob.level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, headTargetPos.x, headTargetPos.y + 1, headTargetPos.z, 0, 0, 0);
            } else {
                MagicManager.spawnParticles(mob.level, ParticleTypes.SOUL_FIRE_FLAME, headTargetPos.x, headTargetPos.y + 1, headTargetPos.z, 1, 0, 0, 0, 0, true);
            }

            this.lookAtCooldown--;
            this.getYRotD().ifPresent(p_287447_ -> this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, p_287447_, this.yMaxRotSpeed));
            this.getXRotD().ifPresent(p_352768_ -> this.mob.setXRot(this.rotateTowards(this.mob.getXRot(), p_352768_, this.xMaxRotAngle)));

        }/* else {
            this.mob.yHeadRot = this.rotateTowards(this.mob.yHeadRot, this.mob.yBodyRot, 10.0F);
        }*/

//        this.clampHeadRotationToBody();
    }

    public void handleFabrik(Vec3 targetPos) {
        int steps = 5;
        var nodes = headFabrikNodes;
        int nodeCount = headFabrikNodes.length;
        for (int j = 0; j < steps; j++) {
            // Backward
            nodes[nodeCount - 1] = targetPos;
            for (int i = nodeCount - 2; i >= 0; i--) {
                var dir = nodes[i].subtract(nodes[i + 1]).normalize();
                nodes[i] = nodes[i + 1].add(dir.scale(nodeLengths[i]));
            }
            // Forward
            nodes[0] = collarPosition;
            for (int i = 1; i < nodeCount; i++) {
                var dir = nodes[i].subtract(nodes[i - 1]).normalize();
                nodes[i] = nodes[i - 1].add(dir.scale(nodeLengths[i - 1]));
            }
        }
    }

    private Vec3 applyToMobSpace(Vec3 vec) {
        return vec.scale(mob.getScale()).yRot((-mob.yBodyRot * Mth.DEG_TO_RAD + Mth.PI));
    }
}
