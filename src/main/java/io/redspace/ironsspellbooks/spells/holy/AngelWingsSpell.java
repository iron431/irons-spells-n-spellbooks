package io.redspace.ironsspellbooks.spells.holy;

import io.redspace.ironsspellbooks.capabilities.magic.PlayerMagicData;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.spells.AbstractSpell;
import io.redspace.ironsspellbooks.spells.SpellType;
import io.redspace.ironsspellbooks.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class AngelWingsSpell extends AbstractSpell {
    public AngelWingsSpell() {
        this(1);
    }

    public AngelWingsSpell(int level) {
        super(SpellType.ANGEL_WING_SPELL);
        this.level = level;
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 30;
        this.spellPowerPerLevel = 30;
        this.castTime = 0;
        this.baseManaCost = 60;
        uniqueInfo.add(Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getEffectDuration(null), 1)));

    }

    private int getEffectDuration(LivingEntity entity) {
        return (int) getSpellPower(entity) * 20;
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
    public void onCast(Level world, LivingEntity entity, PlayerMagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(MobEffectRegistry.ANGEL_WINGS.get(), getEffectDuration(entity)), entity);
        super.onCast(world, entity, playerMagicData);
    }
}
