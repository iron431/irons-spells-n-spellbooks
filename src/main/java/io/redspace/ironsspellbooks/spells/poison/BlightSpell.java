package io.redspace.ironsspellbooks.spells.poison;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.capabilities.magic.CastTargetingData;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.effect.BlightEffect;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.*;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;


public class BlightSpell extends AbstractSpell {
    public BlightSpell() {
        this(1);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.reduced_healing",Utils.stringTruncation((1 + getAmplifier(caster)) * BlightEffect.HEALING_PER_LEVEL * -100, 1)),
                Component.translatable("ui.irons_spellbooks.reduced_damage", Utils.stringTruncation((1 + getAmplifier(caster)) * BlightEffect.DAMAGE_PER_LEVEL * -100, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDuration(caster), 1))
        );
    }

    public static DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchool(SchoolType.POISON)
            .setMaxLevel(8)
            .setCooldownSeconds(35)
            .build();

    public BlightSpell(int level) {
        super(SpellType.BLIGHT_SPELL);
        this.setLevel(level);
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 50;
        this.baseManaCost = 10;

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.BLIGHT_BEGIN.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }


    @Override
    public boolean checkPreCastConditions(Level level, LivingEntity entity, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, entity, playerMagicData, getSpellType(), 32, .35f);
    }

    @Override
    public void onCast(Level world, LivingEntity entity, MagicData playerMagicData) {
        if (playerMagicData.getAdditionalCastData() instanceof CastTargetingData targetData) {
            var targetEntity = targetData.getTarget((ServerLevel) world);
            if (targetEntity != null) {
                targetEntity.addEffect(new MobEffectInstance(MobEffectRegistry.BLIGHT.get(), getDuration(entity), getAmplifier(entity)));
            }
        }

        super.onCast(world, entity, playerMagicData);
    }

    public int getAmplifier(LivingEntity caster) {
        return (int) (getSpellPower(caster) * this.getLevel(caster) - 1);
    }

    public int getDuration(LivingEntity caster) {
        return (int) (getSpellPower(caster) * 20 * 30);
    }

}
