package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.small_magic_arrow.SmallMagicArrow;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ArrowVolleyEntity extends AbstractMagicProjectile {
    public ArrowVolleyEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    int duration = 30;

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide) {
            if (tickCount % 5 == 0) {
                //do volley
                int arrows = 7;
                float speed = .75f;
                Vec3 motion = Vec3.directionFromRotation(70 - tickCount / 5f * 7, this.getYRot()).normalize().scale(speed);
                Vec3 orth = new Vec3(-Mth.cos(-this.getYRot() * Mth.DEG_TO_RAD - (float) Math.PI), 0, Mth.sin(-this.getYRot() * Mth.DEG_TO_RAD - (float) Math.PI));
                for (int i = 0; i < arrows; i++) {
                    float distance = (i - arrows * .5f) * .7f;
                    SmallMagicArrow arrow = new SmallMagicArrow(this.level, this.getOwner());
                    arrow.setPos(this.position().add(orth.scale(distance)));
                    arrow.shoot(motion.add(Utils.getRandomVec3(.04f)));
                    level.addFreshEntity(arrow);
                }
            } else if (tickCount > duration) {
                discard();
            }
        }
    }

    @Override
    public void trailParticles() {

    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.empty();
    }
}
