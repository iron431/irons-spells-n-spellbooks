package io.redspace.ironsspellbooks.spells.blood;

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
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getSpellPower(null), 1)));

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
