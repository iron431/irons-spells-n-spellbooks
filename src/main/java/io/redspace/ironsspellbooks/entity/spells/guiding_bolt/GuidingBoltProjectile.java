package io.redspace.ironsspellbooks.entity.spells.guiding_bolt;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.data.PlayableSound;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class GuidingBoltProjectile extends AbstractMagicProjectile {
    public GuidingBoltProjectile(EntityType<? extends GuidingBoltProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public GuidingBoltProjectile(EntityType<? extends GuidingBoltProjectile> entityType, Level levelIn, Entity shooter) {
        this(entityType, levelIn);
        setOwner(shooter);
    }

    public GuidingBoltProjectile(Level levelIn, Entity shooter) {
        this(EntityRegistry.GUIDING_BOLT.get(), levelIn, shooter);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleHelper.WISP, x, y, z, 25, 0, 0, 0, .18, true);
    }

    @Override
    protected float getBaseSpeed() {
        return 1.3f;
    }

    @Override
    public Optional<PlayableSound> getImpactSound() {
        return impactSound(SoundRegistry.GUIDING_BOLT_IMPACT);
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        discard();

    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        if (DamageSources.applyDamage(entityHitResult.getEntity(), damage, SpellRegistry.GUIDING_BOLT_SPELL.get().getDamageSource(this.level(), this, getOwner()))) {
            if (entityHitResult.getEntity() instanceof LivingEntity livingEntity) {
                livingEntity.addEffect(new MobEffectInstance(MobEffectRegistry.GUIDING_BOLT, getEffectDuration()));
            }
        }
        consumeEntityImpact(entityHitResult, true);

    }

    @Override
    public void trailParticles() {
    }
}
