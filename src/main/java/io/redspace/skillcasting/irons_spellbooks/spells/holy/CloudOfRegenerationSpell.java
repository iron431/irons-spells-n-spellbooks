package io.redspace.skillcasting.irons_spellbooks.spells.holy;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellHealEvent;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.network.particles.HealParticlesPacket;
import io.redspace.ironsspellbooks.network.particles.RegenCloudParticlesPacket;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.Optional;

public class CloudOfRegenerationSpell extends AbstractSpellSkill {

    public static final float RADIUS = 5f;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(35)
            .setDeprecated(true)
            .build();

    public CloudOfRegenerationSpell() {
        this.manaCostPerLevel = 3;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0.5f;
        this.castTime = 200;
        this.baseManaCost = 10;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.healing",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.HEALING, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(RADIUS, 1)));
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
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.HOLY_CAST).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.CLOUD_OF_REGEN_LOOP).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.HEALING, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, RADIUS);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        Entity caster = castContext.asEntityCaster();
        Vec3 center = castContext.position();
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, RADIUS);
        float healAmount = castContext.getOrDefault(SkillcastingComponentTypes.HEALING, 0f);
        level.getEntitiesOfClass(LivingEntity.class, AABB.ofSize(center, radius * 2, radius * 2, radius * 2))
                .forEach(target -> {
                    if (center.distanceToSqr(target.position()) < radius * radius && Utils.shouldHealEntity(caster, target)) {
                        if (caster instanceof LivingEntity livingCaster) {
                            NeoForge.EVENT_BUS.post(new SpellHealEvent(livingCaster, target, healAmount, getSchoolType()));
                        }
                        target.heal(healAmount);
                        castContext.caster().distributeToClients(new HealParticlesPacket(target.position()));
                    }
                });
        castContext.caster().distributeToClients(new RegenCloudParticlesPacket(center));
    }
}
