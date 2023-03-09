package io.redspace.ironsspellbooks.spells.lightning;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;


public class AscensionSpell extends AbstractSpell {
    public AscensionSpell() {
        this(1);
    }

    public AscensionSpell(int level) {
        super(SpellType.ASCENSION_SPELL);
        this.level = level;
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 10;
        this.cooldown = 100;
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.damage", getDamage(null)));

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {

        entity.addEffect(new MobEffectInstance(MobEffectRegistry.ASCENSION.get(), 40, getDamage(entity), false, false, true));

        super.onCast(level, entity, playerMagicData);
    }

    private int getDamage(LivingEntity caster) {
        return (int) getSpellPower(caster);
    }
}
