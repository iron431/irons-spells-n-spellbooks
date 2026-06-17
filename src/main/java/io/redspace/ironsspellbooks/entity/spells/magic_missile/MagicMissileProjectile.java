package io.redspace.ironsspellbooks.entity.spells.magic_missile;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MagicMissileProjectile extends AbstractMagicProjectile {
    public MagicMissileProjectile(EntityType<? extends MagicMissileProjectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public MagicMissileProjectile(EntityType<? extends MagicMissileProjectile> entityType, Level levelIn, LivingEntity shooter) {
        this(entityType, levelIn);
        setOwner(shooter);
    }

    public MagicMissileProjectile(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.MAGIC_MISSILE_PROJECTILE.get(), levelIn, shooter);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleHelper.ENDER_SPARKS, x, y, z, 15, 0, 0, 0, .18, true);
    }

    @Override
    protected float getBaseSpeed() {
        return 2.5f;
    }

    @Override
    public Optional<PlayableSound> getImpactSound() {
        return PlayableSound.standard(SoundRegistry.ARCANE_IMPACT).toOpt();
    }

    @Override
    protected void onHitBlock(BlockHitResult blockHitResult) {
        super.onHitBlock(blockHitResult);
        discard();
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult entityHitResult) {
        super.onHitEntity(entityHitResult);
        DamageSources.applyDamage(entityHitResult.getEntity(), damage, SpellRegistry.MAGIC_MISSILE_SPELL.get().getDamageSource(this, getOwner()));
        consumeEntityImpact(entityHitResult, true);
    }

    @Override
    public void trailParticles() {
        var vec = getDeltaMovement();
        var length = vec.length();
        int count = (int) Math.min(20, Math.round(length) * 3) + 1;
        float f = (float) length / count;
        for (int i = 0; i < count; i++) {
            Vec3 random = Utils.getRandomVec3(0.02);
            Vec3 p = vec.scale(f * i);
            level.addParticle(ParticleHelper.UNSTABLE_ENDER, this.getX() + random.x + p.x, this.getY() + random.y + p.y, this.getZ() + random.z + p.z, random.x, random.y, random.z);
        }
    }
}
