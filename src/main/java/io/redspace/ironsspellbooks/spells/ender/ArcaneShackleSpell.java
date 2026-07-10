package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.ender_chain.ArcaneShackleProjectile;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class ArcaneShackleSpell extends AbstractSpell {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(8)
            .setCooldownSeconds(45)
            .build();

    public ArcaneShackleSpell() {
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 2;
        this.baseManaCost = 40;
        this.manaCostPerLevel = 8;
        this.castTime = 10;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.hp",
                        Utils.stringTruncation(castContext.getOrDefault(SpellcastingComponentTypes.CONSTRUCT_HEALTH, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.duration",
                        Utils.timeFromTicks(castContext.getOrDefault(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, 0), 1)),
                Component.translatable("ui.irons_spellbooks.distance",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 1))
        );
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public Optional<PlayableSound> getCastStartSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.CHARGE_CHAINS).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.of(SoundRegistry.THROW_DAGGER, 2f, .7f, .9f).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SpellcastingComponentTypes.CONSTRUCT_HEALTH, 15 * getSpellPowerMultiplier(castContext));
        castContext.set(SkillcastingComponentTypes.EFFECT_DURATION_TICKS, (int) (100 + getSpellPower(castContext) * 20));
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS, 5f);
        castContext.set(SkillcastingComponentTypes.PROJECTILE_SPEED, 1.2f);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        ArcaneShackleProjectile projectile = new ArcaneShackleProjectile(EntityRegistry.ARCANE_SHACKLE.get(), level);
        projectile.setOwner(castContext.asEntityCaster());
        Vec3 origin = castContext.position(PositionAnchor.CASTING_POSITION);
        projectile.setPos(origin.subtract(0, projectile.getBoundingBox().getYsize() * 0.5f, 0).add(castContext.direction()));
        projectile.shootFromContext(projectile, castContext);
        level.addFreshEntity(projectile);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ONE_HANDED_HORIZONTAL_SWING_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }
}
