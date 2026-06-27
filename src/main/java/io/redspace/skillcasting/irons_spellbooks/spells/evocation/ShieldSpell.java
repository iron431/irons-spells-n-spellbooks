package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.shield.ShieldEntity;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class ShieldSpell extends AbstractSpellSkill {

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.hp", Utils.stringTruncation(castContext.getOrDefault(SpellcastingComponentTypes.CONSTRUCT_HEALTH, 0f), 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(8)
            .build();

    public ShieldSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 10;
        this.baseManaCost = 35;
        this.castTime = 0;
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
        return PlayableSound.standard(SoundEvents.ILLUSIONER_CAST_SPELL).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SpellcastingComponentTypes.CONSTRUCT_HEALTH, 10 + getSpellPower(castContext));
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        ShieldEntity shield = new ShieldEntity(level, castContext.getOrDefault(SpellcastingComponentTypes.CONSTRUCT_HEALTH, 0f));
        Vec3 spawn = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, 3f)
                .checkForBlocks(true)
                .build()
                .getLocation();
        shield.setPos(spawn);
        Entity caster = castContext.asEntityCaster();
        float xRot = caster != null ? caster.getXRot() : castContext.getXRot() * Mth.RAD_TO_DEG;
        float yRot = caster != null ? caster.getYRot() : castContext.getYRot() * Mth.RAD_TO_DEG;
        shield.setRotation(xRot, yRot);
        level.addFreshEntity(shield);
    }
}
