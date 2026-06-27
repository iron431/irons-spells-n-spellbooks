package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class WololoSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(10)
            .build();

    public WololoSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 30;
        this.baseManaCost = 10;
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
        return PlayableSound.standard(SoundEvents.EVOKER_PREPARE_WOLOLO).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 32f);
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        return SkillcastingUtils.preCastTargetHelper(
                castContext,
                castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 32f).intValue(),
                0.35f,
                true,
                livingEntity -> livingEntity instanceof Sheep);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        var targetData = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        LivingEntity target = targetData != null ? targetData.getFirstLivingEntityTarget(serverLevel) : null;
        if (target instanceof Sheep sheep) {
            sheep.setColor(DyeColor.values()[Utils.random.nextInt(DyeColor.values().length)]);
            MagicManager.spawnParticles(level, ParticleTypes.CRIT, sheep.getX(), sheep.getY() + .6, sheep.getZ(), 25, .5, .5, .5, 0, false);
        }
    }
}
