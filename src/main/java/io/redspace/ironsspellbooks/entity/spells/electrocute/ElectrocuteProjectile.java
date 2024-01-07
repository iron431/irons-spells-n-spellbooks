package io.redspace.ironsspellbooks.entity.spells.electrocute;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ElectrocuteProjectile extends AbstractConeProjectile {
    private List<Vec3> beamVectors;

    public ElectrocuteProjectile(EntityType<? extends AbstractConeProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public ElectrocuteProjectile(Level level, LivingEntity entity) {
        super(EntityRegistry.ELECTROCUTE_PROJECTILE.get(), level, entity);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double pDistance) {
        return super.shouldRenderAtSqrDistance(pDistance);
    }

    @Override
    public boolean shouldRender(double pX, double pY, double pZ) {
        return super.shouldRender(pX, pY, pZ);
    }

    public void generateLightningBeams() {
        //irons_spellbooks.LOGGER.debug("generatingLightningBeams");
        Random random = new Random();
        beamVectors = new ArrayList<>();
        Vec3 coreStart = new Vec3(0, 0, 0);
        int coreLength = random.nextInt(3) + 7;
        for (int core = 0; core < coreLength; core++) {
            Vec3 coreEnd = coreStart.add(0, 0, 1).add(randomVector(.3f).multiply(2.5, 1, 2.5));
            beamVectors.add(coreStart);
            beamVectors.add(coreEnd);
            coreStart = coreEnd;

            int branchSegments = random.nextInt(3) + 1;
            beamVectors.addAll(generateBranch(coreEnd, branchSegments, 0.5f, 1));
        }
    }

    public static List<Vec3> generateBranch(Vec3 origin, int maxLength, float splitChance, int recursionCount) {
        List<Vec3> branchSegements = new ArrayList<>();
        Random random = new Random();
        int branches = random.nextInt(maxLength + 1);
        Vec3 branchStart = origin;
        int dir = random.nextBoolean() ? 1 : -1;
        float branchLength = .75f / (recursionCount + 1);
        for (int i = 0; i < branches; i++) {
            Vec3 branchEnd = branchStart.add(dir * branchLength, 0, branchLength).add(randomVector(.3f));
            branchSegements.add(branchStart);
            branchSegements.add(branchEnd);
            if (random.nextFloat() <= splitChance)
                branchSegements.addAll(generateBranch(branchEnd, maxLength - 1, splitChance * 1.2f, recursionCount + 1));
            branchStart = branchEnd;
        }
        return branchSegements;
    }

    public int getAge() {
        return age;
    }

    public static Vec3 randomVector(float radius) {
        double x = Math.random() * 2 * radius - radius;
        double y = Math.random() * 2 * radius - radius;
        double z = Math.random() * 2 * radius - radius;
        return new Vec3(x, y, z);
    }

    public List<Vec3> getBeamCache() {
        if (beamVectors == null)
            generateLightningBeams();
        return beamVectors;
    }

    @Override
    public void spawnParticles() {
//        var owner = getOwner();
//        if (!level.isClientSide || owner == null) {
//            return;
//        }
//        Vec3 rotation = owner.getLookAngle().normalize();
//        var pos = owner.position().add(rotation.scale(0.5f));
//
//        double x = pos.x;
//        double y = pos.y + owner.getEyeHeight() * .8f;
//        double z = pos.z;
//
//        double speed = random.nextDouble() * .35 + .25;
//        for (int i = 0; i < 1; i++) {
//            double offset = .25;
//            double ox = Math.random() * 2 * offset - offset;
//            double oy = 0;
//            double oz = Math.random() * 2 * offset - offset;
//
//            Vec3 randomVec = new Vec3(Math.random() * 2 - 1, Math.random() * 2 - 1, Math.random() * 2 - 1).normalize();
//            Vec3 result = (rotation.scale(3).add(randomVec)).normalize().scale(speed);
//            level.addParticle(ParticleHelper.ELECTRICITY, x + ox, y + oy, z + oz, result.x, result.y, result.z);
//        }

    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        var entity = entityHitResult.getEntity();
        DamageSources.applyDamage(entity, damage, SpellRegistry.ELECTROCUTE_SPELL.get().getDamageSource(this, getOwner()));

        MagicManager.spawnParticles(level(), ParticleHelper.ELECTRICITY, entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(), 10, entity.getBbWidth() / 3, entity.getBbHeight() / 3, entity.getBbWidth() / 3, 0.1, false);
    }
}
