package io.redspace.skillcasting.irons_spellbooks.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.icicle.IcicleProjectile;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.List;

public class IcicleSpell extends AbstractSpellSkill {
    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage",
                Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(1)
            .build();

    public IcicleSpell() {
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 0.5f;
        this.castTime = 0;
        this.baseManaCost = 10;
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
        castContext.set(SkillcastingComponentTypes.DAMAGE, castContext.getOrDefault(SpellcastingComponentTypes.SPELL_POWER,0f));
        castContext.set(SkillcastingComponentTypes.PROJECTILE_PIERCE, -1); //fixme: i don't like -1 terminators as "infinity"
        castContext.set(SkillcastingComponentTypes.PROJECTILE_SPEED, 1.4f);
    }

    @Override
    public void onCast(CastContext castContext) {
        IcicleProjectile icicle = new IcicleProjectile(castContext.level(), castContext.asEntityCaster());
        icicle.setPos(castContext.position());
        icicle.shootFromContext(icicle, castContext);
        // todo: gravity components?
        icicle.setNoGravity(true);
        castContext.level().addFreshEntity(icicle);
    }

    // fixme: full skill takeover
//    @Override
//    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
//        return super.getDamageSource(projectile, attacker).setFreezeTicks(80);
//    }
}
