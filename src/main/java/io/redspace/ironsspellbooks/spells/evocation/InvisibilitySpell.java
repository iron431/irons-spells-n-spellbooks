package io.redspace.ironsspellbooks.spells.evocation;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
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
        this.manaCostPerLevel = 8;
        this.baseSpellPower = 10;
        this.spellPowerPerLevel = 5;
        this.castTime = 40;
        this.baseManaCost = 35;

        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDuration(null), 1)));
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
