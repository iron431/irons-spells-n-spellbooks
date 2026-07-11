package io.redspace.ironsspellbooks.spells.ice;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.ray_of_frost.RayOfFrostVisualEntity;
import io.redspace.ironsspellbooks.particle.RayOfFrostRayParticleOptions;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.AbstractSkillProjectile;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class RayOfFrostSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15)
            .build();

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.freeze_time", Utils.timeFromTicks(getFreezeTime(castContext), 2)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), 1))
        );
    }

    public RayOfFrostSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 25;
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
        return PlayableSound.standard(SoundRegistry.RAY_OF_FROST).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, 3 + getSpellPower(castContext) * 1.5f);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 30f);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {

        int ricochetCount = castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_RICOCHET, 0);
//        int piercingCount = castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_PIERCE, 0);
        List<EntityHitResult> entityHitResults = new ArrayList<>();
        HitResult lastHitResult = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION)
                .checkForBlocks(true)
                .bbInflation(.15f)
                .build();
        Vec3 lastOrigin = castContext.position();
        float range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f);
        for (int i = 0; i <= ricochetCount; i++) {
            if (!(lastHitResult instanceof EntityHitResult entityHitResult)) {
                break;
            }
            entityHitResults.add(entityHitResult);
            Vec3 direction = entityHitResult.getLocation().subtract(lastOrigin).normalize();
            lastOrigin = entityHitResult.getLocation();
            Optional<Vec3> ricochet = AbstractSkillProjectile.findRicochetDirection(level, lastOrigin, direction, range, Utils::canHitWithRaycast, entityHitResult.getEntity());
            if (ricochet.isEmpty()) {
                break;
            }
            lastHitResult = RaycastBuilder.begin(level, entityHitResult.getEntity())
                    .start(lastOrigin)
                    .end(ricochet.get(), range)
                    .checkForBlocks(true)
                    .bbInflation(.15f)
                    .build();
        }
        List<Vec3> chainPositions = new ArrayList<>(List.of(castContext.position(PositionAnchor.BOTTOM_CENTER).lerp(castContext.position(PositionAnchor.CASTING_POSITION), 0.8f)));
        chainPositions.addAll(entityHitResults.stream().map(HitResult::getLocation).toList());
        if (lastHitResult.getType() != HitResult.Type.ENTITY) {
            chainPositions.add(lastHitResult.getLocation());
        }
        for (int i = 0; i < chainPositions.size() - 1; i++) {
            Vec3 start = chainPositions.get(i);
            Vec3 end = chainPositions.get(i + 1);
//            level.addFreshEntity(new RayOfFrostVisualEntity(level, start, end, castContext.asEntityCaster()));
            MagicManager.spawnParticles(level, RayOfFrostRayParticleOptions.inner(end), start.x, start.y, start.z, 1, 0, 0, 0, 0, true);
            MagicManager.spawnParticles(level, RayOfFrostRayParticleOptions.outer(end), start.x, start.y, start.z, 1, 0, 0, 0, 0, true);
        }
        for (var entityhit : entityHitResults) {
            Entity target = entityhit.getEntity();
            //Set freeze time right here because it scales off of level and power
            DamageSources.applyDamage(target, castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f),
                    getDamageSourceIndirect(castContext).setFreezeTicks(target.getTicksRequiredToFreeze() + getFreezeTime(castContext)));
            MagicManager.spawnParticles(level, ParticleHelper.SNOW_DUST, entityhit.getLocation().x, entityhit.getLocation().y, entityhit.getLocation().z, 10, 0, .1, 0, .06, false);
        }
//        if (hitResult.getType() == HitResult.Type.ENTITY) {
//            Entity target = ((EntityHitResult) hitResult).getEntity();
//            //Set freeze time right here because it scales off of level and power
//            DamageSources.applyDamage(target, castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f),
//                    getDamageSourceIndirect(castContext).setFreezeTicks(target.getTicksRequiredToFreeze() + getFreezeTime(castContext)));
//            MagicManager.spawnParticles(level, ParticleHelper.ICY_FOG, hitResult.getLocation().x, target.getY(), hitResult.getLocation().z, 4, 0, 0, 0, .3, true);
//        }
        MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE, lastHitResult.getLocation().x, lastHitResult.getLocation().y, lastHitResult.getLocation().z, 50, 0, 0, 0, .3, false);
    }

    private int getFreezeTime(CastContext castContext) {
        return (int) (getSpellPower(castContext) * 15);
    }
}
