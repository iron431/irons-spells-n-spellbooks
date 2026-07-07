package io.redspace.skillcasting.irons_spellbooks.spells.holy;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.network.particles.AbsorptionParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.FortifyAreaParticlesPacket;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.component.TargetedEntitiesData;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class FortifySpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(180)
            .build();

    public FortifySpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 1;
        this.castTime = 60;
        this.baseManaCost = 80;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.absorption",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 0)),
                Component.translatable("ui.irons_spellbooks.radius",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 1)));
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
        return PlayableSound.standard(SoundRegistry.CLOUD_OF_REGEN_LOOP).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 8f);
    }

    @Override
    public void onServerCastStart(CastContext castContext) {
        super.onServerCastStart(castContext);
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f);
        int channelTicks = castContext.getOrDefault(SkillcastingComponentTypes.CAST_TIME, castTime);
        TargetedAreaEntity area = TargetedAreaEntity.createTargetAreaEntity(
                castContext.level(), castContext.position(), radius, 16239960, castContext.asEntityCaster());
        area.setDuration(channelTicks);
        castContext.set(SkillcastingComponentTypes.ATTACHED_ENTITIES, new TargetedEntitiesData(area));
    }

    @Override
    public void onServerCastComplete(CastContext castContext, CastEndReason reason) {
        super.onServerCastComplete(castContext, reason);
        if (castContext.level() instanceof ServerLevel serverLevel) {
            castContext.find(SkillcastingComponentTypes.ATTACHED_ENTITIES).ifPresent(
                    entities -> entities.getTargets().forEach(uuid -> {
                        if (serverLevel.getEntity(uuid) instanceof TargetedAreaEntity targetEntity) {
                            targetEntity.discard();
                        }
                    }));
        }
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Entity caster = castContext.asEntityCaster();
        Vec3 center = castContext.position();
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f);
        float power = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        level.getEntitiesOfClass(LivingEntity.class,
                new AABB(center.subtract(radius, radius, radius), center.add(radius, radius, radius)))
                .forEach(target -> {
                    if (Utils.shouldHealEntity(caster, target) && center.distanceTo(target.position()) <= radius) {
                        target.addEffect(new MobEffectInstance(MobEffectRegistry.FORTIFY, 20 * 120, (int) power - 1, false, false, true));
                        castContext.caster().distributeToClients(new AbsorptionParticlesPacket(target.position()));
                    }
                });
        castContext.caster().distributeToClients(new FortifyAreaParticlesPacket(castContext.position(PositionAnchor.BOTTOM_CENTER)));
    }
}
