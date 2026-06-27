package io.redspace.skillcasting.irons_spellbooks.spells.evocation;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import io.redspace.ironsspellbooks.entity.mobs.SummonedHorse;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.recast.RecastResult;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Optional;

public class SummonHorseSpell extends AbstractSpellSkill {

    private static final int SUMMON_DURATION_TICKS = 20 * 60 * 10;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(20)
            .build();

    public SummonHorseSpell() {
        this.manaCostPerLevel = 2;
        this.baseSpellPower = 100 - 15;
        this.spellPowerPerLevel = 15;
        this.castTime = 20;
        this.baseManaCost = 50;
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
        return PlayableSound.standard(SoundEvents.ILLUSIONER_PREPARE_MIRROR).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundEvents.ILLUSIONER_MIRROR_MOVE).toOpt();
    }

    @Override
    public Optional<RecastConfig> getRecastConfig(CastContext castContext) {
        return Optional.of(new RecastConfig(2, SUMMON_DURATION_TICKS));
    }

    @Override
    public void onRecastFinished(CastContext castContext, RecastResult result) {
        SummonManager.recastFinishedHelper(castContext, result);
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SpellcastingComponentTypes.SUMMON_HEALTH, 15 * getSpellPowerMultiplier(castContext));
    }

    @Override
    public void onCast(Level level, CastContext castContext) {
        Entity casterEntity = castContext.asEntityCaster();
        if (!(casterEntity instanceof LivingEntity caster)) {
            return;
        }

        if (!castContext.getSkillcastingData().recasts().hasRecast(this)) {
            SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();

            Vec3 forward = caster.getForward().normalize().scale(1.5);
            Vec3 spawn = castContext.position(PositionAnchor.ORIGIN).add(forward.x, 0.15, forward.z);

            SummonedHorse horse = new SummonedHorse(EntityRegistry.SPECTRAL_STEED.get(), level);
            horse.setPos(spawn);
            setAttributes(horse, castContext.getOrDefault(SpellcastingComponentTypes.SUMMON_HEALTH, 15f), getSpellPower(castContext) / 100f);
            var creature = NeoForge.EVENT_BUS.post(new SpellSummonEvent<>(caster, horse, getSkillId(), castContext.getSkillLevel())).getCreature();
            level.addFreshEntity(creature);
            SummonManager.initSummon(caster, creature, SUMMON_DURATION_TICKS, summonedEntitiesCastData);
            castContext.set(SpellcastingComponentTypes.SUMMONED_ENTITY_DATA, summonedEntitiesCastData);
        }
    }

    private void setAttributes(AbstractHorse horse, float health, float powerMultiplier) {
        float speed = .22f * powerMultiplier;
        float jump = .4f * powerMultiplier;
        int safeFall = 6 + (int) ((jump - 0.2f) * 3);

        horse.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(speed);
        horse.getAttribute(Attributes.JUMP_STRENGTH).setBaseValue(jump);
        horse.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        horse.getAttribute(Attributes.SAFE_FALL_DISTANCE).setBaseValue(safeFall);
        horse.setHealth(health);
    }
}
