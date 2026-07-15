package io.redspace.ironsspellbooks.spells.lightning;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.render.SpellRenderingHelper;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.client.SkillcastLevelRenderableManager;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.skillcasting.lifecycle.ActiveCast;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ElectrocuteSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(12)
            .build();

    public ElectrocuteSpell() {
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 100;
        this.baseManaCost = 3;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.cast_range", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), 2))

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
        return PlayableSound.standard(SoundRegistry.ELECTROCUTE_LOOP).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, 1 + getSpellPower(castContext) * 0.75f);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 10f);
        castContext.set(SkillcastingComponentTypes.RANDOM_SEED, castContext.level().random.nextInt(Integer.MAX_VALUE));
    }

    @Override
    public void onClientCastStart(CastContext castContext) {
        super.onClientCastStart(castContext);
        // todo: tick manager has an opt-in helper. should this follow the same pattern?
        SkillcastLevelRenderableManager.track(
                castContext.caster(),
                (poseStack, buf, partialTick, caster, data, cast) -> {
                    // fixme: pretty sure this kills the server
                    float rangeMultiplier = cast.context().getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 8f) / 9f;
                    SpellRenderingHelper.renderElectrocute(caster.level(), poseStack, rangeMultiplier, buf, castContext.getOrDefault(SkillcastingComponentTypes.RANDOM_SEED, 0), partialTick);
                }
        );
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Set<Entity> entities = SkillcastingUtils.collectConeTargets(castContext,
                target -> target.canBeHitByProjectile() && !DamageSources.isFriendlyFireBetween(castContext.asEntityCaster(), target));
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        entities.forEach(entity -> {
            if (entity instanceof LivingEntity livingEntity) {
                DamageSources.ignoreNextKnockback(livingEntity);
            }
            if (DamageSources.applyDamage(entity, damage, getDamageSource(level, null, castContext.asEntityCaster()))) {
                MagicManager.spawnParticles(level, ParticleHelper.ELECTRICITY,
                        entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                        10, entity.getBbWidth() / 3, entity.getBbHeight() / 3, entity.getBbWidth() / 3, 0.1, false);
            }
        });
    }

    @Override
    public boolean shouldAIStopCasting(CastContext castContext, Mob mob, LivingEntity target) {
        float range = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f);
        return mob.distanceToSqr(target) > range * range * 1.2;
    }
}
