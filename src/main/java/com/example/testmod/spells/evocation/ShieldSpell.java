package com.example.testmod.spells.evocation;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.shield.ShieldEntity;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class ShieldSpell extends AbstractSpell {

    public ShieldSpell() {
        this(1);
    }

    public ShieldSpell(int level) {
        super(SpellType.SHIELD_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 2;
        this.baseManaCost = 35;
        this.cooldown = 200;
        this.castTime = 20;

        //TODO: remove these after tsting
        this.baseManaCost = 1;
        this.cooldown = 0;
        this.castTime = 0;

        uniqueInfo.add(Component.translatable("ui.testmod.hp", Utils.stringTruncation(getShieldHP(null), 1)));

    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.ILLUSIONER_CAST_SPELL);
    }

    @Override
    public void onCast(Level level, LivingEntity entity, PlayerMagicData playerMagicData) {
        ShieldEntity shield = new ShieldEntity(level, getShieldHP(entity));
        Vec3 spawn = Utils.raycastForEntity(level, entity, 5, true).getLocation();
        shield.setPos(spawn);
        shield.setRotation(entity.getXRot(), entity.getYRot());
        level.addFreshEntity(shield);
        super.onCast(level, entity, playerMagicData);
    }

    private float getShieldHP(LivingEntity caster) {
        return 10 + getSpellPower(caster);
    }

    //    @Override
//    public MutableComponent getUniqueInfo() {
//        return Component.translatable("ui.testmod.distance", Utils.stringTruncation(getDistance(null), 1));
//    }
}
