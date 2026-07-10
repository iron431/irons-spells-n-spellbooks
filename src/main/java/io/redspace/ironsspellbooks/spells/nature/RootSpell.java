package io.redspace.ironsspellbooks.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.root.RootEntity;
import io.redspace.ironsspellbooks.util.ModTags;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class RootSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(35)
            .build();

    public RootSpell() {
        this.manaCostPerLevel = 3;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 50;
        this.baseManaCost = 45;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length",
                        Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 1)),
                Component.translatable("ui.irons_spellbooks.hp",
                        Utils.stringTruncation(castContext.getOrDefault(
                                SpellcastingComponentTypes.CONSTRUCT_HEALTH, 0f), 1))
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
        return PlayableSound.standard(SoundEvents.EVOKER_PREPARE_ATTACK).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        float powerMultiplier = castContext.getOrDefault(SpellcastingComponentTypes.SPELL_POWER_MULTIPLIER, 1f);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 32f);
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, (int) (getSpellPower(castContext) * 20));
        castContext.set(SpellcastingComponentTypes.CONSTRUCT_HEALTH, 40 * powerMultiplier);
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        return SkillcastingUtils.preCastTargetHelper(castContext, 0.35f, true,
                target -> !target.getType().is(ModTags.CANT_ROOT));
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        LivingEntity target = SkillcastingUtils.getTargetedLivingEntity(level, castContext);
        if (target == null || target.getType().is(ModTags.CANT_ROOT)) {
            return;
        }
        Vec3 spawn = target.position();
        float health = castContext.getOrDefault(SpellcastingComponentTypes.CONSTRUCT_HEALTH, 0f);
        RootEntity rootEntity = new RootEntity(level, castContext.asEntityCaster());
        rootEntity.setDuration(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0));
        rootEntity.setTarget(target);
        rootEntity.moveTo(spawn);
        rootEntity.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        rootEntity.setHealth(health);
        level.addFreshEntity(rootEntity);
        target.stopRiding();
        target.startRiding(rootEntity, true);
    }
}
