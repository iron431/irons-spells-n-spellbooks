package io.redspace.ironsspellbooks.entity.spells.ender_chain;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ArcaneShackleProjectile extends AbstractMagicProjectile {
    private static final int CHAIN_COUNT = 3;

    private float chainHealth = 10f;
    private int chainLifetime = 200;
    private float lashRadius = 8f;
    private float restraintStrength = 0.35f;

    public ArcaneShackleProjectile(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
    }

    public ArcaneShackleProjectile(Level level, LivingEntity shooter) {
        this(EntityRegistry.ARCANE_SHACKLE_PROJECTILE.get(), level);
        setOwner(shooter);
    }

    public void setChainHealth(float chainHealth) {
        this.chainHealth = chainHealth;
    }

    public void setChainLifetime(int chainLifetime) {
        this.chainLifetime = chainLifetime;
    }

    public void setLashRadius(float lashRadius) {
        this.lashRadius = lashRadius;
    }

    public void setRestraintStrength(float restraintStrength) {
        this.restraintStrength = restraintStrength;
    }

    @Override
    public float getSpeed() {
        return 1.2f;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.06;
    }

    @Override
    public void trailParticles() {
        Vec3 motion = getDeltaMovement();
        double x = getX() - motion.x * 0.5;
        double y = getY() - motion.y * 0.5 + getBbHeight() * 0.5;
        double z = getZ() - motion.z * 0.5;
        for (int i = 0; i < 3; i++) {
            Vec3 jitter = Utils.getRandomVec3(0.04f);
            level.addParticle(ParticleHelper.UNSTABLE_ENDER, x + jitter.x, y + jitter.y, z + jitter.z, jitter.x, jitter.y, jitter.z);
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level, ParticleHelper.UNSTABLE_ENDER, x, y, z, 15, .2, .2, .2, .3, false);
    }

    @Override
    public Optional<Holder<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!level.isClientSide && result.getEntity() instanceof LivingEntity victim) {
            spawnChainsOnEntity(victim);
        }
        discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!level.isClientSide) {
            Vec3 impactPos = result.getLocation();
            spawnChainsFromBlock(impactPos);
        }
        discard();
    }

    private void spawnChainsOnEntity(LivingEntity victim) {
//        Vec3 forward = victim.getForward().yRot(60 * Mth.DEG_TO_RAD);
        Vec3 origin = victim.getBoundingBox().getCenter();

        float theta = Mth.TWO_PI / CHAIN_COUNT;
        for (int i = 0; i < CHAIN_COUNT; i++) {
            float angle = theta * i + Mth.TWO_PI / 3;
            float radius = lashRadius * 0.5f + victim.getBbWidth() * .4f;
            Vec3 direction = new Vec3(Mth.cos(angle) * radius, 0, Mth.sin(angle) * radius);
            Vec3 worldPos = Utils.moveToRelativeGroundLevel(victim.level, origin.add(direction), 2);
            if (level.noCollision(AABB.ofSize(worldPos, 0.5, 0.5, 0.5))) {
                Vec3 random = Utils.getRandomVec3(2);
                random = random.subtract(direction.scale(direction.normalize().dot(random.normalize())));
                worldPos = origin.add(direction).add(random);
            }
            spawnChain(victim, worldPos);
        }
    }

    private void spawnChainsFromBlock(Vec3 impactPos) {
        // reduced radius on block hit
        float effectiveRadius = lashRadius * 0.5f;
        AABB searchBox = new AABB(impactPos, impactPos).inflate(lashRadius);
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchBox, entity -> this.canHitEntity(entity) && distanceToSqr(entity) < effectiveRadius * effectiveRadius);
        entities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(impactPos)));

        int count = Math.min(CHAIN_COUNT, entities.size());
        for (int i = 0; i < count; i++) {
            spawnChain(entities.get(i), impactPos);
        }
    }

    private void spawnChain(LivingEntity victim, Vec3 anchor) {
        anchor = Utils.raycastForBlock(victim.level, this.position(), anchor, ClipContext.Fluid.NONE).getLocation();
        EnderChain chain = new EnderChain(level, getOwner(), victim, anchor);
        chain.setHealth(chainHealth);
        chain.setLifetime(chainLifetime);
        chain.setRestraintStrength(restraintStrength);
        level.addFreshEntity(chain);
    }
}
