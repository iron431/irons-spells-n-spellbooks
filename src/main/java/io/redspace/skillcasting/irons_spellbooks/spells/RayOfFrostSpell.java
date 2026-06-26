package io.redspace.skillcasting.irons_spellbooks.spells;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.ray_of_frost.RayOfFrostVisualEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import io.redspace.skillcasting.util.RaycastBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Optional;

public class RayOfFrostSpell extends AbstractSpellSkill {

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(15)
            .build();

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f), 2)),
                Component.translatable("ui.irons_spellbooks.freeze_time", Utils.timeFromTicks(getFreezeTime(castContext), 2)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f), 1))
        );
    }

    public RayOfFrostSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 6;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 25;
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
        return PlayableSound.standard(SoundRegistry.RAY_OF_FROST).toOpt();
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SkillcastingComponentTypes.DAMAGE, 3 + getSpellPower(castContext) * 1.5f);
        castContext.set(SkillcastingComponentTypes.CAST_RANGE, 30f);
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        var hitResult = RaycastBuilder.fromCast(castContext, PositionAnchor.CASTING_POSITION, castContext.getOrDefault(SkillcastingComponentTypes.CAST_RANGE, 0f))
                .checkForBlocks(true)
                .bbInflation(.15f)
                .build();
        level.addFreshEntity(new RayOfFrostVisualEntity(level, castContext.position(PositionAnchor.CASTING_POSITION), hitResult.getLocation(), castContext.asEntityCaster()));
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity target = ((EntityHitResult) hitResult).getEntity();
            //Set freeze time right here because it scales off of level and power
            DamageSources.applyDamage(target, castContext.getOrDefault(SkillcastingComponentTypes.DAMAGE, 0f),
                    getDamageSource(level, null, castContext.asEntityCaster()).indirect().setFreezeTicks(target.getTicksRequiredToFreeze() + getFreezeTime(castContext)));
            MagicManager.spawnParticles(level, ParticleHelper.ICY_FOG, hitResult.getLocation().x, target.getY(), hitResult.getLocation().z, 4, 0, 0, 0, .3, true);
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            MagicManager.spawnParticles(level, ParticleHelper.ICY_FOG, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z, 4, 0, 0, 0, .3, true);
        }
        MagicManager.spawnParticles(level, ParticleHelper.SNOWFLAKE, hitResult.getLocation().x, hitResult.getLocation().y, hitResult.getLocation().z, 50, 0, 0, 0, .3, false);
    }

    private int getFreezeTime(CastContext castContext) {
        return (int) (getSpellPower(castContext) * 15);
    }
}
