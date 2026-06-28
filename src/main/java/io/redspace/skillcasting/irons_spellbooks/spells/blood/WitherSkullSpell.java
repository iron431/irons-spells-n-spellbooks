package io.redspace.skillcasting.irons_spellbooks.spells.blood;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.WitherSkullProjectile;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class WitherSkullSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.BLOOD_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(1)
            .build();

    public WitherSkullSpell() {
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 12;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 20;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage",
                Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)));
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
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundEvents.WITHER_SHOOT).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        int level = castContext.getSkillLevel();
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext) * 0.5f);
        castContext.set(SkillcastingComponentTypes.PROJECTILE_SPEED, (6 + level) * 0.08f);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        float speed = castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_SPEED, 0.5f);
        float damage = castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f);
        Vec3 direction = castContext.direction();
        var skull = new WitherSkullProjectile(level, castContext.asEntityCaster(), speed, damage, direction);
        skull.applyContext(castContext);
        Vec3 spawn = castContext.position(PositionAnchor.CASTING_POSITION).add(direction);
        skull.moveTo(spawn.x, spawn.y - skull.getBoundingBox().getYsize() / 2, spawn.z,
                castContext.getYRot() * Mth.RAD_TO_DEG + 180, castContext.getXRot() * Mth.RAD_TO_DEG);
        level.addFreshEntity(skull);
    }
}
