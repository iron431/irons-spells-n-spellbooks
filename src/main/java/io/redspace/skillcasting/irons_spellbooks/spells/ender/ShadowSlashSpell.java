package io.redspace.skillcasting.irons_spellbooks.spells.ender;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
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
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
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

public class ShadowSlashSpell extends AbstractSpellSkill {

    private static final float DISTANCE = 12f;

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
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        float weaponDamage = castContext.getOrDefault(SkillcastingComponentTypes.WEAPON_DAMAGE, 0f);
        String plus = weaponDamage > 0 ? String.format(" (+%s)", Utils.stringTruncation(weaponDamage, 1)) : "";
        return List.of(Component.translatable("ui.irons_spellbooks.damage",
                Utils.stringTruncation(damage + weaponDamage, 1) + plus));
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
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.SHADOW_SLASH).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        float weaponDamage = 0;
        if (castContext.asEntityCaster() instanceof LivingEntity entity) {
            weaponDamage = Utils.getWeaponDamage(entity);
        }
        castContext.set(SkillcastingComponentTypes.WEAPON_DAMAGE, weaponDamage);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
    }

    @Override
    public void onClientCastStart(CastContext castContext) {
        super.onClientCastStart(castContext);
        if (castContext.asEntityCaster() instanceof LivingEntity entity) {
            entity.setYBodyRot(entity.getYRot());
        }
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Entity caster = castContext.asEntityCaster();
        if (caster == null) {
            return;
        }
        Vec3 forward = castContext.direction();
        Vec3 eyePos = castContext.position(PositionAnchor.CASTING_POSITION);
        Vec3 end = Utils.raycastForBlock(level, eyePos, eyePos.add(forward.scale(DISTANCE)), ClipContext.Fluid.NONE).getLocation();
        AABB hitbox = caster.getBoundingBox().expandTowards(end.subtract(eyePos)).inflate(2);
        var targetableEntities = level.getEntities(caster, hitbox, e ->
                !e.isSpectator()
                        && (e instanceof LivingEntity || e instanceof Projectile)
                        && e.getBoundingBox().getCenter().subtract(caster.getBoundingBox().getCenter()).normalize().dot(caster.getForward()) >= 0.85);
        targetableEntities.sort(Comparator.comparingDouble(caster::distanceToSqr));

        float totalDamage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f)
                + castContext.getOrDefault(SkillcastingComponentTypes.WEAPON_DAMAGE, 0f);
        var damageSource = getDamageSource(level, caster);

        if (!targetableEntities.isEmpty() && targetableEntities.getFirst().distanceToSqr(caster) < DISTANCE * DISTANCE) {
            var closestEntity = targetableEntities.getFirst();
            float radius = 2.5f;
            AABB damageBox = AABB.ofSize(closestEntity.getBoundingBox().getCenter(), radius, radius + 1, radius)
                    .move(forward.scale(radius / 2));
            end = damageBox.getCenter().add(end).scale(0.5);
            var damageEntities = level.getEntities(caster, damageBox);
            boolean projectileEffects = false;
            for (Entity targetEntity : damageEntities) {
                if (targetEntity instanceof Projectile projectile && !projectile.noPhysics && !projectile.getType().is(ModTags.CANT_PARRY)) {
                    projectileEffects = true;
                    projectile.setOwner(caster);
                    projectile.shoot(forward.x, forward.y, forward.z, (float) projectile.getDeltaMovement().length(), 0f);
                } else if (targetEntity.isAlive()
                        && caster.isPickable()
                        && Utils.hasLineOfSight(level, eyePos, targetEntity.getBoundingBox().getCenter(), true)) {
                    if (DamageSources.applyDamage(targetEntity, totalDamage, damageSource)) {
                        MagicManager.spawnParticles(level, ParticleHelper.ENDER_SPARKS,
                                targetEntity.getX(), targetEntity.getY() + targetEntity.getBbHeight() * 0.5f, targetEntity.getZ(),
                                15, targetEntity.getBbWidth() * 0.5f, targetEntity.getBbHeight() * 0.5f, targetEntity.getBbWidth() * 0.5f, 0.25, false);
                        if (level instanceof ServerLevel serverLevel) {
                            EnchantmentHelper.doPostAttackEffects(serverLevel, targetEntity, damageSource);
                        }
                        Vec3 knockback = targetEntity.position().subtract(caster.position()).normalize().add(0, 0.5, 0).normalize();
                        knockback = knockback.scale(Utils.random.nextIntBetweenInclusive(70, 100) / 100f
                                * Utils.clampedKnockbackResistanceFactor(targetEntity, 0.2f, 1f) * 0.1f);
                        targetEntity.setDeltaMovement(targetEntity.getDeltaMovement().add(knockback));
                        targetEntity.hurtMarked = true;
                    }
                }
            }
            if (projectileEffects) {
                level.playSound(null, closestEntity.getX(), closestEntity.getY(), closestEntity.getZ(),
                        SoundRegistry.FIRE_DAGGER_PARRY.get(), caster.getSoundSource());
                MagicManager.spawnParticles(level, ParticleHelper.ENDER_SPARKS,
                        closestEntity.getX(), closestEntity.getY() + closestEntity.getBbHeight() * 0.5f, closestEntity.getZ(),
                        25, 0, 0, 0, 0.4, false);
            }
        }

        Vec3 rayVector = end.subtract(eyePos);
        Vec3 impulse = rayVector.scale(1 / 6f).add(0, 0.1, 0);
        caster.setDeltaMovement(caster.getDeltaMovement().scale(0.2).add(impulse));
        caster.hurtMarked = true;
        if (caster instanceof LivingEntity livingCaster) {
            livingCaster.addEffect(new MobEffectInstance(MobEffectRegistry.FALL_DAMAGE_IMMUNITY, 20, 0, false, false, true));
        }

        Vec3 dashForward = impulse.normalize();
        Vec3 up = new Vec3(0, 1, 0);
        if (dashForward.dot(up) > 0.999) {
            up = new Vec3(1, 0, 0);
        }
        Vec3 right = up.cross(dashForward);
        Vec3 particlePos = end.subtract(dashForward.scale(3)).add(right.scale(-0.3));
        MagicManager.spawnParticles(level,
                new EnderSlashParticleOptions(
                        (float) dashForward.x, (float) dashForward.y, (float) dashForward.z,
                        (float) right.x, (float) right.y, (float) right.z, 1f),
                particlePos.x, particlePos.y + 0.3, particlePos.z, 1, 0, 0, 0, 0, true);

        int trailParticles = 15;
        double speed = rayVector.length() / 12.0 * 0.75;
        for (int i = 0; i < trailParticles; i++) {
            Vec3 particleStart = caster.getBoundingBox().getCenter().add(Utils.getRandomVec3(1 + caster.getBbWidth()));
            Vec3 particleEnd = particleStart.add(rayVector);
            MagicManager.spawnParticles(level, new TraceParticleOptions(Utils.v3f(particleEnd), new Vector3f(1f, 0.333f, 1f)),
                    particleStart.x, particleStart.y, particleStart.z, 1, 0, 0, 0, speed, false);
        }
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.ONE_HANDED_VERTICAL_UPSWING_ANIMATION;
    }
}
