package com.example.testmod.spells.blood;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.registries.MobEffectRegistry;
import com.example.testmod.registries.SoundRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class HeartstopSpell extends AbstractSpell {
    public HeartstopSpell() {
        this(1);
    }

    public HeartstopSpell(int level) {
        super(SpellType.HEARTSTOP_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 300;
        this.spellPowerPerLevel = 20;
        this.castTime = 0;
        this.baseManaCost = 20;
        this.cooldown = 100;
        uniqueInfo.add(Component.translatable("ui.testmod.effect_length", Utils.timeFromTicks(getSpellPower(null), 1)));

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.HEARTSTOP_CAST.get());
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.HEARTSTOP.get(), (int) getSpellPower(entity), 0, false, false, true));
        super.onCast(world, entity, playerMagicData);
    }
}
