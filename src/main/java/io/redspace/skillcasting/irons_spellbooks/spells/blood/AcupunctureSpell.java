package io.redspace.skillcasting.irons_spellbooks.spells.blood;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.blood_needle.BloodNeedle;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.SkillcastingUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AcupunctureSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(20)
            .build();

    public AcupunctureSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 0;
        this.baseManaCost = 25;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.projectile_count", castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_COUNT, 0))
        );
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public boolean checkPreCastConditions(CastContext castContext) {
        return SkillcastingUtils.preCastTargetHelper(castContext, 32, 0.15f);
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, 1 + getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.PROJECTILE_COUNT, (int) ((4 + castContext.getSkillLevel()) * getSpellPower(castContext)));
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        var targetData = castContext.getOrNull(SkillcastingComponentTypes.MULTI_TARGET_ENTITIES);
        if (targetData == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        LivingEntity targetEntity = targetData.getFirstLivingEntityTarget(serverLevel);
        if (targetEntity == null) {
            return;
        }
        int count = castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_COUNT, 0);
        Vec3 center = targetEntity.position().add(0, targetEntity.getEyeHeight() / 2, 0);
        float degreesPerNeedle = 360f / count;
        for (int i = 0; i < count; i++) {
            Vec3 offset = new Vec3(0, Math.random(), 0.55).normalize()
                    .scale(targetEntity.getBbWidth() + 2.75f)
                    .yRot(degreesPerNeedle * i * Mth.DEG_TO_RAD);
            Vec3 spawn = center.add(offset);
            Vec3 motion = center.subtract(spawn).normalize();

            BloodNeedle needle = new BloodNeedle(level, castContext.asEntityCaster());
            needle.moveTo(spawn);
            needle.applyContext(castContext);
            needle.shoot(motion.scale(0.35f));
            needle.setScale(0.4f);
            level.addFreshEntity(needle);
        }
    }
}
