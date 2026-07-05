package io.redspace.skillcasting.irons_spellbooks.spells.ender;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.black_hole.BlackHole;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import io.redspace.skillcasting.api.PositionAnchor;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class BlackHoleSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(120)
            .build();

    public BlackHoleSpell() {
        this.manaCostPerLevel = 100;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 100;
        this.baseManaCost = 300;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.aoe_damage",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 1)),
                Component.translatable("ui.irons_spellbooks.radius",
                        Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f), 1))
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
        return PlayableSound.standard(SoundRegistry.BLACK_HOLE_CHARGE).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.BLACK_HOLE_CAST).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        float power = getSpellPower(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, power * 2);
        castContext.set(SkillcastingComponentTypes.CAST_RADIUS,
                (2 * castContext.getSkillLevel() + 4) + (0.125f * power));
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        float radius = castContext.getOrDefault(SkillcastingComponentTypes.CAST_RADIUS, 0f);
        HitResult raycast = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, 16 + radius * 1.5f)
                .checkForBlocks(true)
                .build();
        Vec3 center = raycast.getLocation();
        if (raycast instanceof BlockHitResult blockHitResult) {
            if (blockHitResult.getDirection().getAxis().isHorizontal()) {
                center = center.subtract(0, radius, 0);
            } else if (blockHitResult.getDirection() == Direction.DOWN) {
                center = center.subtract(0, radius * 2 - 1, 0);
            } else {
                center = center.subtract(0, 1, 0);
            }
        }

        level.playSound(null, center.x, center.y, center.z, SoundRegistry.BLACK_HOLE_CAST.get(), SoundSource.AMBIENT, 4, 1);

        BlackHole blackHole = new BlackHole(level, castContext.asEntityCaster());
        blackHole.setRadius(radius);
        blackHole.setDamage(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f));
        blackHole.moveTo(center);
        level.addFreshEntity(blackHole);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.FINISH_ANIMATION;
    }
}
