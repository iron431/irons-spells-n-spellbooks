package io.redspace.ironsspellbooks.entity.spells.magic_arrow;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
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
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.EventHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MagicArrowProjectile extends AbstractMagicProjectile {
    private final List<Entity> victims = new ArrayList<>();
    private int hitsPerTick;

    public MagicArrowProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
        this.setPierceLevel(-1); //infinite piercing
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
    public float getSpeed() {
        return 2.7f;
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }


    @Override
    protected void onHitBlock(BlockHitResult pResult) {

    }

    @Override
    public void tick() {
        super.tick();
        hitsPerTick = 0;
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (!victims.contains(entity)) {
            DamageSources.applyDamage(entity, damage, SpellRegistry.MAGIC_ARROW_SPELL.get().getDamageSource(this, getOwner()));
            victims.add(entity);
        }
        if (getPierceLevel() != 0) {
            if (hitsPerTick++ < 5) {
                HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
                if (hitresult.getType() != HitResult.Type.MISS && !EventHooks.onProjectileImpact(this, hitresult)) {
                    onHit(hitresult);
                }
            }
            pierceOrDiscard();
        } else {
            discard();
        }
    }

    BlockPos lastHitBlock;

    @Override
    protected void onHit(HitResult result) {
        //IronsSpellbooks.LOGGER.debug("onHit ({})", result.getType());
        if (!level.isClientSide) {
            var blockPos = BlockPos.containing(result.getLocation());
            if (result.getType() == HitResult.Type.BLOCK && !blockPos.equals(lastHitBlock)) {
                lastHitBlock = blockPos;
            } else if (result.getType() == HitResult.Type.ENTITY) {
                level.playSound(null, BlockPos.containing(position()), SoundRegistry.FORCE_IMPACT.get(), SoundSource.NEUTRAL, 2, .65f);
            }
        }
        super.onHit(result);
    }

    @Override
    protected boolean shouldPierceShields() {
        return true;
    }
}
