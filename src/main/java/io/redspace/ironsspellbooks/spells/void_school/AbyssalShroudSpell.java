package io.redspace.ironsspellbooks.spells.void_school;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class AbyssalShroudSpell extends AbstractSpell {
    public AbyssalShroudSpell() {
        this(1);
    }

    public AbyssalShroudSpell(int level) {
        super(SpellType.ABYSSAL_SHROUD_SPELL);
        this.level = level;
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 200;
        this.spellPowerPerLevel = 50;
        this.castTime = 0;
        this.baseManaCost = 5;
        this.cooldown = 100;
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getSpellPower(null), 1)));
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.DARK_SPELL_02.get());
    }

    @Override
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.ABYSSAL_SHROUD.get(), (int) getSpellPower(entity), 0, false, false, true));
        super.onCast(world, entity, playerMagicData);
    }
}
