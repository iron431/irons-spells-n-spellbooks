package io.redspace.skillcasting.irons_spellbooks.spells.blood;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.network.particles.BloodSiphonParticlesPacket;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.render.SpellRenderingHelper;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.resolver.MobAimDirectionResolver;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.client.ClientSkillTicker;
import io.redspace.skillcasting.client.SkillcastLevelRenderableManager;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellDamageSource;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
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

//    @Override
//    public boolean checkPreCastConditions(CastContext castContext) {
//        if(SkillcastingUtils.preCastTargetHelper(castContext, 0.35f, false)){
//            castContext.set(SkillcastingComponentTypes.DIRECTION_RESOLVER, new MobAimDirectionResolver(castContext));
//        }
//        return true;
//    }

    @Override
    public void onClientCastStart(CastContext castContext) {
        super.onClientCastStart(castContext);
        // todo: tick manager has an opt-in helper. should this follow the same pattern?
        SkillcastLevelRenderableManager.track(
                castContext.caster(),
                (poseStack, buf, partialTick, caster, data, cast) -> {
                    var hitResult = RaycastBuilder.fromCast(cast.context(), PositionAnchor.CASTING_POSITION)
                            .checkForBlocks(true)
                            .bbInflation(0.15f)
                            .filter(Utils::canHitWithRaycast)
                            .build();
                    // fixme: pretty sure this kills the server
                    SpellRenderingHelper.renderRayOfSiphoning(caster.level(), poseStack, castContext.position(PositionAnchor.CASTING_POSITION).subtract(
                                    castContext.position(PositionAnchor.ORIGIN)
                            ).subtract(castContext.direction().scale(0.25)).subtract(0, 0.25, 0),
                            hitResult.getLocation().subtract(castContext.position(PositionAnchor.CASTING_POSITION)), buf, partialTick);
                }
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
        var hitResult = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION)
                .checkForBlocks(true)
                .bbInflation(0.15f)
                .build();
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) hitResult).getEntity();
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

    @Override
    public Optional<ClientSkillTicker> createClientTicker() {
        return Optional.of((caster, data, cast) -> {
                    HitResult hit = RaycastBuilder.fromCast(cast.context(), PositionAnchor.CASTING_POSITION)
                            .checkForBlocks(true)
                            .build();
                    Vec3 impact = hit.getLocation().subtract(0, .25, 0);
                    for (int i = 0; i < 8; i++) {
                        Vec3 motion = new Vec3(
                                Utils.getRandomScaled(.2f),
                                Utils.getRandomScaled(.2f),
                                Utils.getRandomScaled(.2f)
                        );
                        caster.level().addParticle(ParticleHelper.SIPHON, impact.x + motion.x, impact.y + motion.y, impact.z + motion.z, motion.x, motion.y, motion.z);
                    }
                }
        );
    }

    @Override
    public SpellDamageSource getDamageSource(Level level, @Nullable Entity projectile, @Nullable Entity attacker) {
        return super.getDamageSource(level, projectile, attacker).setLifestealPercent(1f);
    }

    @Override
    public boolean shouldAIStopCasting(ActiveCast cast, Mob mob, LivingEntity target) {
        float range = cast.context().getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f);
        return mob.distanceToSqr(target) > range * range * 1.2;
    }
}
