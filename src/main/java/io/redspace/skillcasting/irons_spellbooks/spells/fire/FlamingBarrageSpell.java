package io.redspace.skillcasting.irons_spellbooks.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.fireball.SmallMagicFireball;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Unit;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class FlamingBarrageSpell extends AbstractSpellSkill {
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15)
            .build();

    public FlamingBarrageSpell() {
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 3;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 80;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.projectile_count", castContext.find(SkillcastingComponentTypes.RECAST_CONFIG).map(RecastConfig::totalCasts).orElse(0))
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
    public Optional<RecastConfig> getRecastConfig(CastContext castContext) {
        // todo: should this scale? eldritch blast style?
        return Optional.of(new RecastConfig(5, 120));
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, getSpellPower(castContext));
        castContext.set(SkillcastingComponentTypes.PROJECTILE_SPEED, 0.85f);
        castContext.set(SkillcastingComponentTypes.CURSOR_HOMING, Unit.INSTANCE);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Vec3 origin = castContext.position(PositionAnchor.CASTING_POSITION)
                .add(castContext.direction().normalize().scale(0.2f))
                .subtract(0, 0.15, 0);
        SmallMagicFireball fireball = new SmallMagicFireball(level, castContext.asEntityCaster());
        fireball.applyContext(castContext);
        fireball.setPos(origin.subtract(0, fireball.getBbHeight(), 0));
        Vec3 vec = castContext.direction().add(0, 0.2, 0).normalize();
        fireball.shoot(vec.scale(castContext.getOrDefault(SkillcastingComponentTypes.PROJECTILE_SPEED, 0.5f)), 0.4f);
        level.addFreshEntity(fireball);
    }
}
