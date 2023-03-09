package io.redspace.ironsspellbooks.spells.ice;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.entity.mobs.SummonedPolarBear;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class SummonPolarBearSpell extends AbstractSpell {
    public SummonPolarBearSpell() {
        this(1);
    }

    public SummonPolarBearSpell(int level) {
        super(SpellType.SUMMON_POLAR_BEAR_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 4;
        this.spellPowerPerLevel = 1;
        this.castTime = 20;
        this.baseManaCost = 50;
        this.cooldown = 300;
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.summon_count", 1));
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.hp", getBearHealth(null)));
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.damage", getBearDamage(null)));

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.EVOKER_PREPARE_SUMMON);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.EVOKER_CAST_SPELL);
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        int summonTime = 20 * 60 * 3;

        SummonedPolarBear polarBear = new SummonedPolarBear(world, entity);
        polarBear.setPos(entity.position());

        polarBear.getAttributes().getInstance(Attributes.ATTACK_DAMAGE).setBaseValue(getBearDamage(entity));
        polarBear.getAttributes().getInstance(Attributes.MAX_HEALTH).setBaseValue(getBearHealth(entity));

        world.addFreshEntity(polarBear);

        polarBear.addEffect(new MobEffectInstance(MobEffectRegistry.POLAR_BEAR_TIMER.get(), summonTime, 0, false, false, false));
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.POLAR_BEAR_TIMER.get(), summonTime, 0, false, false, true));

        super.onCast(world, entity, playerMagicData);
    }

    private float getBearHealth(LivingEntity caster) {
        return 20 + level * 4;
    }

    private float getBearDamage(LivingEntity caster) {
        return getSpellPower(caster);
    }



}
