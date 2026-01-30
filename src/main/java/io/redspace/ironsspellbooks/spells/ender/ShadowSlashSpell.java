package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.EnderSlashParticleOptions;
import io.redspace.ironsspellbooks.particle.TraceParticleOptions;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ModTags;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class ShadowSlashSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "shadow_slash");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", getDamageText(spellLevel, caster)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15)
            .build();

    public ShadowSlashSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 30;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
        super.onClientCast(level, spellLevel, entity, castData);
        // align body with arms so the sword animation plays more smoothly
        entity.setYBodyRot(entity.getYRot());
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.SHADOW_SLASH.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        float distance = 12f; //todo: scale with power
        Vec3 forward = entity.getForward();
        Vec3 end = Utils.raycastForBlock(level, entity.getEyePosition(), entity.getEyePosition().add(forward.scale(distance)), ClipContext.Fluid.NONE).getLocation();
        AABB hitbox = entity.getHitbox().expandTowards(end.subtract(entity.getEyePosition())).inflate(2);
        var targetableEntities = level.getEntities(entity, hitbox, e ->
                !e.isSpectator() &&
                        (e instanceof LivingEntity || e instanceof Projectile) &&
                        e.getBoundingBox().getCenter().subtract(entity.getBoundingBox().getCenter()).normalize().dot(entity.getForward()) >= .85);
        targetableEntities.sort(Comparator.comparingDouble(e -> e.distanceToSqr(entity)));
        if (!targetableEntities.isEmpty() && targetableEntities.get(0).distanceToSqr(entity) < distance * distance) {
            var closestEntity = targetableEntities.get(0);

            float radius = 2.5f;
            AABB damageBox = AABB.ofSize(closestEntity.getBoundingBox().getCenter(), radius, radius + 1, radius).move(forward.scale(radius / 2));
            // dampen end position by taking average of it and the impact location
            end = damageBox.getCenter().add(end).scale(0.5);
            var damageEntities = level.getEntities(entity, damageBox);
            var damageSource = this.getDamageSource(entity);
            boolean projectileEffects = false;
            for (Entity targetEntity : damageEntities) {
                if (targetEntity instanceof Projectile projectile && !projectile.noPhysics && !projectile.getType().is(ModTags.CANT_PARRY)) {
                    projectileEffects = true;
                    projectile.setOwner(entity);
                    projectile.shoot(forward.x, forward.y, forward.z, (float) projectile.getDeltaMovement().length(), 0f);
                } else if (targetEntity.isAlive() &&
                        entity.isPickable() &&
                        Utils.hasLineOfSight(level, entity.getEyePosition(), targetEntity.getBoundingBox().getCenter(), true)) {
                    if (DamageSources.applyDamage(targetEntity, getDamage(spellLevel, entity), damageSource)) {
                        MagicManager.spawnParticles(level, ParticleHelper.ENDER_SPARKS, targetEntity.getX(), targetEntity.getY() + targetEntity.getBbHeight() * .5f, targetEntity.getZ(), 15, targetEntity.getBbWidth() * .5f, targetEntity.getBbHeight() * .5f, targetEntity.getBbWidth() * .5f, .25, false);
                        EnchantmentHelper.doPostAttackEffects((ServerLevel) level, targetEntity, damageSource);
//                        targetEntity.setDeltaMovement(targetEntity.getDeltaMovement().add(end.subtract(targetEntity.position()).scale(1 / 6f)));
                        Vec3 knockback = targetEntity.position().subtract(entity.position()).normalize().add(0, 0.5, 0).normalize();
                        knockback.scale(Utils.random.nextIntBetweenInclusive(70, 100) / 100f *
                                Utils.clampedKnockbackResistanceFactor(targetEntity, .2f, 1f) * .1f);
                        targetEntity.setDeltaMovement(targetEntity.getDeltaMovement().add(knockback));

                        targetEntity.hurtMarked = true;
                    }
                }
            }
            if (projectileEffects) {
                level.playSound(null, closestEntity.getX(), closestEntity.getY(), closestEntity.getZ(), SoundRegistry.FIRE_DAGGER_PARRY.get(), entity.getSoundSource());
                MagicManager.spawnParticles(level, ParticleHelper.ENDER_SPARKS, closestEntity.getX(), closestEntity.getY() + closestEntity.getBbHeight() * .5f, closestEntity.getZ(), 25, 0, 0, 0, .4, false);
            }
        }
        Vec3 rayVector = end.subtract(entity.getEyePosition());
        Vec3 impulse = rayVector.scale(1 / 6f).add(0, 0.1, 0);
        entity.setDeltaMovement(entity.getDeltaMovement().scale(0.2).add(impulse));
        entity.hurtMarked = true;
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.FALL_DAMAGE_IMMUNITY, 20, 0, false, false, true));

        forward = impulse.normalize(); // recalculate forward as the direction we are actually moving
        Vec3 up = new Vec3(0, 1, 0);
        if (forward.dot(up) > .999) {
            up = new Vec3(1, 0, 0);
        }
        Vec3 right = up.cross(forward);
        Vec3 particlePos = end.subtract(forward.scale(3)).add(right.scale(-0.3));
        MagicManager.spawnParticles(level,
                new EnderSlashParticleOptions(
                        (float) forward.x,
                        (float) forward.y,
                        (float) forward.z,
                        (float) right.x,
                        (float) right.y,
                        (float) right.z,
                        1f),
                particlePos.x, particlePos.y + .3, particlePos.z, 1, 0, 0, 0, 0, true);
        int trailParticles = 15;
        double speed = rayVector.length() / 12.0 * .75;
        for (int i = 0; i < trailParticles; i++) {
            Vec3 particleStart = entity.getBoundingBox().getCenter().add(Utils.getRandomVec3(1 + entity.getBbWidth()));
            Vec3 particleEnd = particleStart.add(rayVector);
            MagicManager.spawnParticles(level, new TraceParticleOptions(Utils.v3f(particleEnd), new Vector3f(1f, .333f, 1f)), particleStart.x, particleStart.y, particleStart.z, 1, 0, 0, 0, speed, false);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return getSpellPower(spellLevel, entity) + Utils.getWeaponDamage(entity);
    }

    private String getDamageText(int spellLevel, LivingEntity entity) {
        if (entity != null) {
            float weaponDamage = Utils.getWeaponDamage(entity);
            String plus = "";
            if (weaponDamage > 0) {
                plus = String.format(" (+%s)", Utils.stringTruncation(weaponDamage, 1));
            }
            String damage = Utils.stringTruncation(getDamage(spellLevel, entity), 1);
            return damage + plus;
        }
        return "" + getSpellPower(spellLevel, entity);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ONE_HANDED_VERTICAL_UPSWING_ANIMATION;
    }
}
