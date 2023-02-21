package com.example.testmod.spells.ender;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.magic_arrow.MagicArrowProjectile;
import com.example.testmod.registries.SoundRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class MagicArrowSpell extends AbstractSpell {
    public MagicArrowSpell(int level) {
        super(SpellType.MAGIC_ARROW_SPELL);
        this.level = level;
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 4;
        this.castTime = 35;
        this.baseManaCost = 50;
        this.cooldown = 600;
        uniqueInfo.add(Component.translatable("ui.testmod.damage", Utils.stringTruncation(getSpellPower(null), 1))) ;

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.MAGIC_ARROW_CHARGE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.MAGIC_ARROW_RELEASE.get());
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        MagicArrowProjectile magicArrow = new MagicArrowProjectile(level, entity);
        magicArrow.setPos(entity.position().add(0, entity.getEyeHeight() - magicArrow.getBoundingBox().getYsize() * .5f, 0).add(entity.getForward()));
        magicArrow.shoot(entity.getLookAngle());
        magicArrow.setDamage(getSpellPower(entity));
        level.addFreshEntity(magicArrow);
        super.onCast(level, entity, playerMagicData);
    }
}
