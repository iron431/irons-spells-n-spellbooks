package com.example.testmod.spells.lightning;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.registries.MobEffectRegistry;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
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
        super(SpellType.ASCENSION);
        this.level = level;
        this.manaCostPerLevel = 1;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 10;
        this.cooldown = 100;
        //uniqueInfo.add(Component.translatable("ui.testmod.damage", Utils.stringTruncation(getSpellPower(null), 1)));

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

//    @Override
//    public void onServerPreCast(Level level, LivingEntity entity, @Nullable PlayerMagicData playerMagicData) {
//        super.onServerPreCast(level, entity, playerMagicData);
//        entity.addEffect(new MobEffectInstance(MobEffectRegistry.ASCENDED.get(), 60, 0, false, false, true));
//        //entity.setDeltaMovement(new Vec3(0, 1, 0));
//        //entity.hasImpulse = true;
//    }
//
//    @Override
//    public void onClientPreCast(Level level, LivingEntity entity, InteractionHand hand, @Nullable PlayerMagicData playerMagicData) {
//        super.onClientPreCast(level, entity, hand, playerMagicData);
//        //entity.push(0, 1, 0);
//    }


    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {

        entity.addEffect(new MobEffectInstance(MobEffectRegistry.ASCENSION.get(), 60, 0, false, false, true));

        super.onCast(level, entity, playerMagicData);
    }
}
