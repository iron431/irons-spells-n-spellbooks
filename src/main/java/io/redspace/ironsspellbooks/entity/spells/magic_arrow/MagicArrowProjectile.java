package io.redspace.ironsspellbooks.entity.spells.magic_arrow;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MagicArrowProjectile extends AbstractMagicProjectile {
    protected final List<UUID> victims = new ArrayList<>();
    protected int blockHits;
    protected BlockPos lastHitBlock;

    public MagicArrowProjectile(EntityType<? extends MagicArrowProjectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
        this.setInfinitePiercing();
    }

    public MagicArrowProjectile(Level levelIn, LivingEntity shooter) {
        this(EntityRegistry.MAGIC_ARROW_PROJECTILE.get(), levelIn);
        setOwner(shooter);
    }

    @Override
    public void trailParticles() {
        var vec = getDeltaMovement();
        var length = vec.length();
        int count = (int) Math.min(20, Math.round(length) * 2) + 1;
        float f = (float) length / count;
        for (int i = 0; i < count; i++) {
            Vec3 random = Utils.getRandomVec3(0.025);
            Vec3 p = vec.scale(f * i);
            level.addParticle(ParticleHelper.UNSTABLE_ENDER, this.getX() + random.x + p.x, this.getY() + random.y + p.y, this.getZ() + random.z + p.z, random.x, random.y, random.z);
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, x, y, z, 10, .1, .1, .1, .4, false);
    }

    @Override
    protected float getBaseSpeed() {
        return 2.7f;
    }

    @Override
    public Optional<PlayableSound> getImpactSound() {
        return impactSound(SoundRegistry.FORCE_IMPACT);
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult pResult) {
        var blockPos = BlockPos.containing(pResult.getLocation());
        if (pResult.getType() == HitResult.Type.BLOCK && !blockPos.equals(lastHitBlock)) {
            lastHitBlock = blockPos;
            if (blockHits++ > 5) {
                discard();
            }
        }

    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (!victims.contains(entity.getUUID())) {
            DamageSources.applyDamage(entity, damage, SpellRegistry.MAGIC_ARROW_SPELL.get().getDamageSource(this, getOwner()));
            victims.add(entity.getUUID());
        }
        consumeEntityImpact(entityHitResult, true);
    }


    @Override
    protected boolean shouldPierceShields() {
        return true;
    }

    @Override
    public boolean collidesWithBlocks() {
        return false;
    }
}
