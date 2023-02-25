package com.example.testmod.spells.evocation;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.registries.MobEffectRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class InvisibilitySpell extends AbstractSpell {
    public InvisibilitySpell() {
        this(1);
    }

    public InvisibilitySpell(int level) {
        super(SpellType.INVISIBILITY_SPELL);
        this.level = level;
        this.manaCostPerLevel = 4;
        this.baseSpellPower = 15;
        this.spellPowerPerLevel = 3;
        this.castTime = 40;
        this.baseManaCost = 30;
        this.cooldown = 30 * 20;

        uniqueInfo.add(Component.translatable("ui.testmod.effect_length", Utils.timeFromTicks(getDuration(null), 1)));
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.ILLUSIONER_PREPARE_MIRROR);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.TRUE_INVISIBILITY.get(), getDuration(entity), 0, false, false, true));
        super.onCast(world, entity, playerMagicData);
    }

    private int getDuration(LivingEntity source) {
        return (int) (getSpellPower(source) * 20);
    }

}
