package com.example.testmod.spells.fire;

import com.example.testmod.capabilities.magic.PlayerMagicData;
import com.example.testmod.spells.AbstractSpell;
import com.example.testmod.spells.SpellType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Fireball;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class FireballSpell extends AbstractSpell {
    public FireballSpell() {
        this(1);
    }

    public FireballSpell(int level) {
        super(SpellType.FIREBALL_SPELL);
        this.level = level;
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.castTime = 60;
        this.baseManaCost = 50;
        this.cooldown = 60;
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
        float speed = 2.5f;
        Vec3 direction = entity.getLookAngle().scale(speed);
        Vec3 origin = entity.getEyePosition();
        Fireball fireball = new LargeFireball(world, entity, direction.x(), direction.y(), direction.z(), (int) getSpellPower(entity));
        fireball.setPos(origin.add(direction));
        world.addFreshEntity(fireball);
        super.onCast(world, entity, playerMagicData);
    }
}
