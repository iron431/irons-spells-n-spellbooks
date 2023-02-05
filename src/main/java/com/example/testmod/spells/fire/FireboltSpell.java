package com.example.testmod.spells.fire;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.entity.firebolt.FireboltProjectile;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import com.example.testmod.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class FireboltSpell extends AbstractSpell {
    public FireboltSpell() {
        this(1);
    }

    public FireboltSpell(int level) {
        super(SpellType.FIREBOLT_SPELL);
        this.level = level;
        this.manaCostPerLevel = 5;
        this.baseSpellPower = 2;
        this.spellPowerPerLevel = 2;
        this.castTime = 0;
        this.baseManaCost = 5;
        this.cooldown = 0;
        uniqueInfo.add(Component.translatable("ui.testmod.damage", Utils.stringTruncation(getSpellPower(null), 1)));
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
        FireboltProjectile firebolt = new FireboltProjectile(world, entity);
        firebolt.setPos(entity.position().add(0, entity.getEyeHeight() - firebolt.getBoundingBox().getYsize() * .5f, 0));
        firebolt.shoot(entity.getLookAngle());
        firebolt.setDamage(getSpellPower(entity));
        world.addFreshEntity(firebolt);
        super.onCast(world, entity, playerMagicData);
    }
}
