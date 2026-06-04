package io.redspace.ironsspellbooks.entity.mobs.dead_king_boss.goals;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;

public class NotIdioticFlyingMoveControl extends MoveControl {
    private final int maxTurn;
    private final boolean hoversInPlace;
    private float hoverHeight = 0.5f;

    public NotIdioticFlyingMoveControl(Mob mob, int maxTurn, boolean hoversInPlace) {
        super(mob);
        this.maxTurn = maxTurn;
        this.hoversInPlace = hoversInPlace;
    }

    @Override
    public void tick() {
        if (this.operation == MoveControl.Operation.STRAFE) {
            float f = (float) this.mob.getAttributeValue(Attributes.FLYING_SPEED);
            float f1 = (float) this.speedModifier * f;

            this.mob.setSpeed(f1);
            this.mob.setZza(this.strafeForwards);
            this.mob.setXxa(this.strafeRight);
        } else if (this.operation == Operation.MOVE_TO) {
            this.mob.setNoGravity(true);
            double d0 = this.wantedX - this.mob.getX();
            double d1 = this.wantedY - this.mob.getY();
            double d2 = this.wantedZ - this.mob.getZ();
            double d3 = d0 * d0 + d1 * d1 + d2 * d2;
            if (d3 < 2.5000003E-7F) {
                this.mob.setYya(0.0F);
                this.mob.setZza(0.0F);
                this.operation = Operation.WAIT;
                return;
            }

            float f = (float) (Mth.atan2(d2, d0) * 180.0F / (float) Math.PI) - 90.0F;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f, 90.0F));
            float effectiveFlyspeed = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.FLYING_SPEED));

            this.mob.setSpeed(effectiveFlyspeed);
            this.mob.setXxa(0.0F);
            double d4 = Math.sqrt(d0 * d0 + d2 * d2);
            if (Math.abs(d1) > 1.0E-5F || Math.abs(d4) > 1.0E-5F) {
                float f2 = (float) (-(Mth.atan2(d1, d4) * 180.0F / (float) Math.PI));
                this.mob.setXRot(this.rotlerp(this.mob.getXRot(), f2, (float) this.maxTurn));
                if (d1 > 0 || Math.abs(d1) > mob.getBoundingBox().getYsize()) {
                    this.mob.setYya((d1 > 0.0 ? effectiveFlyspeed : -effectiveFlyspeed) * .5f);
                } else if (mob.tickCount % 20 == 0 && mob.level.clip(new ClipContext(mob.position(), mob.position().add(0, -hoverHeight, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.ANY, CollisionContext.empty())).getType() == HitResult.Type.BLOCK) {
                    this.mob.setYya(effectiveFlyspeed * .5f);
                }
            }
        } else {
            if (!this.hoversInPlace) {
                this.mob.setNoGravity(false);
            }
            this.mob.setYya(0.0F);
            this.mob.setZza(0.0F);
            this.mob.setXxa(0.0F);
        }
        this.operation = Operation.WAIT;
    }
}
