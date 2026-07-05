package io.redspace.skillcasting.irons_spellbooks.spells.ice;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.effect.FrostbiteEffect;
import io.redspace.ironsspellbooks.particle.SwirlingParticleOptions;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class FrostbiteSpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.shatter_damage", Utils.stringTruncation(getDamage(castContext), 0)),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(60)
            .build();

    public FrostbiteSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 30;
        this.spellPowerPerLevel = 0;
        this.castTime = 0;
        this.baseManaCost = 80;
    }


    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, (int) (getSpellPower(castContext) * 20));
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        if (castContext.asEntityCaster() instanceof LivingEntity entity) {
            entity.addEffect(new MobEffectInstance(MobEffectRegistry.FROSTBITTEN_STRIKES,
                    castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0),
                    getAmplifierForLevel(castContext.getSkillLevel()),
                    false, false, true));
            MagicManager.spawnParticles(level, ParticleHelper.SNOW_DUST, entity.getX(), entity.getY() + 1, entity.getZ(), 50, 0.2, 0.2, 0.2, 0.1, false);
            MagicManager.spawnParticles(level, new SwirlingParticleOptions(ParticleHelper.SNOWFLAKE, new Vec3(0, 1, 0), new Vec3(1, 0, 0),
                    new Vec3(0.75, 0.75, 12), new Vec3(0.025, 0.025, -0.05)), entity.getX(), entity.getY() + 1, entity.getZ(), 35, 0, 0.5, 0, 0.01, false);
        }
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    private float getDamage(CastContext castContext) {
        return FrostbiteEffect.getDamageForAmplifier(getAmplifierForLevel(castContext.getSkillLevel()), castContext.asEntityCaster() instanceof LivingEntity livingEntity ? livingEntity : null);
    }

    private int getAmplifierForLevel(int spellLevel) {
        return spellLevel + 4; // 6 base damage
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }
}
