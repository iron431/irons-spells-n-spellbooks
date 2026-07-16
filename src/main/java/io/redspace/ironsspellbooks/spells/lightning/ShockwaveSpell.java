package io.redspace.ironsspellbooks.spells.lightning;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.CameraShakeData;
import io.redspace.ironsspellbooks.api.util.CameraShakeManager;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.ZapParticleOption;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.data.cast.PositionAnchor;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class ShockwaveSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(30)
            .build();

    public ShockwaveSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 1;
        this.castTime = 16;
        this.baseManaCost = 70;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.radius",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 2))
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.SHOCKWAVE_PREPARE).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.of(SoundRegistry.SHOCKWAVE_CAST, 3f, 0.9f, 1.1f).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, 4 + getSpellPower(castContext) * 0.75f);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 8f + castContext.getSkillLevel());
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f);
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        float radiusSqr = radius * radius;
        Entity caster = castContext.asEntityCaster();

        Vector3f edge = new Vector3f(.7f, 1f, 1f);
        Vector3f blastCenter = new Vector3f(1, 1f, 1f);
        Vec3 ringOrigin = castContext.position(PositionAnchor.BOTTOM_CENTER).add(0, 0.165, 0);
        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(edge, radius * 1.02f), ringOrigin.x, ringOrigin.y + .15f, ringOrigin.z, 1, 0, 0, 0, 0, true);
        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(edge, radius * 0.98f), ringOrigin.x, ringOrigin.y + .15f, ringOrigin.z, 1, 0, 0, 0, 0, true);
        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(blastCenter, radius), ringOrigin.x, ringOrigin.y + .165f, ringOrigin.z, 1, 0, 0, 0, 0, true);
        MagicManager.spawnParticles(level, new BlastwaveParticleOptions(blastCenter, radius), ringOrigin.x, ringOrigin.y + .135f, ringOrigin.z, 1, 0, 0, 0, 0, true);
        MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, ringOrigin.x, ringOrigin.y + 1, ringOrigin.z, 80, .25, .25, .25, 0.7f + radius * .1f, false);
        CameraShakeManager.addCameraShake(new CameraShakeData(level, 20, ringOrigin, radius * 2, 10));
        Vec3 center = Utils.moveToRelativeGroundLevel(level, castContext.position(PositionAnchor.CENTER), 2).add(0,1,0);

        var dummyLightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, level);
        dummyLightningBolt.setDamage(0);
        dummyLightningBolt.setVisualOnly(true);
        AABB searchBox = AABB.ofSize(center, radius * 2, radius * 2, radius * 2);
        level.getEntities(caster, searchBox, target ->
                        !DamageSources.isFriendlyFireBetween(target, caster)
                                && Utils.hasLineOfSight(level, center, target.getBoundingBox().getCenter(), true))
                .forEach(target -> {
                    if (target instanceof LivingEntity livingEntity && canHit(caster, target) && livingEntity.distanceToSqr(ringOrigin) < radiusSqr) {
                        Vec3 dest = livingEntity.getBoundingBox().getCenter();
                        if (level instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(new ZapParticleOption(dest), center.x, center.y, center.z, 1, 0, 0, 0, 0);
                        }
                        MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY, livingEntity.getX(), livingEntity.getY() + livingEntity.getBbHeight() / 2, livingEntity.getZ(),
                                10, livingEntity.getBbWidth() / 3, livingEntity.getBbHeight() / 3, livingEntity.getBbWidth() / 3, 0.1, false);
                        DamageSources.applyDamage(target, damage, getDamageSourceIndirect(castContext));
                        if (target instanceof Creeper creeper && level instanceof ServerLevel serverLevel) {
                            creeper.thunderHit(serverLevel, dummyLightningBolt);
                        }
                    }
                });
        if (level instanceof ServerLevel serverLevel) {
            for (int i = 0; i < 3 + radius * 0.5f; i++) {
                Vec3 dest = Utils.getRandomVec3(1).add(0, 0.75, 0).scale(radius).multiply(0.75f, 0.25f, 0.75f).add(center);
                serverLevel.sendParticles(new ZapParticleOption(dest), center.x, center.y, center.z, 1, 0, 0, 0, 0);
            }
        }
    }

    private static boolean canHit(Entity owner, Entity target) {
        return target != owner && target.isAlive() && target.isPickable() && !target.isSpectator();
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.PREPARE_CROSS_ARMS;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.CAST_T_POSE;
    }
}
