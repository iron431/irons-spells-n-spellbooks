package io.redspace.skillcasting.irons_spellbooks.spells.ice;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import io.redspace.ironsspellbooks.entity.mobs.SummonedPolarBear;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import io.redspace.skillcasting.irons_spellbooks.AbstractSpellSkill;
import io.redspace.skillcasting.irons_spellbooks.SpellcastingComponentTypes;
import io.redspace.skillcasting.registry.SkillcastingComponentTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class SummonPolarBearSpell extends AbstractSpellSkill {
    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.hp", Utils.stringTruncation(castContext.getOrDefault(SpellcastingComponentTypes.SUMMON_HEALTH,10f), 1)),
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(castContext.getOrDefault(SpellcastingComponentTypes.SUMMON_ATTACK_DAMAGE,1f), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ICE_RESOURCE)
            .setMaxLevel(10)
            .setCooldownSeconds(180)
            .build();

    public SummonPolarBearSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
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
    public Optional<PlayableSound> getCastChannelSound(CastContext castContext) {
        return PlayableSound.standard(SoundEvents.EVOKER_PREPARE_SUMMON).toOpt();
    }

    @Override
    public Optional<RecastConfig> getRecastConfig(CastContext castContext) {
        return Optional.of(new RecastConfig(2, 20 * 60 * 10));
    }

    @Override
    public void buildContextComponents(CastContext castContext) {
        super.buildContextComponents(castContext);
        castContext.set(SpellcastingComponentTypes.SUMMON_HEALTH, provideBearHealth(castContext));
        castContext.set(SpellcastingComponentTypes.SUMMON_ATTACK_DAMAGE, provideBearDamage(castContext));
    }

    @Override
    public void onRecastFinished(CastContext castContext, io.redspace.skillcasting.api.recast.RecastResult result) {
        SummonManager.recastFinishedHelper(castContext, result);
    }

    @Override
    public void onCast(Level world, CastContext castContext) {
//        PlayerRecasts recasts = playerMagicData.getPlayerRecasts();
//        if (!recasts.hasRecastForSpell(this)) {
//            SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();
//            int summonTime = 20 * 60 * 10;
//
//            SummonedPolarBear polarBear = new SummonedPolarBear(world, entity);
//            polarBear.setPos(entity.position());
//
//            polarBear.getAttributes().getInstance(Attributes.ATTACK_DAMAGE).setBaseValue(getBearDamage(spellLevel, entity));
//            polarBear.getAttributes().getInstance(Attributes.MAX_HEALTH).setBaseValue(getBearHealth(spellLevel, entity));
//            polarBear.setHealth(polarBear.getMaxHealth());
//            var creature = NeoForge.EVENT_BUS.post(new SpellSummonEvent<>(entity, polarBear, this.spellId, spellLevel)).getCreature();
//            world.addFreshEntity(creature);
//            SummonManager.initSummon(entity, creature, summonTime, summonedEntitiesCastData);
//
//            RecastInstance recastInstance = new RecastInstance(this.getSpellId(), spellLevel, getRecastCount(spellLevel, entity), summonTime, castSource, summonedEntitiesCastData);
//            recasts.addRecast(recastInstance, playerMagicData);
//        }
//
//        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
        if (!castContext.getSkillcastingData().recasts().hasRecast(this)) {
            SummonedPolarBear polarBear = new SummonedPolarBear(EntityRegistry.SUMMONED_POLAR_BEAR.get(), world);
            polarBear.setPos(castContext.position(PositionAnchor.ORIGIN));
            polarBear.getAttributes().getInstance(Attributes.ATTACK_DAMAGE).setBaseValue(castContext.getOrDefault(SpellcastingComponentTypes.SUMMON_ATTACK_DAMAGE, 1f));
            polarBear.getAttributes().getInstance(Attributes.MAX_HEALTH).setBaseValue(castContext.getOrDefault(SpellcastingComponentTypes.SUMMON_HEALTH, 10f));
            polarBear.setHealth(polarBear.getMaxHealth());
            SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();
            // todo: summon event
            world.addFreshEntity(polarBear);
            SummonManager.initSummon(castContext.asEntityCaster(), polarBear, castContext.find(SkillcastingComponentTypes.RECAST_CONFIG).map(RecastConfig::durationTicks).orElse(20 * 60 * 10), summonedEntitiesCastData);
            castContext.set(SpellcastingComponentTypes.SUMMONED_ENTITY_DATA, summonedEntitiesCastData);
        }
    }

    private float provideBearHealth(CastContext castContext) {
        return (20 + castContext.getSkillLevel() * 4) * getSpellPowerMultiplier(castContext);
    }

    private float provideBearDamage(CastContext castContext) {
        return getSpellPower(castContext);
    }
}
