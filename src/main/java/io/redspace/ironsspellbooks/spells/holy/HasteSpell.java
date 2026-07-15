package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.effect.HastenedEffect;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.component.TargetedEntitiesData;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.List;
import java.util.Optional;

public class HasteSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.HOLY_RESOURCE)
            .setMaxLevel(4)
            .setCooldownSeconds(80)
            .build();

    public HasteSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 30;
        this.spellPowerPerLevel = 5;
        this.castTime = 30;
        this.baseManaCost = 50;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.hastened",
                        Utils.stringTruncation(HastenedEffect.getPercentForAmplifier(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0)) * 100, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 1))
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
        return PlayableSound.standard(SoundRegistry.CLOUD_OF_REGEN_LOOP).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 32f);
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, (int) (getSpellPower(castContext) * 20));
        int base = 8 - 1; // 20%
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, (int) (base * getSpellPowerMultiplier(castContext)));
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        Entity caster = castContext.asEntityCaster();
        if (!SkillcastingUtils.preCastTargetHelper(castContext, 0.35f, false,
                target -> caster == null || Utils.shouldHealEntity(caster, target))) {
            if (castContext.asEntityCaster() instanceof LivingEntity self) {
                castContext.set(SkillcastingComponentTypes.TARGETED_ENTITIES, new TargetedEntitiesData(self));
                if (self instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                            Component.translatable("ui.irons_spellbooks.spell_target_success_self", getDisplayName(serverPlayer))
                                    .withStyle(ChatFormatting.GREEN)));
                }
            }
        }
        return true;
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        LivingEntity targetEntity = SkillcastingUtils.getTargetedLivingEntity(level, castContext);
        if (targetEntity == null) {
            return;
        }
        int duration = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0);
        int amplifier = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        targetEntity.addEffect(new MobEffectInstance(MobEffectRegistry.HASTENED, duration, amplifier, false, false, true));
        MagicManager.spawnParticles(level, ParticleHelper.CLEANSE_PARTICLE,
                targetEntity.getX(), targetEntity.getY() + 0.25, targetEntity.getZ(),
                15, targetEntity.getBbWidth() * 0.5, targetEntity.getBbWidth() * 0.5, targetEntity.getBbWidth() * 0.5, 0, false);
    }
}
