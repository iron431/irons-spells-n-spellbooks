package io.redspace.skillcasting.irons_spellbooks.spells.blood;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.blood_needle.BloodNeedle;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellSkillDamageSource;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class BloodNeedlesSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(10)
            .build();

    public BloodNeedlesSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 0.25f;
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
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.PROJECTILE_COUNT, 5);
        castContext.set(SkillcastingComponentTypes.PROJECTILE_SPEED, 2.5f);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        int count = castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_COUNT, 5);
        int degreesPerNeedle = 360 / count;
        var raycast = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, 32)
                .checkForBlocks(true)
                .build();
        for (int i = 0; i < count; i++) {
            BloodNeedle needle = new BloodNeedle(level, castContext.asEntityCaster());
            int rotation = degreesPerNeedle * i - (degreesPerNeedle / 2);
            needle.setZRot(rotation);
            // fixme: hardcoded y offset
            Vec3 spawn = castContext.position().add(0,0.25,0).add(new Vec3(0, 1.5, 0).zRot(rotation * Mth.DEG_TO_RAD).xRot(castContext.getXRot()).yRot(castContext.getYRot()));
            needle.moveTo(spawn);
            needle.applyContext(castContext);
            // fixme: speed handling
            needle.setDeltaMovement(raycast.getLocation().subtract(spawn).normalize().scale(castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_SPEED, 2.5f)));
            level.addFreshEntity(needle);
        }
    }

    @Override
    public SpellSkillDamageSource getDamageSource(Level level, @Nullable Entity projectile, @Nullable Entity attacker) {
        return super.getDamageSource(level, projectile, attacker).setLifestealPercent(0.25f).setIFrames(0);
    }
}
