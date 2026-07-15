package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.events.SpellSummonEvent;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.spells.SpellcastingComponentTypes;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonedEntitiesCastData;
import io.redspace.ironsspellbooks.entity.spells.summoned_weapons.SummonedClaymoreEntity;
import io.redspace.ironsspellbooks.entity.spells.summoned_weapons.SummonedRapierEntity;
import io.redspace.ironsspellbooks.entity.spells.summoned_weapons.SummonedSwordEntity;
import io.redspace.ironsspellbooks.entity.spells.summoned_weapons.SummonedWeaponEntity;
import io.redspace.ironsspellbooks.registries.EntityRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.skillcasting.api.PositionAnchor;
import io.redspace.skillcasting.api.cast.CastContext;
import io.redspace.skillcasting.api.recast.RecastConfig;
import io.redspace.skillcasting.api.recast.RecastResult;
import io.redspace.skillcasting.api.skill.CastType;
import io.redspace.skillcasting.data.PlayableSound;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

import java.util.List;
import java.util.Optional;

public class SummonSwordsSpell extends AbstractSpell {

    private static final int SUMMON_DURATION_TICKS = 20 * 60 * 10;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.ENDER_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(150)
            .build();

    public SummonSwordsSpell() {
        this.manaCostPerLevel = 15;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 2;
        this.castTime = 20;
        this.baseManaCost = 80;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(CastContext castContext) {
        float power = getSpellPower(castContext);
        return List.of(
                Component.translatable("ui.irons_spellbooks.summon_count", 3),
                Component.translatable("ui.irons_spellbooks.percent_damage", (int) (100 + getDamageBonus(power) * 100)),
                Component.translatable("ui.irons_spellbooks.percent_health", (int) (100 + getHealthBonus(power) * 100))
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
        return PlayableSound.standard(SoundRegistry.SUMMONED_SWORDS_CHARGE).toOpt();
    }

    @Override
    public Optional<PlayableSound> getOnCastSound(CastContext castContext) {
        return PlayableSound.standard(SoundRegistry.SUMMONED_SWORDS_CAST).toOpt();
    }

    @Override
    public Optional<RecastConfig> provideRecastConfig(CastContext castContext) {
        return Optional.of(new RecastConfig(2, SUMMON_DURATION_TICKS));
    }

    @Override
    public void onRecastFinished(CastContext castContext, RecastResult result) {
        SummonManager.recastFinishedHelper(castContext, result);
    }

    @Override
    public void onCast(ServerLevel level, CastContext castContext) {
        if (castContext.getSkillcastingData().recasts().hasRecast(this)) {
            return;
        }
        Entity caster = castContext.asEntityCaster();
        float power = getSpellPower(castContext);
        SummonedEntitiesCastData summonedEntitiesCastData = new SummonedEntitiesCastData();
        AttributeModifier healthModifier = new AttributeModifier(
                IronsSpellbooks.id("spell_power_health_bonus"), getHealthBonus(power), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        AttributeModifier damageModifier = new AttributeModifier(
                IronsSpellbooks.id("spell_power_damage_bonus"), getDamageBonus(power), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        SummonedWeaponEntity claymore = new SummonedClaymoreEntity(EntityRegistry.SUMMONED_CLAYMORE.get(), level);
        SummonedWeaponEntity rapier = new SummonedRapierEntity(EntityRegistry.SUMMONED_RAPIER.get(), level);
        SummonedWeaponEntity sword = new SummonedSwordEntity(EntityRegistry.SUMMONED_SWORD.get(), level);

        Vec3 spawnBase = castContext.position(PositionAnchor.ORIGIN).add(0, 1.2, 0);
        for (SummonedWeaponEntity weapon : List.of(claymore, rapier, sword)) {
            weapon.moveTo(spawnBase.add(Utils.getRandomVec3(1)));
            weapon.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(damageModifier);
            weapon.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(healthModifier);
            weapon.setHealth(weapon.getMaxHealth());
            Entity creature = weapon;
            if (caster instanceof LivingEntity living) {
                creature = NeoForge.EVENT_BUS.post(new SpellSummonEvent<>(living, weapon, getSkillId(), castContext.getSkillLevel())).getCreature();
            }
            level.addFreshEntity(creature);
            SummonManager.initSummon(caster, creature, SUMMON_DURATION_TICKS, summonedEntitiesCastData);
        }
        castContext.set(SpellcastingComponentTypes.SUMMONED_ENTITY_DATA, summonedEntitiesCastData);
    }

    private static double getHealthBonus(float spellPower) {
        return (spellPower - 1) * 0.10;
    }

    private static double getDamageBonus(float spellPower) {
        return (spellPower - 1) * 0.05;
    }
}
