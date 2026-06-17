package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.skillcasting.api.AbstractSkillProjectile;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractMagicProjectile extends AbstractSkillProjectile implements AntiMagicSusceptible {

    public AbstractMagicProjectile(EntityType<? extends AbstractMagicProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (!shouldPierceShields() && (entityHitResult.getEntity() instanceof ShieldPart || entityHitResult.getEntity() instanceof AbstractShieldEntity)) {
            // simulate block impact (likely destroying projectile) due to magic shield impact
            this.onHitBlock(new BlockHitResult(entityHitResult.getEntity().position(), Direction.fromYRot(this.getYRot()), entityHitResult.getEntity().blockPosition(), false));
        }
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        this.impactParticles(getX(), getY(), getZ());
        this.discard();
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    /**
     * Whether the projectile should treat magic shields as a block impact
     */
    protected boolean shouldPierceShields() {
        return false;
    }
}