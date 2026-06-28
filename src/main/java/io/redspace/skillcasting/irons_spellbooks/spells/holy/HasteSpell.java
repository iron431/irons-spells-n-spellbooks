package io.redspace.skillcasting.irons_spellbooks.spells.holy;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.cast.CastEndReason;
import io.redspace.skillcasting.api.component.MultiTargetEntityCastComponent;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
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
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class HasteSpell extends AbstractSpellSkill {

    private static final int MAX_TARGETS = 5;

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
        int amplifier = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        return List.of(
                Component.translatable("ui.irons_spellbooks.hastened",
                        Utils.stringTruncation((1 + amplifier) * 0.1f * 100, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 1)),
                Component.translatable("ui.irons_spellbooks.max_victims", MAX_TARGETS));
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
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 3f);
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, (int) (getSpellPower(castContext) * 20));
        castContext.set(SkillcastingComponentTypes.EFFECT_AMPLIFIER, castContext.getSkillLevel() - 1);
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        if (!SkillcastingUtils.preCastTargetHelper(castContext,
                castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 32f).intValue(), 0.35f, false)) {
            if (castContext.asEntityCaster() instanceof LivingEntity self) {
                castContext.set(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES, new MultiTargetEntityCastComponent(self));
                if (self instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(
                            Component.translatable("ui.irons_spellbooks.spell_target_success_self", getDisplayName(serverPlayer))
                                    .withStyle(ChatFormatting.GREEN)));
                }
            }
        }
        if (!(castContext.level() instanceof ServerLevel serverLevel)) {
            return false;
        }
        var targetData = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        LivingEntity target = targetData != null ? targetData.getFirstLivingEntityTarget(serverLevel) : null;
        if (target == null) {
            return false;
        }
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 3f);
        TargetedAreaEntity area = TargetedAreaEntity.createTargetAreaEntity(
                castContext.level(), target.position(), radius, Utils.packRGB(getSchoolType().getTargetingColor()), target);
        castContext.set(SkillcastingComponentTypes.ATTACHED_ENTITIES, new MultiTargetEntityCastComponent(area));
        return true;
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
    public void onCast(Level level, CastContext castContext) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        var targetData = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        LivingEntity targetEntity = targetData != null ? targetData.getFirstLivingEntityTarget(serverLevel) : null;
        if (targetEntity == null) {
            return;
        }
        Entity caster = castContext.asEntityCaster();
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 3f);
        int duration = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0);
        int amplifier = castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_AMPLIFIER, 0);
        AtomicInteger targets = new AtomicInteger(0);
        targetEntity.level().getEntitiesOfClass(LivingEntity.class, targetEntity.getBoundingBox().inflate(radius))
                .forEach(victim -> {
                    if (targets.get() < MAX_TARGETS
                            && victim.distanceToSqr(targetEntity) < radius * radius
                            && Utils.shouldHealEntity(caster, victim)) {
                        victim.addEffect(new MobEffectInstance(MobEffectRegistry.HASTENED, duration, amplifier));
                        targets.incrementAndGet();
                    }
                });
    }
}
