package io.redspace.ironsspellbooks.spells.blood;

import com.mojang.math.Axis;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellDamageSource;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.network.particles.BloodSiphonParticlesPacket;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.render.SpellRenderingHelper;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.client.render.ClientSkillTicker;
import io.redspace.skillcasting.client.render.SkillcastLevelRenderableManager;
import io.redspace.skillcasting.data.CastContext;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.data.cast.CastType;
import io.redspace.skillcasting.data.cast.PositionAnchor;
import io.redspace.skillcasting.data.resolver.MobAimDirectionResolver;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RayOfSiphoningSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(15)
            .build();

    public RayOfSiphoningSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0.25f;
        this.castTime = 100;
        this.baseManaCost = 8;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), 1))
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.CONTINUOUS;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.RAY_OF_SIPHONING).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 12f);
        if (castContext.asEntityCaster() instanceof Mob) {
            castContext.set(SkillcastingComponentTypes.DIRECTION_RESOLVER, new MobAimDirectionResolver(castContext));
        }

    }

    @Override
    public void onClientCastStart(CastContext castContext) {
        super.onClientCastStart(castContext);
        // todo: tick manager has an opt-in helper. should this follow the same pattern?
        SkillcastLevelRenderableManager.track(
                castContext.caster(),
                (poseStack, buf, partialTick, caster, data, cast) -> {
                    // fixme: pretty sure this kills the server
                    List<HitResult> hitResults = RaycastBuilder.fromCast(cast.context(), PositionAnchor.CASTING_POSITION)
                            .checkForBlocks(true)
                            .bbInflation(0.15f)
                            .performRaycastWithPiercingAndRicochet(castContext, true);
                    List<Vec3> rayInflectionPoints = new ArrayList<>(List.of(new Vec3(0,-.2,0)));
                    hitResults.stream().map(r -> r.getLocation().subtract(castContext.position(PositionAnchor.CASTING_POSITION_CENTER))).forEach(rayInflectionPoints::add);
                    for (int i = rayInflectionPoints.size() - 2; i >= 0; i--) {
                        // backwards iteration for alpha clipping
                        poseStack.pushPose();
                        Vec3 start = rayInflectionPoints.get(i);
                        Vec3 end = rayInflectionPoints.get(i + 1);
                        Vec3 ray = end.subtract(start);
                        Vec3 direction = ray.normalize();
                        Vec2 rotation = Utils.rotationFromDirection(direction);
                        poseStack.translate(start.x, start.y, start.z);
                        poseStack.mulPose(Axis.YP.rotation(rotation.y));
                        poseStack.mulPose(Axis.XP.rotation(-rotation.x));
                        SpellRenderingHelper.renderRayOfSiphoning(caster.level(), poseStack, start, ray, buf, partialTick);
                        poseStack.popPose();
                    }

                }, false
        );
    }

    @Override
    public void onServerCastTick(CastContext castContext) {
        MobAimDirectionResolver aimResolver = castContext.find(SkillcastingComponentTypes.DIRECTION_RESOLVER).map(resolver -> resolver instanceof MobAimDirectionResolver aim ? aim : null).orElse(null);
        if (aimResolver == null || !(castContext.asEntityCaster() instanceof Mob mob)) {
            return;
        }
        Entity target = mob.getTarget();
        if (target != null) {
            aimResolver.updateAim(castContext, target, 0.15f);
        }
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        List<HitResult> hitResults = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION)
                .checkForBlocks(true)
                .bbInflation(0.15f)
                .performRaycastWithPiercingAndRicochet(castContext, true);
        for (HitResult hitResult : hitResults) {
            if (hitResult instanceof EntityHitResult entityHitResult) {
                Entity target = entityHitResult.getEntity();
                if (target.canBeHitByProjectile()) {
                    if (DamageSources.applyDamage(target, castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f),
                            getDamageSourceIndirect(castContext))) {
                        Vec3 targetPos = target.position().add(0, target.getBbHeight() / 2, 0);
                        Vec3 casterPos = castContext.position(PositionAnchor.CENTER);
                        castContext.caster().distributeToClients(new BloodSiphonParticlesPacket(targetPos, casterPos));
                    }
                }
            }
        }
    }

    @Override
    public Optional<ClientSkillTicker> createClientTicker() {
        return Optional.of((caster, data, cast) -> {
                    var castContext = cast.context();
                    List<HitResult> hitResults = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION)
                            .checkForBlocks(true)
                            .bbInflation(0.15f)
                            .performRaycastWithPiercingAndRicochet(castContext, true);
                    for (var hit : hitResults) {
                        if (hit instanceof BlockHitResult blockHitResult) {
                            Vec3 impact = hit.getLocation().subtract(0, .25, 0);
                            for (int i = 0; i < 8; i++) {
                                Vec3 motion = new Vec3(
                                        Utils.getRandomScaled(.2f),
                                        Utils.getRandomScaled(.2f),
                                        Utils.getRandomScaled(.2f)
                                );
                                caster.level().addParticle(ParticleHelper.SIPHON, impact.x + motion.x, impact.y + motion.y + .2, impact.z + motion.z, motion.x, motion.y, motion.z);
                            }
                        }
                    }
                }
        );
    }

    @Override
    public SpellDamageSource getDamageSource(Level level, @Nullable Entity projectile, @Nullable Entity attacker) {
        return super.getDamageSource(level, projectile, attacker).setLifestealPercent(1f);
    }

    @Override
    public boolean shouldAIStopCasting(CastContext castContext, Mob mob, LivingEntity target) {
        float range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f);
        return mob.distanceToSqr(target) > range * range * 1.2;
    }
}
