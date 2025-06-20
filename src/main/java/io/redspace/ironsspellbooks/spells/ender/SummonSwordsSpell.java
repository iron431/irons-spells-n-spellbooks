package io.redspace.ironsspellbooks.spells.ender;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.summoned_weapons.SummonedClaymoreEntity;
import io.redspace.ironsspellbooks.entity.spells.summoned_weapons.SummonedRapierEntity;
import io.redspace.ironsspellbooks.entity.spells.summoned_weapons.SummonedSwordEntity;
import io.redspace.ironsspellbooks.entity.spells.summoned_weapons.SummonedWeaponEntity;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class SummonSwordsSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(IronsSpellbooks.MODID, "summon_swords");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.summon_count", 3),
                Component.translatable("ui.irons_spellbooks.percent_damage", (int) (100 + getDamageBonus(spellLevel, caster) * 100)),
                Component.translatable("ui.irons_spellbooks.percent_health", (int) (100 + getHealthBonus(spellLevel, caster) * 100))
        );
    }

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
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.SUMMONED_SWORDS_CHARGE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.SUMMONED_SWORDS_CAST.get());
    }

    public double getHealthBonus(int spellLevel, LivingEntity caster) {
        // 10% extra health for every extra spell power
        return (getSpellPower(spellLevel, caster) - 1) * .10;
    }

    public double getDamageBonus(int spellLevel, LivingEntity caster) {
        // 5% extra damage for every extra spell power
        return (getSpellPower(spellLevel, caster) - 1) * .05;
    }

    @Override
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        int summonTime = 20 * 60 * 10;
        AttributeModifier healthModifier = new AttributeModifier(IronsSpellbooks.id("spell_power_health_bonus"), getHealthBonus(spellLevel, entity), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        AttributeModifier damageModifier = new AttributeModifier(IronsSpellbooks.id("spell_power_damage_bonus"), getDamageBonus(spellLevel, entity), AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        SummonedWeaponEntity claymore = new SummonedClaymoreEntity(world, entity);
        SummonedWeaponEntity rapier = new SummonedRapierEntity(world, entity);
        SummonedWeaponEntity sword = new SummonedSwordEntity(world, entity);

        List<SummonedWeaponEntity> weapons = List.of(claymore, rapier, sword);
        weapons.forEach(weapon -> {
            weapon.moveTo(entity.position().add(0, 1.2, 0).add(Utils.getRandomVec3(1)));
            weapon.getAttribute(Attributes.ATTACK_DAMAGE).addPermanentModifier(damageModifier);
            weapon.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(healthModifier);
            weapon.setHealth(weapon.getMaxHealth());
            weapon.addEffect(new MobEffectInstance(MobEffectRegistry.SUMMONED_SWORD_TIMER, summonTime, 0, false, false, true));

            world.addFreshEntity(weapon);
        });


        int effectAmplifier = 2;
        if (entity.hasEffect(MobEffectRegistry.SUMMONED_SWORD_TIMER)) {
            effectAmplifier += entity.getEffect(MobEffectRegistry.SUMMONED_SWORD_TIMER).getAmplifier() + 1;
        }
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.SUMMONED_SWORD_TIMER, summonTime, effectAmplifier, false, false, true));
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }
}
