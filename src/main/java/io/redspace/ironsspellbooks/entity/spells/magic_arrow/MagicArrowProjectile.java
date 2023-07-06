package io.redspace.ironsspellbooks.entity.spells.magic_arrow;

import io.redspace.ironsspellbooks.api.spells.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MagicArrowProjectile extends AbstractMagicProjectile {
    private final List<Entity> victims = new ArrayList<>();
    private int penetration;

    @Override
    public void trailParticles() {
        Vec3 vec3 = this.position().subtract(getDeltaMovement());
        level.addParticle(ParticleHelper.UNSTABLE_ENDER, vec3.x, vec3.y, vec3.z, 0, 0, 0);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, x, y, z, 15, .1, .1, .1, .5, false);
    }

    @Override
    public float getSpeed() {
        return 2.7f;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.empty();
    }

    public MagicArrowProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public MagicArrowProjectile(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.MAGIC_ARROW_PROJECTILE.get(), levelIn);
        setOwner(shooter);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {

    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (!victims.contains(entity)) {
            DamageSources.applyDamage(entity, damage, SpellRegistry.MAGIC_ARROW_SPELL.get().getDamageSource(this, getOwner()), SchoolType.ENDER);
            victims.add(entity);
        }
    }


    @Override
    protected void onHit(HitResult result) {
        //IronsSpellbooks.LOGGER.debug("onHit ({})", result.getType());

        penetration++;
        if (!level.isClientSide) {
            if (result.getType() == HitResult.Type.ENTITY) {
                level.playSound(null, new BlockPos(position()), SoundRegistry.FORCE_IMPACT.get(), SoundSource.NEUTRAL, 2, .65f);
                //IronsSpellbooks.LOGGER.debug("Playing Sound");
            }
        }

        super.onHit(result);
    }
}
