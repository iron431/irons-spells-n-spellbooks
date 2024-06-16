package io.redspace.ironsspellbooks.entity.spells;

import io.redspace.ironsspellbooks.api.events.SpellHealEvent;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;

import java.util.Optional;

public class HealingAoe extends AoeEntity implements AntiMagicSusceptible {

    public HealingAoe(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);

    }

    public HealingAoe(Level level) {
        this(EntityRegistry.HEALING_AOE.get(), level);
    }

    @Override
    public void applyEffect(LivingEntity target) {
        //var owner = getOwner();
        //IronsSpellbooks.LOGGER.debug("HealingAoe apply effect: target: {} owner: {} should heal: {}",target.getName().getString(),owner==null?null:owner.getName().getString(),owner==null?false: Utils.shouldHealEntity((LivingEntity) owner,target));
        if (getOwner() instanceof LivingEntity owner && Utils.shouldHealEntity(owner, target)) {
            float healAmount = getDamage();
            MinecraftForge.EVENT_BUS.post(new SpellHealEvent((LivingEntity) getOwner(), target, healAmount, SchoolRegistry.HOLY.get()));
            target.heal(healAmount);
        }
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return !pTarget.isSpectator() && pTarget.isAlive() && pTarget.isPickable();
    }

    @Override
    public float getParticleCount() {
        return .15f;
    }

    @Override
    public void ambientParticles() {

        if (!level.isClientSide) {
            return;
        }

        int color = PotionUtils.getColor(Potion.byName("healing"));
        double d0 = (double) (color >> 16 & 255) / 255.0D;
        double d1 = (double) (color >> 8 & 255) / 255.0D;
        double d2 = (double) (color >> 0 & 255) / 255.0D;
        float f = getParticleCount();
        f = Mth.clamp(f * getRadius(), f / 4, f * 10);
        for (int i = 0; i < f; i++) {
            if (f - i < 1 && random.nextFloat() > f - i)
                return;
            var r = getRadius();
            Vec3 pos;
            if (isCircular()) {
                float distance = (1 - this.random.nextFloat() * this.random.nextFloat()) * r;
                pos = new Vec3(0, 0, distance).yRot(this.random.nextFloat() * 360);
            } else {
                pos = new Vec3(
                        Utils.getRandomScaled(r * .85f),
                        .2f,
                        Utils.getRandomScaled(r * .85f)
                );
            }
            level.addParticle(ParticleTypes.ENTITY_EFFECT, getX() + pos.x, getY() + pos.y + particleYOffset(), getZ() + pos.z, d0, d1, d2);
        }
    }

    @Override
    protected Vec3 getInflation() {
        return new Vec3(0, 1, 0);
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    @Override
    public void onAntiMagic(MagicData magicData) {
        discard();
    }
}
